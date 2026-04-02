param(
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot
$nodeVersion = (& node -v).TrimStart("v")
$major = [int]($nodeVersion.Split(".")[0])

if ($major -lt 16 -or $major -ge 19) {
    Write-Host "Detected Node.js v$nodeVersion (outside recommended range 16/18)." -ForegroundColor Yellow
    $nvmCommand = Get-Command nvm -ErrorAction SilentlyContinue
    if ($nvmCommand) {
        Write-Host "Trying to switch to Node.js 18 using nvm..." -ForegroundColor Yellow
        nvm use 18 | Out-Host
        $nodeVersion = (& node -v).TrimStart("v")
        $major = [int]($nodeVersion.Split(".")[0])
    }

    if ($major -lt 16 -or $major -ge 19) {
        Write-Host "Continuing with Node.js v$nodeVersion. If startup fails, install/use Node 18." -ForegroundColor Yellow
    }
}

$env:NODE_OPTIONS = "--openssl-legacy-provider"

if (-not $SkipInstall -and -not (Test-Path "$PSScriptRoot\node_modules")) {
    Write-Host "Installing dependencies..."
    npm install
}

Write-Host "Starting frontend..."
$servePort = $null
foreach ($candidatePort in 4200..4210) {
    $portInUse = Get-NetTCPConnection -LocalPort $candidatePort -State Listen -ErrorAction SilentlyContinue
    if (-not $portInUse) {
        $servePort = $candidatePort
        break
    }
}

if (-not $servePort) {
    Write-Host "No free port found between 4200 and 4210." -ForegroundColor Red
    exit 1
}

if ($servePort -ne 4200) {
    Write-Host "Port 4200 is busy. Switching to port $servePort." -ForegroundColor Yellow
}
npx ng serve --port=$servePort
