import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { DashboardSummary, RecentDocument } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';

@Component({
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
  revenueExpenseChartConfig: Record<string, unknown> = {};
  cashFlowChartConfig: Record<string, unknown> = {};

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

  constructor(private api: AccountingApiService, private translationService: TranslationService) {}

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
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (summary) => {
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
    if (value > 0) {
      return 'text-success';
    }
    if (value < 0) {
      return 'text-danger';
    }
    return 'text-muted';
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
    this.revenueExpenseChartConfig = {
      chart: { type: 'line', height: 280, toolbar: { show: false } },
      stroke: { width: 3, curve: 'smooth' },
      series: [
        { name: this.translationService.instant('DASHBOARD.TOTAL_REVENUE'), data: summary.rollingMonthCreditSeries },
        { name: this.translationService.instant('DASHBOARD.TOTAL_EXPENSES'), data: summary.rollingMonthDebitSeries }
      ],
      xaxis: { categories: labels },
      yaxis: { labels: { formatter: (val: number) => this.formatAmount(val) } },
      legend: { position: 'top' },
      colors: ['#2ed8b6', '#ff5370'],
      dataLabels: { enabled: false }
    };

    this.cashFlowChartConfig = {
      chart: { type: 'bar', height: 280, toolbar: { show: false } },
      series: [{ name: this.translationService.instant('DASHBOARD.CASH_FLOW'), data: this.monthlySummaries.map((item) => item.cashFlow) }],
      xaxis: { categories: labels },
      yaxis: { labels: { formatter: (val: number) => this.formatAmount(val) } },
      colors: ['#4099ff'],
      dataLabels: { enabled: false }
    };
  }

  private getRollingMonthLabels(total: number): string[] {
    const labels: string[] = [];
    const now = new Date();
    for (let i = total - 1; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
      labels.push(
        date.toLocaleDateString(undefined, {
          month: 'short',
          year: '2-digit'
        })
      );
    }
    return labels;
  }
}
