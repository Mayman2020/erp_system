# ERP API smoke test
$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8081/api/v1'
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

$login = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"usernameOrEmail":"admin","password":"Admin@123"}'
$h = @{ Authorization = "Bearer $($login.data.token)" }
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
