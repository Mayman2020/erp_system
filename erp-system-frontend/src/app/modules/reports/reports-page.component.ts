import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import * as XLSX from 'xlsx';
import { BalanceSheetReportDto, ProfitLossReportDto } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { LookupItem } from '../../core/models/lookup.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({ standalone: false,
  selector: 'app-reports-page',
  templateUrl: './reports-page.component.html',
  styleUrls: ['./reports-page.component.scss']
})
export class ReportsPageComponent implements OnInit {
  readonly titleKey = 'REPORTS.TITLE';
  loading = false;
  activeReport: 'profitLoss' | 'balanceSheet' = 'profitLoss';
  periodOptions: LookupItem[] = [];
  profitLoss: ProfitLossReportDto | null = null;
  balanceSheet: BalanceSheetReportDto | null = null;
  errorKey = '';

  readonly form = this.fb.group({
    fromDate: ['', Validators.required],
    toDate: ['', Validators.required],
    asOfDate: ['', Validators.required],
    period: ['CUSTOM']
  });

  reportCards = [
    { id: 'profitLoss', title: 'REPORTS.PROFIT_LOSS.TITLE', description: 'REPORTS.PROFIT_LOSS.DESCRIPTION' },
    { id: 'balanceSheet', title: 'REPORTS.BALANCE_SHEET.TITLE', description: 'REPORTS.BALANCE_SHEET.DESCRIPTION' }
  ];

  constructor(
    private api: AccountingApiService,
    private lookupService: LookupService,
    private fb: FormBuilder,
    private translationService: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const today = new Date().toISOString().slice(0, 10);
    const monthStart = `${today.slice(0, 8)}01`;
    this.form.patchValue({ fromDate: monthStart, toDate: today, asOfDate: today });
    this.lookupService.getLookup('report-periods').subscribe((items) => (this.periodOptions = items), () => undefined);
    this.form.controls.period.valueChanges.subscribe((value) => this.applyPeriodPreset(String(value || 'CUSTOM')));
    this.runReport('profitLoss');
  }

