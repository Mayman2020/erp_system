/**
 * CoreERP — API ↔ frontend integration smoke test.
 * Usage: node scripts/api-integration-test.mjs [baseUrl]
 * Default: http://localhost:8087/api/v1
 */
const BASE = (process.argv[2] || process.env.ERP_API_BASE || 'http://localhost:8087/api/v1').replace(/\/$/, '');
const LOGIN = {
  usernameOrEmail: process.env.ERP_LOGIN_USER || 'admin',
  password: process.env.ERP_LOGIN_PASS || 'Admin@123'
};

const results = [];
let token = '';

function pass(name, detail = '') {
  results.push({ name, ok: true, detail });
  console.log(`  ✓ ${name}${detail ? ` — ${detail}` : ''}`);
}

function fail(name, detail = '') {
  results.push({ name, ok: false, detail });
  console.log(`  ✗ ${name}${detail ? ` — ${detail}` : ''}`);
}

async function request(method, path, { body, expect = [200, 201], auth = true } = {}) {
  const headers = { Accept: 'application/json' };
  if (auth && token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined
  });

  let json = null;
  const text = await res.text();
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    json = { raw: text };
  }

  const ok = expect.includes(res.status);
  return { ok, status: res.status, json, path, method };
}

async function check(name, method, path, opts = {}) {
  try {
    const r = await request(method, path, opts);
    if (r.ok) {
      pass(name, `${r.status}`);
      return r.json;
    }
    const msg = r.json?.message || r.json?.error || '';
    fail(name, `HTTP ${r.status} ${msg}`.trim());
    return null;
  } catch (err) {
    fail(name, err.message);
    return null;
  }
}

