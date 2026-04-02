import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { DashboardSummary, RecentDocument } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';

@Component({ standalone: false,
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss']
})
export class DashboardPageComponent implements OnInit {
  loading = false;
  errorKey = '';
  summary: DashboardSummary | null = null;
  monthlySummaries: Array<{ label: string; debit: number; credit: number; cashFlow: number }> = [];

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

  documentColumns = [
    { key: 'reference', title: 'DASHBOARD.DOC_REFERENCE', className: 'erp-table__col--compact' },
    { key: 'date', title: 'DASHBOARD.DOC_DATE', kind: 'date' as 'date', className: 'erp-table__col--compact' },
    { key: 'amount', title: 'DASHBOARD.DOC_AMOUNT', className: 'erp-table__col--compact' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' as 'status' }
  ];
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
    this.translationService.currentLanguage$.subscribe(() => {
      if (this.summary) {
        this.buildCharts(this.summary);
      }
    });
  }

  fetchDashboard(): void {
    this.loading = true;
    this.errorKey = '';

    this.api
      .getDashboardSummary()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (summary) => {
          if (summary == null) {
            this.summary = null;
            this.errorKey = 'COMMON.ERROR_LOADING';
            return;
          }
          this.summary = summary;
          this.monthlySummaries = this.mapMonthlySummaries(summary);
          this.buildCharts(summary);
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
        }
      });
  }

  toDocumentRows(items: RecentDocument[]): Array<Record<string, unknown>> {
    return (items || []).map((item) => ({
      reference: item.reference,
      date: item.date,
      amount: this.formatAmount(item.amount),
      status: item.status
    }));
  }

  toMonthlySummaryRows(): Array<Record<string, unknown>> {
    return (this.monthlySummaries || []).map((month) => ({
      month: month.label,
      debit: this.formatAmount(month.debit),
      credit: this.formatAmount(month.credit),
      cashFlow: this.formatAmount(month.cashFlow)
    }));
  }

  toBankBalanceRows(): Array<Record<string, unknown>> {
    return (this.summary?.bankBalances || []).map((item) => ({
      bankName: item.bankName,
      accountNumber: item.accountNumber,
      balance: this.formatAmount(item.balance),
      currency: item.currency
    }));
  }

  formatAmount(value: number): string {
    return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  metricTrendClass(value: number): string {
    if (value > 0) { return 'text-success'; }
    if (value < 0) { return 'text-danger'; }
    return 'text-muted';
  }

  get hasBudgetData(): boolean {
    return !!(this.summary?.budgetSummaries?.length);
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
        { name: t('DASHBOARD.TOTAL_REVENUE'), data: summary.rollingMonthCreditSeries },
        { name: t('DASHBOARD.TOTAL_EXPENSES'), data: summary.rollingMonthDebitSeries }
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
    const data = this.monthlySummaries.map((item) => item.cashFlow);
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
    const netProfitSeries = summary.rollingMonthCreditSeries.map(
      (credit, i) => (credit || 0) - (summary.rollingMonthDebitSeries[i] || 0)
    );

    this.netProfitTrendChartConfig = {
      chart: { type: 'area', height: 310, toolbar: { show: false }, fontFamily: 'inherit', sparkline: { enabled: false } },
      stroke: { width: 3, curve: 'smooth' },
      fill: {
        type: 'gradient',
        gradient: { shadeIntensity: 1, opacityFrom: 0.45, opacityTo: 0.05, stops: [0, 100] }
      },
      series: [{ name: t('DASHBOARD.NET_PROFIT'), data: netProfitSeries }],
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
