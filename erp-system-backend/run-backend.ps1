<#
.SYNOPSIS
    Build and run the ERP System Spring Boot backend (restart-safe).

.DESCRIPTION
    Production-ready startup script that:
    - Automatically stops any existing Java process on port 8091 (restart behavior)
    - Configures Java/Maven environment
    - Runs Maven clean install
    - Starts Spring Boot with configurable profile

.PARAMETER Profile
    Spring profile to use. Default "" = erp_system DB. Use "dev" for erp_system_dev (must exist).

.PARAMETER SkipBuild
    Skip Maven build; only run the application.

.EXAMPLE
    .\run-backend.ps1
    .\run-backend.ps1 -SkipBuild
    .\run-backend.ps1 -Profile dev
#>

[CmdletBinding()]
param(
    [string]$Profile = "",
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$DefaultPort = 8091
$ExpectedProcess = "java"
$ContextPath = "/api/v1"
$BaseUrl = "http://localhost:$DefaultPort$ContextPath"

# ─── Script Root ─────────────────────────────────────────────────────────────
$ScriptDir = if ($PSScriptRoot) { $PSScriptRoot } else { Split-Path -Parent $MyInvocation.MyCommand.Path }
$ProjectRoot = $ScriptDir
$MvnwPath = Join-Path $ProjectRoot "mvnw.cmd"
$SecretsFile = Join-Path $ProjectRoot "run-backend.secrets.ps1"

# ─── Logging ─────────────────────────────────────────────────────────────────
function Write-Step {
    param([string]$Message, [string]$Color = "Cyan")
    $ts = Get-Date -Format "HH:mm:ss"
    Write-Host "[$ts] " -NoNewline
    Write-Host $Message -ForegroundColor $Color
}
function Write-Success { param([string]$Message) Write-Step $Message "Green" }
function Write-Warn { param([string]$Message) Write-Step $Message "Yellow" }
function Write-Err { param([string]$Message) Write-Step $Message "Red" }
function Write-Info { param([string]$Message) Write-Step $Message "Gray" }

# Local secrets (not committed)
# DO NOT hardcode passwords into this script. Put local secrets into `run-backend.secrets.ps1`
# (gitignored) or set env vars in your shell before running.
if ((-not $env:MAIL_PASSWORD -or $env:MAIL_PASSWORD.Trim() -eq "") -and (Test-Path $SecretsFile)) {
    Write-Info "Loading local secrets: $SecretsFile"
    . $SecretsFile
}
if (-not $env:MAIL_PASSWORD -or $env:MAIL_PASSWORD.Trim() -eq "") {
    Write-Warn "MAIL_PASSWORD is not set. Gmail SMTP emails will fail (emails will be logged as FAILED)."
    Write-Info "  Fix: create `run-backend.secrets.ps1` (gitignored) with: `$env:MAIL_PASSWORD = 'your-app-password'"
}

# ─── Port & Process (RESTART BEHAVIOR) ───────────────────────────────────────
function Get-ProcessOnPort {
    param([int]$Port)
    try {
        $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
        if ($conn) {
            $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
            return @{ Process = $proc; Connection = $conn }
        }
    } catch { }
    try {
        $line = netstat -ano 2>$null | Select-String ":\$Port\s+.*LISTENING" | Select-Object -First 1
        if ($line) {
            $parts = ($line -split '\s+')
            $pidVal = $parts[-1]
            if ($pidVal -match '^\d+$') {
                $proc = Get-Process -Id $pidVal -ErrorAction SilentlyContinue
                if ($proc) { return @{ Process = $proc } }
            }
        }
    } catch { }
    return $null
}

function Stop-ProcessOnPort {
    param([int]$Port, [string]$ExpectedName)
    $found = Get-ProcessOnPort -Port $Port
    if (-not $found) {
        Write-Info "Port $Port is free."
        return $true
    }
    $proc = $found.Process
    $pidVal = $proc.Id
    $procName = $proc.ProcessName
    $match = $procName -like "*$ExpectedName*"
    if (-not $match) {
        Write-Warn "Port $Port is in use by $procName (PID $pidVal), not $ExpectedName. Skipping kill for safety."
        return $false
    }
    Write-Step "Port $Port is in use by $procName (PID $pidVal) → stopping process..." "Yellow"
    try {
        Stop-Process -Id $pidVal -Force -ErrorAction Stop
    } catch {
        Write-Err "Failed to stop process: $_"
        return $false
    }
    Start-Sleep -Seconds 2
    if (Get-ProcessOnPort -Port $Port) {
        Write-Err "Process stopped but port $Port still in use."
        return $false
    }
    Write-Success "Process stopped successfully."
    return $true
}

# ─── Java setup ──────────────────────────────────────────────────────────────
$JavaCandidates = @(
    $env:JAVA_HOME,
    "D:\Progs\Progs Work\jdk_17_new_java",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\Microsoft\jdk-17*"
)
$ResolvedJavaHome = $null
foreach ($candidate in $JavaCandidates) {
    if (-not $candidate) { continue }
    $path = if ($candidate -match '\*') { (Get-Item $candidate -ErrorAction SilentlyContinue | Select-Object -First 1).FullName } else { $candidate }
    if ($path -and (Test-Path $path) -and (Test-Path (Join-Path $path "bin\java.exe"))) {
        $ResolvedJavaHome = $path
        break
    }
}
if (-not $ResolvedJavaHome) {
    Write-Err "JAVA_HOME not found. Set JAVA_HOME or install JDK 17."
    exit 1
}
$env:JAVA_HOME = $ResolvedJavaHome
$env:Path = "$($env:JAVA_HOME)\bin;$env:Path"
Write-Step "Java configured" "Cyan"
$prevErr = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
& java -version 2>&1 | ForEach-Object { Write-Info "  $_" }
$ErrorActionPreference = $prevErr

# ─── Maven wrapper ──────────────────────────────────────────────────────────
if (-not (Test-Path $MvnwPath)) {
    Write-Err "Maven wrapper not found: $MvnwPath"
    exit 1
}
Set-Location $ProjectRoot

# ─── Pre-flight: PostgreSQL port ─────────────────────────────────────────────
$DbName = if ($Profile -eq 'dev') { 'erp_system_dev' } else { 'erp_system' }
$pgPort = 5432
try {
    $pgListen = Get-NetTCPConnection -LocalPort $pgPort -State Listen -ErrorAction SilentlyContinue
    if (-not $pgListen) {
        Write-Warn "PostgreSQL does not appear to be listening on port $pgPort. Start PostgreSQL before running."
        Write-Info "  Database required: $DbName. Create with: psql -U postgres -c `"CREATE DATABASE $DbName`";"
    }
} catch { }

# ─── RESTART: Stop old backend on port 8091 ──────────────────────────────────
Write-Step "Checking port $DefaultPort..." "Cyan"
if (-not (Stop-ProcessOnPort -Port $DefaultPort -ExpectedName $ExpectedProcess)) {
    exit 1
}

# ─── Maven build ─────────────────────────────────────────────────────────────
if (-not $SkipBuild) {
    Write-Step "Maven build started..." "Cyan"
    & $MvnwPath clean install -U
    if ($LASTEXITCODE -ne 0) {
        Write-Err "Maven build FAILED."
        exit $LASTEXITCODE
    }
    Write-Success "Maven build finished successfully."
} else {
    Write-Info "Skipping build (-SkipBuild)."
}

# ─── Start backend ───────────────────────────────────────────────────────────
if ($Profile) {
    $env:SPRING_PROFILES_ACTIVE = $Profile
    Write-Step "Starting backend (profile: $Profile)..." "Cyan"
} else {
    if (Test-Path Env:SPRING_PROFILES_ACTIVE) { Remove-Item Env:SPRING_PROFILES_ACTIVE }
    Write-Step "Starting backend (default config)..." "Cyan"
}
Write-Info "  Server: $BaseUrl"
Write-Info "  Database: $(if ($Profile -eq 'dev') { 'erp_system_dev' } else { 'erp_system' })"
Write-Info "  Stop with Ctrl+C"
Write-Host ""

$runArgs = @("spring-boot:run")
if ($Profile) { $runArgs += "-Dspring-boot.run.profiles=$Profile" }

& $MvnwPath @runArgs
$exitCode = $LASTEXITCODE

Write-Host ""
if ($exitCode -eq 0) {
    Write-Success "Backend stopped normally."
} else {
    Write-Err "Backend exited with failure (exit code $exitCode)."
    Write-Info "Common causes: database not found (create erp_system, or erp_system_dev for -Profile dev), PostgreSQL not running, port in use."
    Write-Info "  Fix: Run 'psql -U postgres -c ""CREATE DATABASE $DbName""' then try again."
}
exit $exitCode