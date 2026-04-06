import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { finalize, map, takeUntil } from 'rxjs/operators';
import { DashboardSummary } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';

@Component({ standalone: false,
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardPageComponent implements OnInit {
  loading = false;
  errorKey = '';
  
  private readonly summarySubject = new BehaviorSubject<DashboardSummary | null>(null);
  public readonly summary$: Observable<DashboardSummary | null> = this.summarySubject.asObservable();
  private readonly destroy$ = new Subject<void>();

  public readonly monthlySummaryRows$: Observable<Array<Record<string, unknown>>> = this.summary$.pipe(
    map((summary) => {
      if (!summary) return [];
      const monthly = this.mapMonthlySummaries(summary);
      return monthly.map((month) => ({
        month: month.label,
        debit: this.formatAmount(month.debit),
        credit: this.formatAmount(month.credit),
        cashFlow: this.formatAmount(month.cashFlow)
      }));
    })
  );

  public readonly bankBalanceRows$: Observable<Array<Record<string, unknown>>> = this.summary$.pipe(
    map((summary) => {
      if (!summary) return [];
      return (summary.bankBalances || []).map((item) => ({
        bankName: item.bankName,
        accountNumber: item.accountNumber,
        balance: this.formatAmount(item.balance),
        currency: item.currency
      }));
    })
  );

  readonly filterFromCtrl = new FormControl('', { nonNullable: true });
  readonly filterToCtrl = new FormControl('', { nonNullable: true });

  revenueExpenseChartId = 'revenue-expense-chart';
  cashFlowChartId = 'cash-flow-chart';
  balanceSheetChartId = 'balance-sheet-chart';
  netProfitTrendChartId = 'net-profit-trend-chart';
  receivablesPayablesChartId = 'receivables-payables-chart';
  budgetChartId = 'budget-performance-chart';
  weeklyActivityChartId = 'weekly-activity-chart';

  revenueExpenseChartConfig: Record<string, unknown> = {};
  cashFlowChartConfig: Record<string, unknown> = {};
  balanceSheetChartConfig: Record<string, unknown> = {};
  netProfitTrendChartConfig: Record<string, unknown> = {};
  receivablesPayablesChartConfig: Record<string, unknown> = {};
  budgetChartConfig: Record<string, unknown> = {};
  weeklyActivityChartConfig: Record<string, unknown> = {};

  monthlySummaryColumns = [
    { key: 'month', title: 'DASHBOARD.MONTH', className: 'erp-table__col--compact' },
    { key: 'debit', title: 'DASHBOARD.DEBIT', className: 'erp-table__col--compact' },
    { key: 'credit', title: 'DASHBOARD.CREDIT', className: 'erp-table__col--compact' },
    { key: 'cashFlow', title: 'DASHBOARD.CASH_FLOW', className: 'erp-table__col--compact' }
  ];
  bankBalanceColumns = [
    { key: 'bankName', title: 'DASHBOARD.BANK' },
    { key: 'accountNumber', title: 'DASHBOARD.ACCOUNT_NUMBER' },
    { key: 'balance', title: 'DASHBOARD.BALANCE' },
    { key: 'currency', title: 'DASHBOARD.CURRENCY' }
  ];

  constructor(
    private api: AccountingApiService,
    private translationService: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchDashboard();
    this.translationService.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      const summary = this.summarySubject.value;
      if (summary) {
        this.buildCharts(summary);
        this.cdr.markForCheck();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  applyFilter(): void {
    this.fetchDashboard();
  }

  resetFilter(): void {
    this.filterFromCtrl.setValue('');
    this.filterToCtrl.setValue('');
    this.fetchDashboard();
  }

  fetchDashboard(): void {
    this.loading = true;
    this.errorKey = '';

    const filters: { fromDate?: string; toDate?: string } = {};
    const fromDate = (this.filterFromCtrl.value || '').trim();
    const toDate = (this.filterToCtrl.value || '').trim();
    if (fromDate) {
      filters.fromDate = fromDate;
    }
    if (toDate) {
      filters.toDate = toDate;
    }

    this.api
      .getDashboardSummary(filters)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (summary) => {
          if (summary == null) {
            this.summarySubject.next(null);
            this.errorKey = 'COMMON.ERROR_LOADING';
            return;
          }
          this.summarySubject.next(summary);
          this.buildCharts(summary);
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
        }
      });
  }

  toMonthlySummaryRows(): Array<Record<string, unknown>> {
    return []; // Obsolete: used monthlySummaryRows$ instead
  }

  toBankBalanceRows(): Array<Record<string, unknown>> {
    return []; // Obsolete: used bankBalanceRows$ instead
  }

  recentActivityConfig = [
    { titleKey: 'DASHBOARD.LATEST_JOURNAL_VOUCHERS', kind: 'journals' as const, viewAllLink: ['/journal-entries'] },
    { titleKey: 'DASHBOARD.LATEST_PAYMENTS', kind: 'payments' as const, viewAllLink: ['/vouchers', 'payment'] },
    { titleKey: 'DASHBOARD.LATEST_RECEIPTS', kind: 'receipts' as const, viewAllLink: ['/vouchers', 'receipt'] }
  ];

  formatAmount(value: number): string {
    return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  metricTrendClass(value: number): string {
    if (value > 0) { return 'text-success'; }
    if (value < 0) { return 'text-danger'; }
    return 'text-muted';
  }

  get hasBudgetData(): boolean {
    return !!(this.summarySubject.value?.budgetSummaries?.length);
  }

  private mapMonthlySummaries(summary: DashboardSummary): Array<{ label: string; debit: number; credit: number; cashFlow: number }> {
    const monthLabels = this.getRollingMonthLabels(summary.rollingMonthDebitSeries.length);
    return monthLabels.map((label, index) => {
      const debit = summary.rollingMonthDebitSeries[index] || 0;
      const credit = summary.rollingMonthCreditSeries[index] || 0;
      return { label, debit, credit, cashFlow: credit - debit };
    });
  }

  private buildCharts(summary: DashboardSummary): void {
    const labels = this.getRollingMonthLabels(summary.rollingMonthDebitSeries.length);
    const t = (key: string) => this.translationService.instant(key);

    this.buildRevenueExpenseChart(summary, labels, t);
    this.buildCashFlowChart(labels, t);
    this.buildBalanceSheetChart(summary, t);
    this.buildNetProfitTrendChart(summary, labels, t);
    this.buildReceivablesPayablesChart(summary, t);
    this.buildWeeklyActivityChart(summary, t);
    if (summary.budgetSummaries?.length) {
      this.buildBudgetChart(summary, t);
    }
  }

  private buildRevenueExpenseChart(summary: DashboardSummary, labels: string[], t: (k: string) => string): void {
    this.revenueExpenseChartConfig = {
      chart: { type: 'area', height: 300, toolbar: { show: false }, fontFamily: 'inherit' },
      stroke: { width: 2.5, curve: 'smooth' },
      fill: {
        type: 'gradient',
        gradient: { shadeIntensity: 1, opacityFrom: 0.4, opacityTo: 0.05, stops: [0, 90, 100] }
      },
      series: [
        { name: t('DASHBOARD.TOTAL_REVENUE'), data: summary.rollingMonthRevenueSeries || [] },
        { name: t('DASHBOARD.TOTAL_EXPENSES'), data: summary.rollingMonthExpenseSeries || [] }
      ],
      xaxis: { categories: labels, labels: { style: { fontSize: '11px' } } },
      yaxis: { labels: { formatter: (val: number) => this.compactNumber(val), style: { fontSize: '11px' } } },
      legend: { position: 'top', fontSize: '12px' },
      colors: ['#10b981', '#ef4444'],
      dataLabels: { enabled: false },
      grid: { borderColor: 'var(--erp-border)', strokeDashArray: 4 },
      tooltip: { y: { formatter: (val: number) => this.formatAmount(val) } }
    };
  }

  private buildCashFlowChart(labels: string[], t: (k: string) => string): void {
    const summary = this.summarySubject.value;
    if (!summary) return;
    const monthly = this.mapMonthlySummaries(summary);
    const data = monthly.map((item) => item.cashFlow);
    const positive = data.map(v => v >= 0 ? v : 0);
    const negative = data.map(v => v < 0 ? v : 0);

    this.cashFlowChartConfig = {
      chart: { type: 'bar', height: 300, toolbar: { show: false }, stacked: true, fontFamily: 'inherit' },
      plotOptions: { bar: { borderRadius: 4, columnWidth: '50%' } },
      series: [
        { name: t('DASHBOARD.CASH_FLOW') + ' (+)', data: positive },
        { name: t('DASHBOARD.CASH_FLOW') + ' (-)', data: negative }
      ],
      xaxis: { categories: labels, labels: { style: { fontSize: '11px' } } },
      yaxis: { labels: { formatter: (val: number) => this.compactNumber(val), style: { fontSize: '11px' } } },
      colors: ['#10b981', '#ef4444'],
      dataLabels: { enabled: false },
      grid: { borderColor: 'var(--erp-border)', strokeDashArray: 4 },
      tooltip: { y: { formatter: (val: number) => this.formatAmount(val) } },
      legend: { position: 'top', fontSize: '12px' }
    };
  }

  private buildBalanceSheetChart(summary: DashboardSummary, t: (k: string) => string): void {
    const assets = Math.abs(summary.totalAssets || 0);
    const liabilities = Math.abs(summary.totalLiabilities || 0);
    const equity = Math.abs(summary.totalEquity || 0);

    this.balanceSheetChartConfig = {
      chart: { type: 'donut', height: 310, fontFamily: 'inherit' },
      series: [assets, liabilities, equity],
      labels: [t('DASHBOARD.ASSETS'), t('DASHBOARD.LIABILITIES'), t('DASHBOARD.EQUITY')],
      colors: ['#3b82f6', '#f59e0b', '#8b5cf6'],
      plotOptions: {
        pie: {
          donut: {
            size: '68%',
            labels: {
              show: true,
              total: {
                show: true,
                showAlways: true,
                fontSize: '14px',
                fontWeight: 700,
                formatter: () => this.formatAmount(assets + liabilities + equity)
              }
            }
          }
        }
      },
      legend: { position: 'bottom', fontSize: '12px' },
      dataLabels: { enabled: false },
      tooltip: { y: { formatter: (val: number) => this.formatAmount(val) } },
      stroke: { width: 2, colors: ['var(--erp-bg-card, #fff)'] }
    };
  }

  private buildNetProfitTrendChart(summary: DashboardSummary, labels: string[], t: (k: string) => string): void {
    this.netProfitTrendChartConfig = {
      chart: { type: 'area', height: 310, toolbar: { show: false }, fontFamily: 'inherit', sparkline: { enabled: false } },
      stroke: { width: 3, curve: 'smooth' },
      fill: {
        type: 'gradient',
        gradient: { shadeIntensity: 1, opacityFrom: 0.45, opacityTo: 0.05, stops: [0, 100] }
      },
      series: [{ name: t('DASHBOARD.NET_PROFIT'), data: summary.rollingMonthNetProfitSeries || [] }],
      xaxis: { categories: labels, labels: { style: { fontSize: '11px' } } },
      yaxis: { labels: { formatter: (val: number) => this.compactNumber(val), style: { fontSize: '11px' } } },
      colors: ['#6366f1'],
      dataLabels: { enabled: false },
      grid: { borderColor: 'var(--erp-border)', strokeDashArray: 4 },
      tooltip: { y: { formatter: (val: number) => this.formatAmount(val) } },
      markers: { size: 4, strokeWidth: 2, hover: { size: 6 } }
    };
  }

  private buildReceivablesPayablesChart(summary: DashboardSummary, t: (k: string) => string): void {
    const receivables = Math.abs(summary.receivablesSummary || 0);
    const payablesOut = Math.abs(summary.payablesOutstanding || 0);
    const payablesPaid = Math.abs(summary.payablesPaid || 0);
    const totalPayables = payablesOut + payablesPaid;
    const collectionPct = totalPayables > 0 ? Math.round((payablesPaid / totalPayables) * 100) : 0;

    this.receivablesPayablesChartConfig = {
      chart: { type: 'radialBar', height: 310, fontFamily: 'inherit' },
      series: [
        receivables > 0 ? Math.min(100, Math.round((receivables / Math.max(receivables, totalPayables)) * 100)) : 0,
        totalPayables > 0 ? Math.min(100, Math.round((totalPayables / Math.max(receivables, totalPayables)) * 100)) : 0,
        collectionPct
      ],
      labels: [t('DASHBOARD.RECEIVABLES'), t('DASHBOARD.PAYABLES_OUTSTANDING'), t('DASHBOARD.COLLECTION_RATE')],
      colors: ['#10b981', '#ef4444', '#3b82f6'],
      plotOptions: {
        radialBar: {
          hollow: { size: '35%' },
          track: { background: 'var(--erp-bg-muted, #f3f4f6)', strokeWidth: '100%' },
          dataLabels: {
            name: { fontSize: '12px' },
            value: { fontSize: '16px', fontWeight: 700, formatter: (val: number) => val + '%' },
            total: {
              show: true,
              label: t('DASHBOARD.COLLECTION_RATE'),
              formatter: () => collectionPct + '%'
            }
          }
        }
      },
      legend: { show: false }
    };
  }

  private buildBudgetChart(summary: DashboardSummary, t: (k: string) => string): void {
    const budgets = summary.budgetSummaries || [];
    const categories = budgets.map(b => b.label);
    const planned = budgets.map(b => b.planned || 0);
    const actual = budgets.map(b => b.actual || 0);

    this.budgetChartConfig = {
      chart: { type: 'bar', height: 300, toolbar: { show: false }, fontFamily: 'inherit' },
      plotOptions: { bar: { horizontal: true, borderRadius: 4, barHeight: '60%' } },
      series: [
        { name: t('DASHBOARD.PLANNED'), data: planned },
        { name: t('DASHBOARD.ACTUAL'), data: actual }
      ],
      xaxis: { categories, labels: { formatter: (val: number) => this.compactNumber(val), style: { fontSize: '11px' } } },
      yaxis: { labels: { style: { fontSize: '11px' } } },
      colors: ['#94a3b8', '#6366f1'],
      dataLabels: { enabled: false },
      grid: { borderColor: 'var(--erp-border)', strokeDashArray: 4 },
      tooltip: { y: { formatter: (val: number) => this.formatAmount(val) } },
      legend: { position: 'top', fontSize: '12px' }
    };
  }

  private buildWeeklyActivityChart(summary: DashboardSummary, t: (k: string) => string): void {
    const weekDays = this.getWeekDayLabels();
    this.weeklyActivityChartConfig = {
      chart: { type: 'bar', height: 260, toolbar: { show: false }, fontFamily: 'inherit' },
      plotOptions: { bar: { borderRadius: 6, columnWidth: '55%' } },
      series: [
        { name: t('DASHBOARD.DEBIT'), data: summary.weekDebitSeries || [] },
        { name: t('DASHBOARD.CREDIT'), data: summary.weekCreditSeries || [] }
      ],
      xaxis: { categories: weekDays, labels: { style: { fontSize: '11px' } } },
      yaxis: { labels: { formatter: (val: number) => this.compactNumber(val), style: { fontSize: '11px' } } },
      colors: ['#f59e0b', '#10b981'],
      dataLabels: { enabled: false },
      grid: { borderColor: 'var(--erp-border)', strokeDashArray: 4 },
      tooltip: { y: { formatter: (val: number) => this.formatAmount(val) } },
      legend: { position: 'top', fontSize: '12px' }
    };
  }

  private compactNumber(val: number): string {
    const abs = Math.abs(val);
    if (abs >= 1_000_000) { return (val / 1_000_000).toFixed(1) + 'M'; }
    if (abs >= 1_000) { return (val / 1_000).toFixed(1) + 'K'; }
    return val.toFixed(0);
  }

  private getRollingMonthLabels(total: number): string[] {
    const labels: string[] = [];
    const now = new Date();
    for (let i = total - 1; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
      labels.push(date.toLocaleDateString(undefined, { month: 'short', year: '2-digit' }));
    }
    return labels;
  }

  private getWeekDayLabels(): string[] {
    const labels: string[] = [];
    const now = new Date();
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth(), now.getDate() - i);
      labels.push(date.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric' }));
    }
    return labels;
  }
}
