import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const appRoot = path.join(__dirname, '..', 'src', 'app', 'modules');

const listPageScss = `.erp-list-page__card {
  margin-top: 1rem;
}

.erp-list-page__card .erp-card__body {
  padding: 1.25rem;
}

[data-theme='dark'] .erp-list-page__card {
  background: var(--erp-surface-elevated, rgba(255, 255, 255, 0.04));
  border-color: var(--erp-border-subtle, rgba(255, 255, 255, 0.08));
}
`;

const reportPageScss = `.erp-report-page__card {
  margin-top: 1rem;
}

.erp-report-page__card .erp-card__body {
  padding: 1.25rem;
}

.erp-report-page__summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.erp-report-page__metric {
  padding: 1rem;
  border-radius: var(--erp-radius-md, 8px);
  background: var(--erp-surface-muted, rgba(0, 0, 0, 0.03));
  border: 1px solid var(--erp-border-subtle, rgba(0, 0, 0, 0.08));
}

.erp-report-page__metric-label {
  font-size: 0.8rem;
  opacity: 0.75;
  margin-bottom: 0.35rem;
}

.erp-report-page__metric-value {
  font-size: 1.25rem;
  font-weight: 600;
}

[data-theme='dark'] .erp-report-page__metric {
  background: var(--erp-surface-elevated, rgba(255, 255, 255, 0.04));
  border-color: var(--erp-border-subtle, rgba(255, 255, 255, 0.08));
}
`;

function writeFile(filePath, content) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, content, 'utf8');
  return filePath;
}

function pascalCase(name) {
  return name.split('-').map((p) => p.charAt(0).toUpperCase() + p.slice(1)).join('');
}

function generateListPage(moduleDir, page) {
  const className = `${pascalCase(page.fileName)}PageComponent`;
  const selector = `app-${page.fileName}-page`;
  const base = path.join(moduleDir, page.fileName);

  const ts = `import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: '${selector}',
  templateUrl: './${page.fileName}-page.component.html',
  styleUrls: ['./${page.fileName}-page.component.scss']
})
export class ${className} implements OnInit {
  titleKey = '${page.titleKey}';
  loading = false;
  errorKey = '';
  rows: Array<Record<string, unknown>> = [];
  statusOptions: string[] = ${JSON.stringify(page.statusOptions || ['DRAFT', 'APPROVED', 'CANCELLED'])};
  columns = ${JSON.stringify(page.columns, null, 4).replace(/"(\w+)":/g, '$1:')};
  showDateRange = ${page.showDateRange};
  showStatus = ${page.showStatus};

  private filters: Record<string, string> = {};

  constructor(private api: ErpApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  onSearch(filters: Record<string, string>): void {
    this.filters = filters || {};
    this.load();
  }

  private load(): void {
    this.loading = true;
    this.errorKey = '';
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.showStatus && this.filters.status) {
      params.status = this.filters.status;
    }
    if (this.showDateRange) {
      if (this.filters.fromDate) params.fromDate = this.filters.fromDate;
      if (this.filters.toDate) params.toDate = this.filters.toDate;
    }

    this.api.${page.apiMethod}(params)
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (rows) => {
          this.rows = (rows || []).map((row) => this.mapRow(row as Record<string, unknown>));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }

  private mapRow(row: Record<string, unknown>): Record<string, unknown> {
    const mapped = { ...row };
${page.rowMap || ''}
    return mapped;
  }
}
`;

  const html = `<section class="erp-page erp-list-page">
  <app-page-header [titleKey]="titleKey"></app-page-header>

  <div class="erp-card erp-list-page__card">
    <div class="erp-card__body">
      <app-advanced-search-bar
        [showDateRange]="${page.showDateRange}"
        [showStatus]="${page.showStatus}"
        [statusOptions]="statusOptions"
        (search)="onSearch($event)"
      ></app-advanced-search-bar>

      <div class="alert alert-danger alert-dismissible fade show mt-3" *ngIf="errorKey">
        {{ errorKey | translate }}
        <button type="button" class="btn-close" (click)="errorKey = ''"></button>
      </div>

      <app-data-table [columns]="columns" [data]="rows" [loading]="loading"></app-data-table>
    </div>
  </div>
</section>
`;

  writeFile(`${base}-page.component.ts`, ts);
  writeFile(`${base}-page.component.html`, html);
  writeFile(`${base}-page.component.scss`, listPageScss);
  return className;
}