async function main() {
  console.log(`\nCoreERP API Integration Test\nBase: ${BASE}\n`);

  const loginRes = await check('POST /auth/login', 'POST', '/auth/login', {
    body: LOGIN,
    auth: false
  });
  token = loginRes?.data?.token || loginRes?.data?.accessToken || '';
  if (!token) {
    console.error('\nLogin failed — cannot continue.\n');
    process.exit(1);
  }

  await check('GET /actuator/health', 'GET', '/actuator/health');

  // Accounting core
  await check('GET /accounting/accounts', 'GET', '/accounting/accounts');
  await check('GET /accounting/dashboard', 'GET', '/accounting/dashboard');
  await check('GET /accounting/journal-entries', 'GET', '/accounting/journal-entries');
  await check('GET /accounting/payment-vouchers', 'GET', '/accounting/payment-vouchers');
  await check('GET /accounting/receipt-vouchers', 'GET', '/accounting/receipt-vouchers');
  await check('GET /accounting/transactions', 'GET', '/accounting/transactions');
  await check('GET /accounting/invoices', 'GET', '/accounting/invoices');
  await check('GET /accounting/checks', 'GET', '/accounting/checks');
  await check('GET /accounting/bank-accounts', 'GET', '/accounting/bank-accounts');
  await check('GET /accounting/bills', 'GET', '/accounting/bills');
  await check('GET /accounting/budget', 'GET', '/accounting/budget');
  await check('GET /accounting/exchange-rates', 'GET', '/accounting/exchange-rates');
  await check('GET /accounting/transfers', 'GET', '/accounting/transfers');
  await check('GET /accounting/settings', 'GET', '/accounting/settings');

  // ERP dashboard & reports
  await check('GET /erp/dashboard', 'GET', '/erp/dashboard');
  await check('GET /erp/activity-logs', 'GET', '/erp/activity-logs');
  await check('GET /erp/reports/sales', 'GET', '/erp/reports/sales');
  await check('GET /erp/reports/purchases', 'GET', '/erp/reports/purchases');
  await check('GET /erp/reports/inventory', 'GET', '/erp/reports/inventory');
  await check('GET /erp/reports/profit', 'GET', '/erp/reports/profit');

  // Inventory
  await check('GET /inventory/products', 'GET', '/inventory/products');
  await check('GET /inventory/categories', 'GET', '/inventory/categories');
  await check('GET /inventory/warehouses', 'GET', '/inventory/warehouses');
  await check('GET /inventory/units', 'GET', '/inventory/units');
  await check('GET /inventory/stock/levels', 'GET', '/inventory/stock/levels');
  await check('GET /inventory/stock/low-stock', 'GET', '/inventory/stock/low-stock');
  await check('GET /inventory/stock/movements', 'GET', '/inventory/stock/movements');

  // Sales
  await check('GET /sales/customers', 'GET', '/sales/customers');
  await check('GET /sales/quotations', 'GET', '/sales/quotations');
  await check('GET /sales/orders', 'GET', '/sales/orders');
  await check('GET /sales/invoices', 'GET', '/sales/invoices');
  await check('GET /sales/returns', 'GET', '/sales/returns');

  // Purchases
  await check('GET /purchases/suppliers', 'GET', '/purchases/suppliers');
  await check('GET /purchases/orders', 'GET', '/purchases/orders');
  await check('GET /purchases/invoices', 'GET', '/purchases/invoices');
  await check('GET /purchases/returns', 'GET', '/purchases/returns');
  await check('GET /purchases/payments', 'GET', '/purchases/payments');

  // HR
  await check('GET /hr/departments', 'GET', '/hr/departments');
  await check('GET /hr/employees', 'GET', '/hr/employees');
  await check('GET /hr/attendance', 'GET', '/hr/attendance');
  await check('GET /hr/leave-requests', 'GET', '/hr/leave-requests');
  await check('GET /hr/payroll', 'GET', '/hr/payroll');
  await check('GET /hr/documents', 'GET', '/hr/documents');

  // CRM & Projects
  await check('GET /crm/leads', 'GET', '/crm/leads');
  await check('GET /crm/activities', 'GET', '/crm/activities');
  await check('GET /crm/notes', 'GET', '/crm/notes');
  await check('GET /projects', 'GET', '/projects');

  // Manufacturing
  await check('GET /manufacturing/work-orders', 'GET', '/manufacturing/work-orders');
  await check('GET /manufacturing/bom', 'GET', '/manufacturing/bom?parentProductId=1');

  const today = new Date().toISOString().slice(0, 10);

  // Admin / auth
  await check('GET /profile/me', 'GET', '/profile/me');
  await check('GET /ui/menu', 'GET', '/ui/menu');

  // Admin access management
  await check('GET /admin/access/context', 'GET', '/admin/access/context');
  await check('GET /admin/access/users', 'GET', '/admin/access/users');
  await check('GET /admin/access/roles', 'GET', '/admin/access/roles');
  await check('GET /admin/ui/menu-items', 'GET', '/admin/ui/menu-items');
  await check('GET /admin/screen-settings', 'GET', '/admin/screen-settings');

  const meForPermissions = await check('GET /profile/me (for effective-permissions)', 'GET', '/profile/me');
  const meId = meForPermissions?.data?.id;
  if (meId) {
    await check('GET /admin/access/users/{id}/effective-permissions', 'GET', `/admin/access/users/${meId}/effective-permissions`);
  }

  // Role CRUD round-trip (minimal permission on a guaranteed-existing menu item)
  const roleSuffix = Date.now();
  const createdRole = await check('POST /admin/access/roles', 'POST', '/admin/access/roles', {
    body: {
      code: `SMOKE_TEST_ROLE_${roleSuffix}`,
      nameEn: 'Smoke Test Role',
      nameAr: 'دور اختبار',
      active: true,
      permissions: [{ menuItemId: 'dashboard', canView: true, canCreate: false, canEdit: false, canDelete: false }]
    },
    expect: [200, 201]
  });
  const roleId = createdRole?.data?.id;
  if (roleId) {
    await check('PUT /admin/access/roles/{id}', 'PUT', `/admin/access/roles/${roleId}`, {
      body: {
        code: `SMOKE_TEST_ROLE_${roleSuffix}`,
        nameEn: 'Smoke Test Role Updated',
        nameAr: 'دور اختبار محدث',
        active: true,
        permissions: [{ menuItemId: 'dashboard', canView: true, canCreate: false, canEdit: false, canDelete: false }]
      }
    });
    await check('DELETE /admin/access/roles/{id}', 'DELETE', `/admin/access/roles/${roleId}`, { expect: [200, 204] });
  }

  // Screen settings toggle round-trip
  await check('PUT /admin/screen-settings/{key}', 'PUT', '/admin/screen-settings/smoke-test-screen', {
    body: { enabled: false }
  });
  await check('PUT /admin/screen-settings/{key} (revert)', 'PUT', '/admin/screen-settings/smoke-test-screen', {
    body: { enabled: true }
  });

  // Lookups admin CRUD round-trip
  const lookupSuffix = Date.now();
  const createdLookupType = await check('POST /admin/lookups/types', 'POST', '/admin/lookups/types', {
    body: { code: `SMOKE_TYPE_${lookupSuffix}`, nameEn: 'Smoke Type', nameAr: 'نوع اختبار', sortOrder: 999, active: true },
    expect: [200, 201]
  });
  const lookupTypeId = createdLookupType?.data?.id;
  const lookupTypeCode = createdLookupType?.data?.code;
  if (lookupTypeId && lookupTypeCode) {
    const createdLookupValue = await check('POST /admin/lookups/values', 'POST', '/admin/lookups/values', {
      body: { typeCode: lookupTypeCode, code: 'SMOKE_VALUE', nameEn: 'Smoke Value', nameAr: 'قيمة اختبار', sortOrder: 0, active: true },
      expect: [200, 201]
    });
    const lookupValueId = createdLookupValue?.data?.id;
    await check('GET /admin/lookups/values?typeCode=...', 'GET', `/admin/lookups/values?typeCode=${encodeURIComponent(lookupTypeCode)}`);
    if (lookupValueId) {
      await check('PUT /admin/lookups/values/{id}', 'PUT', `/admin/lookups/values/${lookupValueId}`, {
        body: { typeCode: lookupTypeCode, code: 'SMOKE_VALUE', nameEn: 'Smoke Value Updated', nameAr: 'قيمة محدثة', sortOrder: 0, active: true }
      });
      await check('DELETE /admin/lookups/values/{id}', 'DELETE', `/admin/lookups/values/${lookupValueId}`, { expect: [200, 204] });
    }
    await check('DELETE /admin/lookups/types/{id}', 'DELETE', `/admin/lookups/types/${lookupTypeId}`, { expect: [200, 204] });
  }

  // Notifications inbox
  await check('GET /notifications/my', 'GET', '/notifications/my?page=0&size=10');
  await check('GET /notifications/my/unread-count', 'GET', '/notifications/my/unread-count');
  await check('PATCH /notifications/my/read-all', 'PATCH', '/notifications/my/read-all');

  // Reconciliation read + create smoke
  await check('GET /accounting/reconciliation', 'GET', '/accounting/reconciliation');
  await check('GET /accounting/reconciliation/bank-accounts', 'GET', '/accounting/reconciliation/bank-accounts');
  const bankAccounts = await check('GET /accounting/bank-accounts (for reconciliation)', 'GET', '/accounting/bank-accounts');
  const bankAccountId = bankAccounts?.data?.[0]?.id;
  if (bankAccountId) {
    const createdReconciliation = await check('POST /accounting/reconciliation', 'POST', '/accounting/reconciliation', {
      body: {
        bankAccountId,
        statementStartDate: today,
        statementEndDate: today,
        openingBalance: 0,
        closingBalance: 0
      },
      expect: [200, 201]
    });
    const reconciliationId = createdReconciliation?.data?.id;
    if (reconciliationId) {
      await check('GET /accounting/reconciliation/{id}/summary', 'GET', `/accounting/reconciliation/${reconciliationId}/summary`);
    }
  }

  // Ledger + accounting reports
  const accountsForLedger = await check('GET /accounting/accounts (for ledger)', 'GET', '/accounting/accounts');
  const ledgerAccountId = accountsForLedger?.data?.[0]?.id;
  if (ledgerAccountId) {
    await check('GET /accounting/ledger', 'GET', `/accounting/ledger?accountId=${ledgerAccountId}`);
  }
  await check('GET /accounting/reports/profit-loss', 'GET', `/accounting/reports/profit-loss?fromDate=${today}&toDate=${today}`);
  await check('GET /accounting/reports/balance-sheet', 'GET', `/accounting/reports/balance-sheet?asOfDate=${today}`);

  // Password reset OTP send (dev log mode — no mail server required)
  await check('POST /auth/password/otp/send', 'POST', '/auth/password/otp/send', {
    body: { email: 'admin@erp.local' },
    auth: false
  });

  // Journal entry approve workflow
  if (ledgerAccountId && accountsForLedger?.data?.length > 1) {
    const secondAccountId = accountsForLedger.data.find((a) => a.id !== ledgerAccountId && a.active)?.id;
    if (secondAccountId) {
      const createdJournal = await check('POST /accounting/journal-entries', 'POST', '/accounting/journal-entries', {
        body: {
          entryDate: today,
          description: 'Smoke test journal entry',
          entryType: 'MANUAL',
          lines: [
            { accountId: ledgerAccountId, description: 'debit', debit: 1, credit: 0 },
            { accountId: secondAccountId, description: 'credit', debit: 0, credit: 1 }
          ]
        },
        expect: [200, 201]
      });
      const journalId = createdJournal?.data?.id;
      if (journalId) {
        await check('POST /accounting/journal-entries/{id}/approve', 'POST', `/accounting/journal-entries/${journalId}/approve?approvedBy=smoke-test`);
      }
    }
  }

  // Stock movement create + submit workflow
  const productsForMovement = await check('GET /inventory/products (for movement)', 'GET', '/inventory/products');
  const warehousesForMovement = await check('GET /inventory/warehouses (for movement)', 'GET', '/inventory/warehouses');
  const movementProductId = productsForMovement?.data?.[0]?.id;
  const movementWarehouseId = warehousesForMovement?.data?.[0]?.id;
  if (movementProductId && movementWarehouseId) {
    const createdMovement = await check('POST /inventory/stock/movements', 'POST', '/inventory/stock/movements', {
      body: {
        movementDate: today,
        movementType: 'IN',
        productId: movementProductId,
        warehouseId: movementWarehouseId,
        quantity: 1,
        notes: 'Smoke test movement'
      },
      expect: [200, 201]
    });
    const movementId = createdMovement?.data?.id;
    if (movementId) {
      await check('PUT /inventory/stock/movements/{id}/submit', 'PUT', `/inventory/stock/movements/${movementId}/submit`);
    }
  }

  // Exchange rate CRUD round-trip
  const created = await check('POST /accounting/exchange-rates', 'POST', '/accounting/exchange-rates', {
    body: {
      sourceCurrency: 'EUR',
      targetCurrency: 'SAR',
      rate: 4.05,
      effectiveDate: today
    },
    expect: [200, 201]
  });
  const rateId = created?.data?.id;
  if (rateId) {
    await check('GET /accounting/exchange-rates/{id}', 'GET', `/accounting/exchange-rates/${rateId}`);
    await check('PUT /accounting/exchange-rates/{id}', 'PUT', `/accounting/exchange-rates/${rateId}`, {
      body: {
        sourceCurrency: 'EUR',
        targetCurrency: 'SAR',
        rate: 4.1,
        effectiveDate: today
      }
    });
    await check('DELETE /accounting/exchange-rates/{id}', 'DELETE', `/accounting/exchange-rates/${rateId}`, {
      expect: [200, 204]
    });
  }

  const passed = results.filter((r) => r.ok).length;
  const total = results.length;
  console.log(`\n${passed}/${total} passed\n`);
  process.exit(passed === total ? 0 : 1);
}

main();
