#Requires -Version 5.1
<#
.SYNOPSIS
  Build and push ERP backend + frontend images to Docker Hub.

.EXAMPLE
  .\build-and-push.ps1

.EXAMPLE
  $env:IMAGE_TAG = "v1.0.0"; $env:NG_API_BASE_URL = "https://api.example.com/api/v1"; .\build-and-push.ps1
#>

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# --- Configuration (override via environment variables or edit defaults below) ---
$DockerUser = if ($env:DOCKER_USER) { $env:DOCKER_USER } else { 'mayman2020' }
$ImageTag = if ($env:IMAGE_TAG) { $env:IMAGE_TAG } else { 'latest' }
$NgApiBaseUrl = if ($env:NG_API_BASE_URL) { $env:NG_API_BASE_URL } else { 'http://93.127.141.227:10080/api/v1' }

$BackendImage = "${DockerUser}/erp-backend:${ImageTag}"
$FrontendImage = "${DockerUser}/erp-frontend:${ImageTag}"

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = $ScriptRoot
Set-Location -LiteralPath $RepoRoot

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host ("[{0}] {1}" -f (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ"), $Message)
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker not found in PATH. Install Docker Desktop and ensure docker is available."
}

Write-Step "Repository root: $RepoRoot"
Write-Step "IMAGE_TAG=$ImageTag"
Write-Step "NG_API_BASE_URL=$NgApiBaseUrl"
Write-Step "Backend image:  $BackendImage"
Write-Step "Frontend image: $FrontendImage"

Write-Step "Building backend..."
& docker build `
    -t $BackendImage `
    -f (Join-Path $RepoRoot 'erp-system-backend\Dockerfile') `
    (Join-Path $RepoRoot 'erp-system-backend')
if ($LASTEXITCODE -ne 0) { throw "docker build (backend) failed with exit code $LASTEXITCODE" }

Write-Step "Pushing backend..."
& docker push $BackendImage
if ($LASTEXITCODE -ne 0) { throw "docker push (backend) failed with exit code $LASTEXITCODE" }

Write-Step "Building frontend (baking API URL into the bundle)..."
& docker build `
    --build-arg "NG_API_BASE_URL=$NgApiBaseUrl" `
    -t $FrontendImage `
    -f (Join-Path $RepoRoot 'erp-system-frontend\Dockerfile') `
    (Join-Path $RepoRoot 'erp-system-frontend')
if ($LASTEXITCODE -ne 0) { throw "docker build (frontend) failed with exit code $LASTEXITCODE" }

Write-Step "Pushing frontend..."
& docker push $FrontendImage
if ($LASTEXITCODE -ne 0) { throw "docker push (frontend) failed with exit code $LASTEXITCODE" }

Write-Step "Done. Images pushed:"
Write-Step "  $BackendImage"
Write-Step "  $FrontendImage"
