# ERP integration flow test — create → approve → convert chains
$ErrorActionPreference = 'Stop'
$base = if ($env:ERP_API_BASE_URL) { $env:ERP_API_BASE_URL.TrimEnd('/') } else { 'http://localhost:10080/api/v1' }
$username = if ($env:ERP_TEST_USERNAME) { $env:ERP_TEST_USERNAME } else { 'admin' }
$password = if ($env:ERP_TEST_PASSWORD) { $env:ERP_TEST_PASSWORD } else { 'admin' }
$replacementPassword = if ($env:ERP_TEST_NEW_PASSWORD) { $env:ERP_TEST_NEW_PASSWORD } else { 'Admin@Test2026!' }
$actor = 'integration-test'
$failures = @()
$passed = 0

function Assert-Ok {
    param([string]$Name, $Response)
    if ($null -eq $Response -or $Response.success -eq $false) {
        $msg = if ($Response.message) { $Response.message } else { 'null response' }
        $script:failures += "$Name : $msg"
        Write-Host "[FAIL] $Name - $msg" -ForegroundColor Red
        return $false
    }
    $script:passed++
    Write-Host "[OK] $Name" -ForegroundColor Green
    return $true
}

function Invoke-Api {
    param(
        [string]$Method = 'GET',
        [string]$Url,
        $Body = $null,
        [hashtable]$Headers
    )
    $params = @{
        Uri         = $Url
        Method      = $Method
        Headers     = $Headers
        ContentType = 'application/json'
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 10 -Compress)
    }
    return Invoke-RestMethod @params
}

Write-Host "=== ERP Integration Flow Test ===" -ForegroundColor Cyan

$login = Invoke-Api -Method POST -Url "$base/auth/login" -Body @{
    usernameOrEmail = $username
    password        = $password
}
if (-not (Assert-Ok 'Login' $login)) { exit 1 }
$h = @{ Authorization = "Bearer $($login.data.token)" }
if ($login.data.user.mustChangePassword) {
    Invoke-Api -Method PUT -Url "$base/profile/me/password" -Headers $h -Body @{
        currentPassword = $password
        newPassword     = $replacementPassword
    } | Out-Null
    $password = $replacementPassword
    $login = Invoke-Api -Method POST -Url "$base/auth/login" -Body @{
        usernameOrEmail = $username
        password        = $password
    }
    $h = @{ Authorization = "Bearer $($login.data.token)" }
    Assert-Ok 'Forced password change' $login | Out-Null
}

$customers = Invoke-Api -Url "$base/sales/customers" -Headers $h
$products = Invoke-Api -Url "$base/inventory/products" -Headers $h
$suppliers = Invoke-Api -Url "$base/purchases/suppliers" -Headers $h
$warehouses = Invoke-Api -Url "$base/inventory/warehouses" -Headers $h

$customerId = $customers.data[0].id
$productId = $products.data[0].id
$componentProductId = if ($products.data.Count -gt 1) { $products.data[1].id } else { $products.data[0].id }
$supplierId = $suppliers.data[0].id
$warehouseId = ($warehouses.data | Where-Object { $_.code -eq 'WH-MAIN' } | Select-Object -First 1).id
if (-not $warehouseId) { $warehouseId = $warehouses.data[0].id }
$today = (Get-Date).ToString('yyyy-MM-dd')

Write-Host "Using customer=$customerId product=$productId supplier=$supplierId warehouse=$warehouseId" -ForegroundColor DarkGray

# --- Sales: Quotation → Order → Invoice ---
$quote = Invoke-Api -Method POST -Url "$base/sales/quotations" -Headers $h -Body @{
    quotationDate  = $today
    validUntil     = $today
    customerId     = $customerId
    discountAmount = 0
    notes          = 'Integration test quotation'
    lines          = @(@{
            productId        = $productId
            description      = 'Test line'
            quantity         = 1
            unitPrice        = 100
            discountPercent  = 0
            taxPercent       = 15
        })
}
if (Assert-Ok 'Create quotation' $quote) {
    $qid = $quote.data.id
    $approvedQ = Invoke-Api -Method POST -Url "$base/sales/quotations/$qid/approve?actor=$actor" -Headers $h
    if (Assert-Ok 'Approve quotation' $approvedQ) {
        $order = Invoke-Api -Method POST -Url "$base/sales/quotations/$qid/convert-to-order" -Headers $h
        if (Assert-Ok 'Convert quotation to order' $order) {
            $oid = $order.data.id
            $approvedO = Invoke-Api -Method POST -Url "$base/sales/orders/$oid/approve?actor=$actor" -Headers $h
            if (Assert-Ok 'Approve sales order' $approvedO) {
                $invoice = Invoke-Api -Method POST -Url "$base/sales/orders/$oid/convert-to-invoice" -Headers $h
                if (Assert-Ok 'Convert order to invoice' $invoice) {
                    $iid = $invoice.data.id
                    $approvedInv = Invoke-Api -Method POST -Url "$base/sales/invoices/$iid/approve?actor=$actor" -Headers $h
                    Assert-Ok 'Approve sales invoice (stock + GL)' $approvedInv | Out-Null
                }
            }
        }
    }
}

