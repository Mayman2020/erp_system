param(
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot
$nodeVersion = (& node -v).TrimStart("v")
$major = [int]($nodeVersion.Split(".")[0])

if ($major -lt 20) {
    Write-Host "Detected Node.js v$nodeVersion. Angular 21 requires Node.js 20+." -ForegroundColor Yellow
    $nvmCommand = Get-Command nvm -ErrorAction SilentlyContinue
    if ($nvmCommand) {
        Write-Host "Trying to switch to Node.js 22 using nvm..." -ForegroundColor Yellow
        nvm use 22 | Out-Host
        $nodeVersion = (& node -v).TrimStart("v")
        $major = [int]($nodeVersion.Split(".")[0])
    }

    if ($major -lt 20) {
        Write-Host "Node.js 20+ is required. Please install Node.js 20, 22, or 24." -ForegroundColor Red
        exit 1
    }
}

Write-Host "Using Node.js v$nodeVersion" -ForegroundColor Green

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
