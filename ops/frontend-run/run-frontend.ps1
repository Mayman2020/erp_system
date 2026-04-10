#Requires -Version 5.1
# Start Angular dev server from erp-system-frontend (proxy: ops/frontend-run/proxy.conf.json via angular.json).
$ErrorActionPreference = 'Stop'
$Here = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = (Get-Item -LiteralPath $Here).Parent.Parent.FullName
Set-Location (Join-Path $RepoRoot 'erp-system-frontend')
npm run start