# --- Purchases: PO → Invoice ---
$po = Invoke-Api -Method POST -Url "$base/purchases/orders" -Headers $h -Body @{
    orderDate      = $today
    supplierId     = $supplierId
    warehouseId    = $warehouseId
    discountAmount = 0
    notes          = 'Integration test PO'
    lines          = @(@{
            productId       = $productId
            description     = 'PO test line'
            quantity        = 2
            unitPrice       = 50
            discountPercent = 0
            taxPercent      = 15
        })
}
if (Assert-Ok 'Create purchase order' $po) {
    $poid = $po.data.id
    $approvedPo = Invoke-Api -Method POST -Url "$base/purchases/orders/$poid/approve?actor=$actor" -Headers $h
    if (Assert-Ok 'Approve purchase order' $approvedPo) {
        $pinv = Invoke-Api -Method POST -Url "$base/purchases/orders/$poid/convert-to-invoice" -Headers $h
        if (Assert-Ok 'Convert PO to purchase invoice' $pinv) {
            $piid = $pinv.data.id
            $approvedPi = Invoke-Api -Method POST -Url "$base/purchases/invoices/$piid/approve?actor=$actor" -Headers $h
            Assert-Ok 'Approve purchase invoice (stock + GL)' $approvedPi | Out-Null
        }
    }
}

# --- Accounting transfer: create + post ---
$accounts = Invoke-Api -Url "$base/accounting/accounts" -Headers $h
$src = ($accounts.data | Where-Object { $_.code -eq '1010' } | Select-Object -First 1)
$dst = ($accounts.data | Where-Object { $_.code -eq '1020' } | Select-Object -First 1)
if (-not $src) { $src = $accounts.data[0] }
if (-not $dst) { $dst = $accounts.data[1] }

if ($src -and $dst -and $src.id -ne $dst.id) {
    $xfer = Invoke-Api -Method POST -Url "$base/accounting/transfers" -Headers $h -Body @{
        transferDate         = $today
        reference            = "INT-XFER-$(Get-Date -Format 'HHmmss')"
        description          = 'Integration test transfer'
        amount               = 10
        sourceAccountId      = $src.id
        destinationAccountId = $dst.id
    }
    if (Assert-Ok 'Create transfer' $xfer) {
        $xid = $xfer.data.id
        $posted = Invoke-Api -Method POST -Url "$base/accounting/transfers/$xid/post?actor=$actor" -Headers $h
        Assert-Ok 'Post transfer (journal)' $posted | Out-Null
    }
} else {
    $failures += 'Transfer: need at least 2 accounts'
    Write-Host '[FAIL] Transfer — insufficient accounts' -ForegroundColor Red
}

# --- BOM save (idempotent: update the component line when it already exists) ---
$bomBody = @{
    parentProductId     = $productId
    componentProductId  = $componentProductId
    quantityPerUnit     = 1
}
$existingBom = Invoke-Api -Url "$base/manufacturing/bom?parentProductId=$productId" -Headers $h
$existingBomLine = $existingBom.data | Where-Object { $_.componentProductId -eq $componentProductId } | Select-Object -First 1
$bom = if ($existingBomLine) {
    Invoke-Api -Method PUT -Url "$base/manufacturing/bom/$($existingBomLine.id)" -Headers $h -Body $bomBody
} else {
    Invoke-Api -Method POST -Url "$base/manufacturing/bom" -Headers $h -Body $bomBody
}
Assert-Ok 'Save product BOM' $bom | Out-Null

Write-Host ""
Write-Host "Passed: $passed  Failed: $($failures.Count)" -ForegroundColor $(if ($failures.Count -eq 0) { 'Green' } else { 'Yellow' })
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    exit 1
}
exit 0