function generateReportPage(moduleDir, page) {
  const className = `${pascalCase(page.fileName)}PageComponent`;
  const selector = `app-${page.fileName}-page`;
  const base = path.join(moduleDir, page.fileName);

  const ts = `import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: '${selector}',
  templateUrl: './${page.fileName}-page.component.html',
  styleUrls: ['./${page.fileName}-page.component.scss']
})
export class ${className} implements OnInit {
  titleKey = '${page.titleKey}';
  loading = false;
  errorKey = '';
  summary: Record<string, unknown> = {};
  rows: Array<Record<string, unknown>> = [];
  columns = ${JSON.stringify(page.columns, null, 4).replace(/"(\w+)":/g, '$1:')};
  dateFilter = ${page.dateFilter};

  private filters: Record<string, string> = {};

  constructor(private api: ErpApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  onSearch(filters: Record<string, string>): void {
    this.filters = filters || {};
    this.load();
  }

  private load(): void {
    this.loading = true;
    this.errorKey = '';
    const fromDate = this.filters.fromDate || '';
    const toDate = this.filters.toDate || '';

    this.api.${page.apiMethod}(${page.dateFilter ? 'fromDate || undefined, toDate || undefined' : ''})
      .pipe(finalize(() => { this.loading = false; this.cdr.detectChanges(); }))
      .subscribe({
        next: (data) => {
          this.summary = {
            ${page.summaryFields.map((f) => `${f.key}: data?.${f.key}`).join(',\n            ')}
          };
          this.rows = ${page.rowsMap};
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.summary = {};
          this.rows = [];
        }
      });
  }
}
`;

  const html = `<section class="erp-page erp-report-page">
  <app-page-header [titleKey]="titleKey"></app-page-header>

  <div class="erp-card erp-report-page__card">
    <div class="erp-card__body">
      <app-advanced-search-bar
        [showDateRange]="${page.dateFilter}"
        [showStatus]="false"
        (search)="onSearch($event)"
      ></app-advanced-search-bar>

      <div class="alert alert-danger alert-dismissible fade show mt-3" *ngIf="errorKey">
        {{ errorKey | translate }}
        <button type="button" class="btn-close" (click)="errorKey = ''"></button>
      </div>

      <div class="erp-report-page__summary" *ngIf="!loading && !errorKey">
        ${page.summaryFields.map((f) => `<div class="erp-report-page__metric">
          <div class="erp-report-page__metric-label">{{ '${f.label}' | translate }}</div>
          <div class="erp-report-page__metric-value">{{ summary.${f.key} }}</div>
        </div>`).join('\n        ')}
      </div>

      <app-data-table [columns]="columns" [data]="rows" [loading]="loading"></app-data-table>
    </div>
  </div>
</section>
`;

  writeFile(`${base}-page.component.ts`, ts);
  writeFile(`${base}-page.component.html`, html);
  writeFile(`${base}-page.component.scss`, reportPageScss);
  return className;
}

const amountFmt = `    ['totalAmount', 'subtotal', 'taxAmount', 'discountAmount', 'paidAmount', 'remainingAmount', 'amount', 'quantity', 'availableQuantity', 'costPrice', 'salePrice', 'budget', 'basicSalary', 'totalSales', 'totalPurchases', 'netProfit', 'totalQuantity'].forEach((key) => {
      if (mapped[key] !== undefined && mapped[key] !== null && typeof mapped[key] === 'number') {
        mapped[key] = Number(mapped[key]).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      }
    });`;

const activeStatusMap = `    if (typeof mapped.active === 'boolean') {
      mapped.status = mapped.active ? 'ACTIVE' : 'INACTIVE';
    }`;

