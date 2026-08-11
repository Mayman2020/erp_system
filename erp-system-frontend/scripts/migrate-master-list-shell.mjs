#!/usr/bin/env node
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '../src/app/modules');
const pages = [];

function walk(dir) {
  for (const ent of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, ent.name);
    if (ent.isDirectory()) walk(p);
    else if (ent.name.endsWith('-page.component.ts')) {
      const ts = fs.readFileSync(p, 'utf8');
      if (ts.includes('extends ErpMasterPageBase')) pages.push(p);
    }
  }
}
walk(root);

const actionsBlock = () => `  <app-page-header [titleKey]="titleKey">
    <app-erp-master-page-actions
      header-actions
      [canCreate]="canCreate"
      [canExport]="canExport"
      [createKey]="config.createKey"
      [titleKey]="titleKey"
      [exportFileName]="exportFileName"
      [exportColumns]="exportColumns"
      [rows]="rows"
      [menuItemId]="config.menuItemId || ''"
      [disabled]="listLoad.refreshing"
      (create)="openCreate()">
    </app-erp-master-page-actions>
  </app-page-header>`;

const shellBlock = (extraAttrs = '', projected = '') => `  <app-erp-master-list-shell
    [listLoad]="listLoad"
    [showSearch]="showSearch"
    [showDateRange]="showDateRange"
    [showStatus]="showStatus"
    [statusOptions]="statusOptions"
    [errorKey]="errorKey"
    [successKey]="successKey"
    [formVisible]="formVisible"
    [columns]="columns"
    [pagedRows]="pagedRows"
    [actions]="actions"
    [totalElements]="totalElements"
    [pageIndex]="pageIndex"
    [pageSize]="pageSize"${extraAttrs}
    (search)="onSearch($event)"
    (pageIndexChange)="onPageChange($event)"
    (actionClick)="onTableAction($event)"
    (dismissError)="errorKey = ''"
    (dismissSuccess)="successKey = ''">
${projected}  </app-erp-master-list-shell>`;

let updated = 0;
for (const tsPath of pages) {
  const htmlPath = tsPath.replace(/\.ts$/, '.html');
  if (!fs.existsSync(htmlPath)) {
    console.log('MISSING', htmlPath);
    continue;
  }
  let html = fs.readFileSync(htmlPath, 'utf8');
  const rel = path.relative(root, htmlPath).replace(/\\/g, '/');

  if (html.includes('app-erp-master-list-shell')) {
    console.log('SKIP', rel);
    continue;
  }

  if (rel === 'hr/employees-page.component.html') {
    const stats = `    <section list-stats class="list-stats" *ngIf="rows.length || !listLoad.refreshing">
      <article class="stat-pill stat-pill--purple">
        <span class="stat-pill__icon"><mat-icon>groups</mat-icon></span>
        <span class="stat-label">{{ 'COMMON.ALL' | translate }}</span>
        <strong [erpCountUp]="totalElements" [erpCountUpDecimals]="0"></strong>
      </article>
      <article class="stat-pill stat-pill--green">
        <span class="stat-pill__icon"><mat-icon>person_check</mat-icon></span>
        <span class="stat-label">{{ 'STATUS.ACTIVE' | translate }}</span>
        <strong [erpCountUp]="activeCount" [erpCountUpDecimals]="0"></strong>
      </article>
      <article class="stat-pill stat-pill--amber">
        <span class="stat-pill__icon"><mat-icon>person_off</mat-icon></span>
        <span class="stat-label">{{ 'STATUS.INACTIVE' | translate }}</span>
        <strong [erpCountUp]="inactiveCount" [erpCountUpDecimals]="0"></strong>
      </article>
    </section>
`;
    const dialogIdx = html.indexOf('<app-erp-dialog');
    const tail = html.slice(dialogIdx);
    const newHead = `<section class="erp-page erp-list-page erp-estate-fade-up">
${actionsBlock()}

${shellBlock('', stats)}
</section>

`;
    fs.writeFileSync(htmlPath, newHead + tail);
    updated++;
    console.log('OK', rel);
    continue;
  }

  if (rel === 'hr/documents-page.component.html') {
    const filters = `    <div list-filters class="row mb-3">
      <div class="col-md-4">
        <label class="form-label">{{ 'MENU.EMPLOYEES' | translate }}</label>
        <select class="form-control" [ngModel]="filterEmployeeId" (ngModelChange)="onEmployeeFilterChange($event)">
          <option [ngValue]="null">{{ 'COMMON.ALL' | translate }}</option>
          <option *ngFor="let e of employeeOptions.slice(1)" [ngValue]="e.id">{{ e.label }}</option>
        </select>
      </div>
    </div>
`;
    const dialogIdx = html.indexOf('<app-erp-dialog');
    const tail = html.slice(dialogIdx);
    const newHead = `<section class="erp-page erp-list-page erp-estate-fade-up">
${actionsBlock()}

${shellBlock('\n    [hideSearchBar]="true"', filters)}
</section>

`;
    fs.writeFileSync(htmlPath, newHead + tail);
    updated++;
    console.log('OK', rel);
    continue;
  }

  if (rel === 'banks/banks-page.component.html') {
    const dialogIdx = html.indexOf('<app-erp-dialog');
    const tail = html.slice(dialogIdx);
    const newHead = `<section class="erp-page erp-list-page erp-estate-fade-up">
${actionsBlock()}

${shellBlock('\n    statusLabelPrefix="COMMON.BOOL."')}
</section>
`;
    fs.writeFileSync(htmlPath, newHead + tail);
    updated++;
    console.log('OK', rel);
    continue;
  }

  const sectionMatch = html.match(/^[\s\S]*?<\/section>/);
  if (!sectionMatch) {
    console.log('NO SECTION', rel);
    continue;
  }
  const rest = html.slice(sectionMatch[0].length);
  const newSection = `<section class="erp-page erp-list-page erp-estate-fade-up">
${actionsBlock()}

${shellBlock()}
</section>`;
  fs.writeFileSync(htmlPath, newSection + rest);
  updated++;
  console.log('OK', rel);
}

console.log('Updated', updated, 'of', pages.length);
