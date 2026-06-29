<#
.SYNOPSIS
    Build and run the ERP System Spring Boot backend (restart-safe).

.DESCRIPTION
    Production-ready startup script that:
    - If the port is used by a previous Java backend, stops it automatically
    - If the port is used by another app (e.g. Oracle TNS on 8080), picks the next free port
    - Configures Java/Maven environment
    - Runs Maven clean install
    - Starts Spring Boot with configurable profile

.PARAMETER Profile
    Spring profile to use. Default "" uses postgres DB with schema erp_system (see application-dev.yml when -Profile dev).

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
$ExpectedProcess = "java"
$ContextPath = "/api/v1"

# Must match application.yml / application-dev.yml server.port defaults
function Get-PreferredBackendPort {
    param([string]$ProfileName)
    if ($env:PORT -and $env:PORT.Trim() -ne "") {
        return [int]$env:PORT
    }
    if ($ProfileName -eq 'prod') { return 8080 }
    # application.yml defaults to dev profile; application-dev.yml uses 8081
    return 8081
}

function Resolve-BackendPort {
    param([string]$ProfileName)
    $preferred = Get-PreferredBackendPort -ProfileName $ProfileName
    $fallbacks = @(8081, 8091, 8082, 8090, 8092, 8083)
    $candidates = @($preferred) + $fallbacks | Where-Object { $_ -gt 0 } | Select-Object -Unique

    foreach ($port in $candidates) {
        $found = Get-ProcessOnPort -Port $port
        if (-not $found) {
            if ($port -ne $preferred) {
                Write-Warn "Preferred port $preferred is busy - using port $port instead."
            } else {
                Write-Info "Port $port is free."
            }
            return $port
        }

        $proc = $found.Process
        $pidVal = $proc.Id
        $procName = $proc.ProcessName
        if ($procName -like "*$ExpectedProcess*") {
            Write-Step "Port $port is in use by $procName (PID $pidVal) - stopping process..." "Yellow"
            try {
                Stop-Process -Id $pidVal -Force -ErrorAction Stop
            } catch {
                Write-Warn "Could not stop $procName on port $port - trying next port..."
                continue
            }
            Start-Sleep -Seconds 2
            if (-not (Get-ProcessOnPort -Port $port)) {
                Write-Success "Port $port cleared."
                return $port
            }
            Write-Warn "Port $port still busy after stop - trying next port..."
            continue
        }

        Write-Warn "Port $port is in use by $procName (PID $pidVal) - trying next port..."
    }

    return $null
}

# ─── Script Root ─────────────────────────────────────────────────────────────
$ScriptDir = if ($PSScriptRoot) { $PSScriptRoot } else { Split-Path -Parent $MyInvocation.MyCommand.Path }
$ProjectRoot = $ScriptDir
$MvnwPath = Join-Path $ProjectRoot "mvnw.cmd"
$ErpRoot = Split-Path -Parent $ProjectRoot
$RuntimeDir = Join-Path $ErpRoot ".runtime"
$RuntimeStateFile = Join-Path $RuntimeDir "launcher-state.json"
$ProxyFile = Join-Path $ErpRoot "ops\frontend-run\proxy.conf.json"
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
$DbLabel = if ($Profile -eq 'prod') { 'erp_db (schema: erp_system)' } else { 'postgres (schema: erp_system)' }
$pgPort = 5432
try {
    $pgListen = Get-NetTCPConnection -LocalPort $pgPort -State Listen -ErrorAction SilentlyContinue
    if (-not $pgListen) {
        Write-Warn "PostgreSQL does not appear to be listening on port $pgPort. Start PostgreSQL before running."
        Write-Info "  Required DB: $DbLabel. Flyway creates schema erp_system on first run."
    }
} catch { }

# ─── RESTART: Free preferred port or pick the next available one ─────────────
$ServerPort = Resolve-BackendPort -ProfileName $Profile
if (-not $ServerPort) {
    Write-Err "No free port found for the backend. Stop other services or set `$env:PORT."
    exit 1
}
$env:PORT = "$ServerPort"
$BaseUrl = "http://localhost:$ServerPort$ContextPath"

if (-not (Test-Path $RuntimeDir)) {
    New-Item -ItemType Directory -Path $RuntimeDir | Out-Null
}
@{
    backendPort = $ServerPort
    backendBaseUrl = $BaseUrl
    updatedAt = (Get-Date).ToString("o")
} | ConvertTo-Json | Set-Content -Path $RuntimeStateFile -Encoding UTF8
Write-Info "  Runtime state: $RuntimeStateFile"

if (Test-Path (Split-Path -Parent $ProxyFile)) {
    $proxyJson = @{
        "/api/v1" = @{
            target = "http://127.0.0.1:$ServerPort"
            secure = $false
            changeOrigin = $true
        }
    } | ConvertTo-Json -Depth 4
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($ProxyFile, $proxyJson + [Environment]::NewLine, $utf8NoBom)
    Write-Info "  Frontend proxy target: http://127.0.0.1:$ServerPort"
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
Write-Info "  Database: $DbLabel"
Write-Info "  Stop with Ctrl+C"
Write-Host ""

$runArgs = @("spring-boot:run", "-Dspring-boot.run.arguments=--server.port=$ServerPort")
if ($Profile) { $runArgs += "-Dspring-boot.run.profiles=$Profile" }

& $MvnwPath @runArgs
$exitCode = $LASTEXITCODE

Write-Host ""
if ($exitCode -eq 0) {
    Write-Success "Backend stopped normally."
} else {
    Write-Err "Backend exited with failure (exit code $exitCode)."
    Write-Info "Common causes: PostgreSQL not running, DB password wrong (set DB_PASS), port $ServerPort in use."
    Write-Info "  Dev DB: postgres on localhost:5432 with schema erp_system (Flyway creates it)."
}
exit $exitCode
