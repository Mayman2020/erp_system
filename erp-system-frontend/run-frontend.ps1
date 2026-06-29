[CmdletBinding()]
param([switch]$SkipInstall)

$ErrorActionPreference = 'Stop'
$DefaultPort = 4200
$DefaultBackendApiUrl = '/api/v1'

$ProjectRoot = if ($PSScriptRoot) { $PSScriptRoot } else { Split-Path -Parent $MyInvocation.MyCommand.Path }
$ErpRoot = Split-Path -Parent $ProjectRoot
$RuntimeStateFile = Join-Path $ErpRoot ".runtime\launcher-state.json"
$RuntimeConfigJs = Join-Path $ProjectRoot "src\assets\runtime-config.js"

function Write-Step { param([string]$Message, [string]$Color = "Cyan") Write-Host $Message -ForegroundColor $Color }

Set-Location $ProjectRoot
$env:NG_CLI_ANALYTICS = "false"
$env:CI = "true"

$nodeVersion = (& node -v).TrimStart("v")
$major = [int]($nodeVersion.Split(".")[0])
if ($major -lt 20) {
    Write-Step "Node.js 20+ is required (detected v$nodeVersion)." "Red"
    exit 1
}

Write-Step "Using Node.js v$nodeVersion" "Green"

if (-not $SkipInstall -and -not (Test-Path "$ProjectRoot\node_modules")) {
    Write-Step "Installing dependencies..."
    npm install
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

$backendApiUrl = $DefaultBackendApiUrl
if (Test-Path $RuntimeStateFile) {
    try {
        $runtimeState = Get-Content -Path $RuntimeStateFile -Raw | ConvertFrom-Json
        if ($runtimeState.backendBaseUrl) {
            $backendApiUrl = [string]$runtimeState.backendBaseUrl
        }
    } catch {
        Write-Step "Runtime state file unreadable; using default $DefaultBackendApiUrl" "Yellow"
    }
} else {
    Write-Step "Runtime state not found; start backend first or using default $DefaultBackendApiUrl" "Yellow"
}

@"
window.__ERP_API_URL__ = '$backendApiUrl';
"@ | Set-Content -Path $RuntimeConfigJs -Encoding UTF8

Write-Step "Backend API: $backendApiUrl" "Cyan"

$servePort = $null
foreach ($candidatePort in 4200..4210) {
    $portInUse = Get-NetTCPConnection -LocalPort $candidatePort -State Listen -ErrorAction SilentlyContinue
    if (-not $portInUse) {
        $servePort = $candidatePort
        break
    }
}

if (-not $servePort) {
    Write-Step "No free port found between 4200 and 4210." "Red"
    exit 1
}

if ($servePort -ne 4200) {
    Write-Step "Port 4200 is busy. Switching to port $servePort." "Yellow"
}

Write-Step "Starting http://localhost:$servePort" "Green"
npx ng serve --project erp-system-frontend --port=$servePort
exit $LASTEXITCODE