  runReport(report: 'profitLoss' | 'balanceSheet'): void {
    this.activeReport = report;
    if (!this.isRangeValid()) {
      this.errorKey = 'REPORTS.INVALID_RANGE';
      return;
    }

    this.activeReport = report;
    this.loading = true;
    this.errorKey = '';
    const raw = this.form.getRawValue();
    if (report === 'profitLoss') {
      this.api
        .getProfitLoss(String(raw.fromDate || ''), String(raw.toDate || ''))
        .pipe(
          finalize(() => {
            this.loading = false;
            this.cdr.detectChanges();
          })
        )
        .subscribe({
          next: (result) => (this.profitLoss = result),
          error: () => (this.errorKey = 'COMMON.ERROR_LOADING')
        });
      return;
    }

    this.api
      .getBalanceSheet(String(raw.asOfDate || ''))
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (result) => (this.balanceSheet = result),
        error: () => (this.errorKey = 'COMMON.ERROR_LOADING')
      });
  }

  runReportCard(reportId: string): void {
    if (reportId === 'balanceSheet') {
      this.runReport('balanceSheet');
      return;
    }
    this.runReport('profitLoss');
  }

  displayAccountName(line: { accountNameAr?: string; accountNameEn: string }): string {
    if (this.translationService.currentLanguage === 'ar') {
      return line.accountNameAr || line.accountNameEn;
    }
    return line.accountNameEn || line.accountNameAr || '';
  }

  /** Account column: no lone " - " when code or name is missing. */
  formatReportAccountCell(line: { accountCode?: string; accountNameAr?: string; accountNameEn: string }): string {
    const code = String(line.accountCode ?? '').trim();
    const name = String(this.displayAccountName(line) ?? '').trim();
    if (code && name) {
      return `${code} - ${name}`;
    }
    return code || name || '';
  }

  exportProfitLoss(): void {
    if (!this.profitLoss) return;
    const headers = [
      this.translationService.instant('ACCOUNTS.CODE') + ' - ' + this.translationService.instant('COMMON.NAME'),
      this.translationService.instant('VOUCHERS.FORM.AMOUNT')
    ];
    const revenueRows = this.profitLoss.revenues.map((l) => [this.formatReportAccountCell(l), l.amount]);
    const expenseRows = this.profitLoss.expenses.map((l) => [this.formatReportAccountCell(l), l.amount]);
    const data: any[][] = [
      headers,
      [this.translationService.instant('REPORTS.PROFIT_LOSS.REVENUE_SECTION'), ''],
      ...revenueRows,
      [this.translationService.instant('REPORTS.PROFIT_LOSS.TOTAL_REVENUE'), this.profitLoss.totalRevenue],
      ['', ''],
      [this.translationService.instant('REPORTS.PROFIT_LOSS.EXPENSE_SECTION'), ''],
      ...expenseRows,
      [this.translationService.instant('REPORTS.PROFIT_LOSS.TOTAL_EXPENSES'), this.profitLoss.totalExpenses],
      ['', ''],
      [this.translationService.instant('REPORTS.PROFIT_LOSS.NET_RESULT'), this.profitLoss.netProfit]
    ];
    const ws = XLSX.utils.aoa_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Profit & Loss');
    XLSX.writeFile(wb, 'profit-loss.xlsx');
  }

  exportBalanceSheet(): void {
    if (!this.balanceSheet) return;
    const headers = [
      this.translationService.instant('ACCOUNTS.CODE') + ' - ' + this.translationService.instant('COMMON.NAME'),
      this.translationService.instant('DASHBOARD.BALANCE')
    ];
    const assetRows = this.balanceSheet.assets.map((l) => [this.formatReportAccountCell(l), l.balance]);
    const liabilityRows = this.balanceSheet.liabilities.map((l) => [this.formatReportAccountCell(l), l.balance]);
    const equityRows = this.balanceSheet.equity.map((l) => [this.formatReportAccountCell(l), l.balance]);
    const data: any[][] = [
      headers,
      [this.translationService.instant('REPORTS.BALANCE_SHEET.ASSETS'), ''],
      ...assetRows,
      [this.translationService.instant('REPORTS.BALANCE_SHEET.TOTAL_ASSETS'), this.balanceSheet.totalAssets],
      ['', ''],
      [this.translationService.instant('REPORTS.BALANCE_SHEET.LIABILITIES'), ''],
      ...liabilityRows,
      [this.translationService.instant('REPORTS.BALANCE_SHEET.TOTAL_LIABILITIES'), this.balanceSheet.totalLiabilities],
      ['', ''],
      [this.translationService.instant('REPORTS.BALANCE_SHEET.EQUITY'), ''],
      ...equityRows,
      [this.translationService.instant('REPORTS.BALANCE_SHEET.TOTAL_EQUITY'), this.balanceSheet.totalEquity]
    ];
    const ws = XLSX.utils.aoa_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Balance Sheet');
    XLSX.writeFile(wb, 'balance-sheet.xlsx');
  }

  private isRangeValid(): boolean {
    if (this.activeReport === 'balanceSheet') {
      return !!this.form.controls.asOfDate.value;
    }
    const fromDate = String(this.form.controls.fromDate.value || '');
    const toDate = String(this.form.controls.toDate.value || '');
    return !!fromDate && !!toDate && fromDate <= toDate;
  }

  private applyPeriodPreset(periodCode: string): void {
    const today = new Date();
    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth();
    let fromDate = this.form.controls.fromDate.value || '';
    let toDate = this.form.controls.toDate.value || '';

    switch (periodCode) {
      case 'THIS_MONTH':
        fromDate = this.toIsoDate(new Date(currentYear, currentMonth, 1));
        toDate = this.toIsoDate(today);
        break;
      case 'LAST_MONTH': {
        const start = new Date(currentYear, currentMonth - 1, 1);
        const end = new Date(currentYear, currentMonth, 0);
        fromDate = this.toIsoDate(start);
        toDate = this.toIsoDate(end);
        break;
      }
      case 'THIS_QUARTER': {
        const quarterStartMonth = Math.floor(currentMonth / 3) * 3;
        fromDate = this.toIsoDate(new Date(currentYear, quarterStartMonth, 1));
        toDate = this.toIsoDate(today);
        break;
      }
      case 'THIS_YEAR':
        fromDate = this.toIsoDate(new Date(currentYear, 0, 1));
        toDate = this.toIsoDate(today);
        break;
      default:
        return;
    }

    this.form.patchValue({ fromDate, toDate, asOfDate: toDate }, { emitEvent: false });
  }

  private toIsoDate(value: Date): string {
    return value.toISOString().slice(0, 10);
  }
}