const modules = [
  {
    name: 'inventory',
    moduleClass: 'InventoryModule',
    pages: [
      { fileName: 'products', route: 'products', titleKey: 'MENU.PRODUCTS', apiMethod: 'getProducts', showDateRange: false, showStatus: false,
        columns: [{ key: 'code', title: 'ERP.CODE' }, { key: 'name', title: 'COMMON.NAME' }, { key: 'categoryName', title: 'MENU.CATEGORIES' }, { key: 'salePrice', title: 'ERP.SALE_PRICE', align: 'end' }, { key: 'totalQuantity', title: 'ERP.QUANTITY', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: activeStatusMap },
      { fileName: 'categories', route: 'categories', titleKey: 'MENU.CATEGORIES', apiMethod: 'getCategories', showDateRange: false, showStatus: false,
        columns: [{ key: 'code', title: 'ERP.CODE' }, { key: 'name', title: 'COMMON.NAME' }, { key: 'parentCode', title: 'ERP.PARENT' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: activeStatusMap },
      { fileName: 'warehouses', route: 'warehouses', titleKey: 'MENU.WAREHOUSES', apiMethod: 'getWarehouses', showDateRange: false, showStatus: false,
        columns: [{ key: 'code', title: 'ERP.CODE' }, { key: 'name', title: 'COMMON.NAME' }, { key: 'location', title: 'ERP.LOCATION' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: activeStatusMap },
      { fileName: 'stock-levels', route: 'stock-levels', titleKey: 'MENU.STOCK_LEVELS', apiMethod: 'getStockLevels', showDateRange: false, showStatus: false,
        columns: [{ key: 'productCode', title: 'ERP.PRODUCT' }, { key: 'productName', title: 'COMMON.NAME' }, { key: 'warehouseName', title: 'MENU.WAREHOUSES' }, { key: 'quantity', title: 'ERP.QUANTITY', align: 'end' }, { key: 'availableQuantity', title: 'ERP.AVAILABLE', align: 'end' }],
        rowMap: amountFmt },
      { fileName: 'movements', route: 'movements', titleKey: 'MENU.STOCK_MOVEMENTS', apiMethod: 'getStockMovements', showDateRange: true, showStatus: true,
        columns: [{ key: 'movementNumber', title: 'ERP.NUMBER' }, { key: 'movementDate', title: 'ERP.DATE', kind: 'date' }, { key: 'movementType', title: 'ERP.TYPE' }, { key: 'productName', title: 'ERP.PRODUCT' }, { key: 'quantity', title: 'ERP.QUANTITY', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt }
    ]
  },
  {
    name: 'sales',
    moduleClass: 'SalesModule',
    pages: [
      { fileName: 'customers', route: 'customers', titleKey: 'MENU.CUSTOMERS', apiMethod: 'getCustomers', showDateRange: false, showStatus: false,
        columns: [{ key: 'code', title: 'ERP.CODE' }, { key: 'nameEn', title: 'COMMON.NAME' }, { key: 'email', title: 'ERP.EMAIL' }, { key: 'phone', title: 'ERP.PHONE' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: activeStatusMap },
      { fileName: 'quotations', route: 'quotations', titleKey: 'MENU.SALES_QUOTATIONS', apiMethod: 'getSalesQuotations', showDateRange: true, showStatus: true,
        columns: [{ key: 'quotationNumber', title: 'ERP.NUMBER' }, { key: 'quotationDate', title: 'ERP.DATE', kind: 'date' }, { key: 'customerName', title: 'MENU.CUSTOMERS' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt },
      { fileName: 'orders', route: 'orders', titleKey: 'MENU.SALES_ORDERS', apiMethod: 'getSalesOrders', showDateRange: true, showStatus: true,
        columns: [{ key: 'orderNumber', title: 'ERP.NUMBER' }, { key: 'orderDate', title: 'ERP.DATE', kind: 'date' }, { key: 'customerName', title: 'MENU.CUSTOMERS' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt },
      { fileName: 'invoices', route: 'invoices', titleKey: 'MENU.SALES_INVOICES', apiMethod: 'getSalesInvoices', showDateRange: true, showStatus: true,
        columns: [{ key: 'invoiceNumber', title: 'ERP.NUMBER' }, { key: 'invoiceDate', title: 'ERP.DATE', kind: 'date' }, { key: 'customerName', title: 'MENU.CUSTOMERS' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'remainingAmount', title: 'ERP.REMAINING', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt },
      { fileName: 'returns', route: 'returns', titleKey: 'MENU.SALES_RETURNS', apiMethod: 'getSalesReturns', showDateRange: true, showStatus: true,
        columns: [{ key: 'returnNumber', title: 'ERP.NUMBER' }, { key: 'returnDate', title: 'ERP.DATE', kind: 'date' }, { key: 'customerName', title: 'MENU.CUSTOMERS' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt }
    ]
  },
  {
    name: 'purchases',
    moduleClass: 'PurchasesModule',
    pages: [
      { fileName: 'suppliers', route: 'suppliers', titleKey: 'MENU.SUPPLIERS', apiMethod: 'getSuppliers', showDateRange: false, showStatus: false,
        columns: [{ key: 'code', title: 'ERP.CODE' }, { key: 'nameEn', title: 'COMMON.NAME' }, { key: 'email', title: 'ERP.EMAIL' }, { key: 'phone', title: 'ERP.PHONE' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: activeStatusMap },
      { fileName: 'orders', route: 'orders', titleKey: 'MENU.PURCHASE_ORDERS', apiMethod: 'getPurchaseOrders', showDateRange: true, showStatus: true,
        columns: [{ key: 'orderNumber', title: 'ERP.NUMBER' }, { key: 'orderDate', title: 'ERP.DATE', kind: 'date' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt },
      { fileName: 'invoices', route: 'invoices', titleKey: 'MENU.PURCHASE_INVOICES', apiMethod: 'getPurchaseInvoices', showDateRange: true, showStatus: true,
        columns: [{ key: 'invoiceNumber', title: 'ERP.NUMBER' }, { key: 'invoiceDate', title: 'ERP.DATE', kind: 'date' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'remainingAmount', title: 'ERP.REMAINING', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt },
      { fileName: 'returns', route: 'returns', titleKey: 'MENU.PURCHASE_RETURNS', apiMethod: 'getPurchaseReturns', showDateRange: true, showStatus: true,
        columns: [{ key: 'returnNumber', title: 'ERP.NUMBER' }, { key: 'returnDate', title: 'ERP.DATE', kind: 'date' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt },
      { fileName: 'payments', route: 'payments', titleKey: 'MENU.SUPPLIER_PAYMENTS', apiMethod: 'getSupplierPayments', showDateRange: true, showStatus: true,
        columns: [{ key: 'paymentNumber', title: 'ERP.NUMBER' }, { key: 'paymentDate', title: 'ERP.DATE', kind: 'date' }, { key: 'amount', title: 'ERP.AMOUNT', align: 'end' }, { key: 'paymentMethod', title: 'ERP.PAYMENT_METHOD' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt }
    ]
  },
  {
    name: 'hr',
    moduleClass: 'HrModule',
    pages: [
      { fileName: 'departments', route: 'departments', titleKey: 'MENU.DEPARTMENTS', apiMethod: 'getDepartments', showDateRange: false, showStatus: false,
        columns: [{ key: 'code', title: 'ERP.CODE' }, { key: 'nameEn', title: 'COMMON.NAME' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: activeStatusMap },
      { fileName: 'employees', route: 'employees', titleKey: 'MENU.EMPLOYEES', apiMethod: 'getEmployees', showDateRange: false, showStatus: false,
        columns: [{ key: 'employeeCode', title: 'ERP.CODE' }, { key: 'fullNameEn', title: 'COMMON.NAME' }, { key: 'jobTitle', title: 'ERP.JOB_TITLE' }, { key: 'hireDate', title: 'ERP.HIRE_DATE', kind: 'date' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: activeStatusMap },
      { fileName: 'attendance', route: 'attendance', titleKey: 'MENU.ATTENDANCE', apiMethod: 'getAttendanceRecords', showDateRange: true, showStatus: false,
        columns: [{ key: 'employeeId', title: 'MENU.EMPLOYEES' }, { key: 'attendanceDate', title: 'ERP.DATE', kind: 'date' }, { key: 'checkIn', title: 'ERP.CHECK_IN' }, { key: 'checkOut', title: 'ERP.CHECK_OUT' }, { key: 'status', title: 'COMMON.STATUS' }] },
      { fileName: 'leave-requests', route: 'leave-requests', titleKey: 'MENU.LEAVE_REQUESTS', apiMethod: 'getLeaveRequests', showDateRange: true, showStatus: true,
        columns: [{ key: 'employeeId', title: 'MENU.EMPLOYEES' }, { key: 'leaveType', title: 'ERP.LEAVE_TYPE' }, { key: 'startDate', title: 'ERP.START_DATE', kind: 'date' }, { key: 'endDate', title: 'ERP.END_DATE', kind: 'date' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }] },
      { fileName: 'payroll', route: 'payroll', titleKey: 'MENU.PAYROLL', apiMethod: 'getPayrollRuns', showDateRange: true, showStatus: true,
        columns: [{ key: 'payrollNumber', title: 'ERP.NUMBER' }, { key: 'periodStart', title: 'ERP.START_DATE', kind: 'date' }, { key: 'periodEnd', title: 'ERP.END_DATE', kind: 'date' }, { key: 'totalAmount', title: 'ERP.TOTAL', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt }
    ]
  },
  {
    name: 'crm',
    moduleClass: 'CrmModule',
    pages: [
      { fileName: 'leads', route: 'leads', titleKey: 'MENU.LEADS', apiMethod: 'getLeads', showDateRange: false, showStatus: false,
        columns: [{ key: 'leadNumber', title: 'ERP.NUMBER' }, { key: 'name', title: 'COMMON.NAME' }, { key: 'company', title: 'ERP.COMPANY' }, { key: 'source', title: 'ERP.SOURCE' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }] },
      { fileName: 'activities', route: 'activities', titleKey: 'MENU.CRM_ACTIVITIES', apiMethod: 'getCrmActivities', showDateRange: true, showStatus: false,
        columns: [{ key: 'activityType', title: 'ERP.TYPE' }, { key: 'subject', title: 'ERP.SUBJECT' }, { key: 'activityDate', title: 'ERP.DATE', kind: 'date' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }] }
    ]
  },
  {
    name: 'projects',
    moduleClass: 'ProjectsModule',
    pages: [
      { fileName: 'projects', route: '', titleKey: 'MENU.PROJECT_LIST', apiMethod: 'getProjects', showDateRange: false, showStatus: false,
        columns: [{ key: 'projectCode', title: 'ERP.CODE' }, { key: 'nameEn', title: 'COMMON.NAME' }, { key: 'startDate', title: 'ERP.START_DATE', kind: 'date' }, { key: 'endDate', title: 'ERP.END_DATE', kind: 'date' }, { key: 'budget', title: 'ERP.BUDGET', align: 'end' }, { key: 'status', title: 'COMMON.STATUS', kind: 'status' }],
        rowMap: amountFmt }
    ]
  }
];

const reportPages = [
  {
    fileName: 'sales-report', route: 'sales', titleKey: 'MENU.SALES_REPORT', apiMethod: 'getSalesReport', dateFilter: true,
    summaryFields: [
      { key: 'fromDate', label: 'ERP.REPORT_FROM' },
      { key: 'toDate', label: 'ERP.REPORT_TO' },
      { key: 'invoiceCount', label: 'ERP.INVOICE_COUNT' },
      { key: 'totalSales', label: 'ERP.TOTAL_SALES' }
    ],
    columns: [{ key: 'number', title: 'ERP.NUMBER' }, { key: 'date', title: 'ERP.DATE', kind: 'date' }, { key: 'customer', title: 'MENU.CUSTOMERS' }, { key: 'total', title: 'ERP.TOTAL', align: 'end' }],
    rowsMap: `(data?.invoices || []).map((row: Record<string, unknown>) => ({
            ...row,
            total: Number(row.total || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
          }))`
  },
  {
    fileName: 'purchases-report', route: 'purchases', titleKey: 'MENU.PURCHASE_REPORT', apiMethod: 'getPurchasesReport', dateFilter: true,
    summaryFields: [
      { key: 'fromDate', label: 'ERP.REPORT_FROM' },
      { key: 'toDate', label: 'ERP.REPORT_TO' },
      { key: 'invoiceCount', label: 'ERP.INVOICE_COUNT' },
      { key: 'totalPurchases', label: 'ERP.TOTAL_PURCHASES' }
    ],
    columns: [{ key: 'number', title: 'ERP.NUMBER' }, { key: 'date', title: 'ERP.DATE', kind: 'date' }, { key: 'supplier', title: 'MENU.SUPPLIERS' }, { key: 'total', title: 'ERP.TOTAL', align: 'end' }],
    rowsMap: `(data?.invoices || []).map((row: Record<string, unknown>) => ({
            ...row,
            total: Number(row.total || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
          }))`
  },
  {
    fileName: 'inventory-report', route: 'inventory', titleKey: 'MENU.INVENTORY_REPORT', apiMethod: 'getInventoryReport', dateFilter: false,
    summaryFields: [
      { key: 'totalSkus', label: 'ERP.TOTAL_SKUS' },
      { key: 'lowStockCount', label: 'ERP.LOW_STOCK_COUNT' },
      { key: 'totalQuantity', label: 'ERP.TOTAL_QUANTITY' }
    ],
    columns: [{ key: 'productCode', title: 'ERP.PRODUCT' }, { key: 'productName', title: 'COMMON.NAME' }, { key: 'warehouseName', title: 'MENU.WAREHOUSES' }, { key: 'quantity', title: 'ERP.QUANTITY', align: 'end' }, { key: 'availableQuantity', title: 'ERP.AVAILABLE', align: 'end' }],
    rowsMap: `(data?.stockLevels || []).map((row: Record<string, unknown>) => ({
            ...row,
            quantity: Number(row.quantity || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
            availableQuantity: Number(row.availableQuantity || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
          }))`
  },
  {
    fileName: 'profit-report', route: 'profit', titleKey: 'MENU.PROFIT_REPORT', apiMethod: 'getProfitReport', dateFilter: true,
    summaryFields: [
      { key: 'fromDate', label: 'ERP.REPORT_FROM' },
      { key: 'toDate', label: 'ERP.REPORT_TO' },
      { key: 'totalSales', label: 'ERP.TOTAL_SALES' },
      { key: 'totalPurchases', label: 'ERP.TOTAL_PURCHASES' },
      { key: 'netProfit', label: 'ERP.NET_PROFIT' }
    ],
    columns: [],
    rowsMap: '[]'
  }
];

const created = [];

for (const mod of modules) {
  const moduleDir = path.join(appRoot, mod.name);
  const components = mod.pages.map((p) => generateListPage(moduleDir, p));

  const routes = mod.pages.map((p) => `  { path: '${p.route}', component: ${pascalCase(p.fileName)}PageComponent }`).join(',\n');
  const routingTs = `import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
${mod.pages.map((p) => `import { ${pascalCase(p.fileName)}PageComponent } from './${p.fileName}-page.component';`).join('\n')}

const routes: Routes = [
${routes}
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class ${pascalCase(mod.name)}RoutingModule {}
`;

  const moduleTs = `import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ${pascalCase(mod.name)}RoutingModule } from './${mod.name}-routing.module';
${components.map((c, i) => `import { ${c} } from './${mod.pages[i].fileName}-page.component';`).join('\n')}

@NgModule({
  declarations: [${components.join(', ')}],
  imports: [SharedModule, ${pascalCase(mod.name)}RoutingModule]
})
export class ${mod.moduleClass} {}
`;

  writeFile(path.join(moduleDir, `${mod.name}-routing.module.ts`), routingTs);
  writeFile(path.join(moduleDir, `${mod.name}.module.ts`), moduleTs);
  created.push(path.join(moduleDir, `${mod.name}.module.ts`));
}

const erpReportsDir = path.join(appRoot, 'erp-reports');
const reportComponents = reportPages.map((p) => generateReportPage(erpReportsDir, p));
const reportRoutes = reportPages.map((p) => `  { path: '${p.route}', component: ${pascalCase(p.fileName)}PageComponent }`).join(',\n');

writeFile(path.join(erpReportsDir, 'erp-reports-routing.module.ts'), `import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
${reportPages.map((p) => `import { ${pascalCase(p.fileName)}PageComponent } from './${p.fileName}-page.component';`).join('\n')}

const routes: Routes = [
${reportRoutes}
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class ErpReportsRoutingModule {}
`);

writeFile(path.join(erpReportsDir, 'erp-reports.module.ts'), `import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ErpReportsRoutingModule } from './erp-reports-routing.module';
${reportComponents.map((c, i) => `import { ${c} } from './${reportPages[i].fileName}-page.component';`).join('\n')}

@NgModule({
  declarations: [${reportComponents.join(', ')}],
  imports: [SharedModule, ErpReportsRoutingModule]
})
export class ErpReportsModule {}
`);

console.log('Generated ERP modules successfully');
console.log(created.join('\n'));
