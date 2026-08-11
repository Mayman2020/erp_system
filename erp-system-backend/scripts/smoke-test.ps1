# ERP API smoke test
$ErrorActionPreference = 'Continue'
$base = if ($env:ERP_API_BASE_URL) { $env:ERP_API_BASE_URL.TrimEnd('/') } else { 'http://localhost:10080/api/v1' }
$username = if ($env:ERP_TEST_USERNAME) { $env:ERP_TEST_USERNAME } else { 'admin' }
$password = if ($env:ERP_TEST_PASSWORD) { $env:ERP_TEST_PASSWORD } else { 'admin' }
$replacementPassword = if ($env:ERP_TEST_NEW_PASSWORD) { $env:ERP_TEST_NEW_PASSWORD } else { 'Admin@Test2026!' }
$failures = @()
$passed = 0

function Test-Get {
    param([string]$Name, [string]$Url, [hashtable]$Headers)
    try {
        $r = Invoke-RestMethod -Uri $Url -Headers $Headers -Method GET
        if ($r.success -eq $false) { $script:failures += "$Name returned success=false"; Write-Host "[FAIL] $Name" -ForegroundColor Red }
        else { $script:passed++; Write-Host "[OK] $Name" -ForegroundColor Green }
    } catch {
        $script:failures += "$Name : $($_.Exception.Message)"
        Write-Host "[FAIL] $Name" -ForegroundColor Red
    }
}

$loginBody = @{ usernameOrEmail = $username; password = $password } | ConvertTo-Json -Compress
$login = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body $loginBody
$h = @{ Authorization = "Bearer $($login.data.token)" }
if ($login.data.user.mustChangePassword) {
    $changeBody = @{ currentPassword = $password; newPassword = $replacementPassword } | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$base/profile/me/password" -Headers $h -Method PUT -ContentType 'application/json' -Body $changeBody | Out-Null
    $password = $replacementPassword
    $loginBody = @{ usernameOrEmail = $username; password = $password } | ConvertTo-Json -Compress
    $login = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body $loginBody
    $h = @{ Authorization = "Bearer $($login.data.token)" }
}
Write-Host "Login OK" -ForegroundColor Green

@(
    @('Health', "$base/health"),
    @('UI Menu', "$base/ui/menu"),
    @('Dashboard', "$base/erp/dashboard"),
    @('Activity Logs', "$base/erp/activity-logs?page=0&size=5"),
    @('Sales Report', "$base/erp/reports/sales"),
    @('Products', "$base/inventory/products"),
    @('Low Stock', "$base/inventory/stock/low-stock"),
    @('Customers', "$base/sales/customers"),
    @('Quotations', "$base/sales/quotations"),
    @('Sales Orders', "$base/sales/orders"),
    @('Suppliers', "$base/purchases/suppliers"),
    @('PO', "$base/purchases/orders"),
    @('Employees', "$base/hr/employees"),
    @('Payroll', "$base/hr/payroll"),
    @('Payroll Lines', "$base/hr/payroll-lines"),
    @('HR Documents', "$base/hr/documents"),
    @('Leads', "$base/crm/leads"),
    @('Projects', "$base/projects"),
    @('Work Orders', "$base/manufacturing/work-orders"),
    @('BOM', "$base/manufacturing/bom?parentProductId=1"),
    @('Transfers', "$base/accounting/transfers"),
    @('Bills', "$base/accounting/bills"),
    @('Accounts', "$base/accounting/accounts")
) | ForEach-Object { Test-Get $_[0] $_[1] $h }

Write-Host "`nPassed: $passed  Failed: $($failures.Count)"
$failures | ForEach-Object { Write-Host $_ -ForegroundColor Red }
if ($failures.Count -gt 0) { exit 1 }
