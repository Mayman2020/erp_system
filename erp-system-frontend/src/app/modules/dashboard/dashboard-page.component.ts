import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { ErpDashboardDto } from '../../core/models/erp.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { ErpApiService } from '../../core/services/erp-api.service';

interface QuickAction {
  labelKey: string;
  icon: string;
  route: string[];
  tone: 'blue' | 'green' | 'orange' | 'purple' | 'cyan' | 'pink' | 'indigo' | 'amber';
}

@Component({
  standalone: false,
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  loading = false;
  errorKey = '';
  calendarDays: Array<{ day: number; muted?: boolean; today?: boolean }> = [];

  private readonly summarySubject = new BehaviorSubject<ErpDashboardDto | null>(null);
  readonly summary$ = this.summarySubject.asObservable();
  private readonly destroy$ = new Subject<void>();

  readonly filterFromCtrl = new FormControl('', { nonNullable: true });
  readonly filterToCtrl = new FormControl('', { nonNullable: true });

  topProductsChartId = 'erp-top-products-chart';
  salesOverviewChartId = 'erp-sales-overview-chart';
  revenueDeptChartId = 'erp-revenue-dept-chart';
  cashFlowChartId = 'erp-cash-flow-chart';

  topProductsChartConfig: Record<string, unknown> = {};
  salesOverviewChartConfig: Record<string, unknown> = {};
  revenueDeptChartConfig: Record<string, unknown> = {};
  cashFlowChartConfig: Record<string, unknown> = {};

  readonly quickActions: QuickAction[] = [
    { labelKey: 'DASHBOARD.QA_SALES_INVOICE', icon: 'receipt_long', route: ['/sales/invoices'], tone: 'blue' },
    { labelKey: 'DASHBOARD.QA_PURCHASE_ORDER', icon: 'local_shipping', route: ['/purchases/orders'], tone: 'green' },
    { labelKey: 'DASHBOARD.QA_NEW_PRODUCT', icon: 'inventory_2', route: ['/inventory/products'], tone: 'orange' },
    { labelKey: 'DASHBOARD.QA_NEW_CUSTOMER', icon: 'person_add', route: ['/sales/customers'], tone: 'purple' },
    { labelKey: 'DASHBOARD.QA_NEW_SUPPLIER', icon: 'business', route: ['/purchases/suppliers'], tone: 'cyan' },
    { labelKey: 'DASHBOARD.QA_NEW_EMPLOYEE', icon: 'badge', route: ['/hr/employees'], tone: 'pink' },
    { labelKey: 'DASHBOARD.QA_DAILY_ENTRY', icon: 'menu_book', route: ['/journal-entries'], tone: 'indigo' },
    { labelKey: 'DASHBOARD.QA_NEW_REPORT', icon: 'insert_chart', route: ['/reports/sales'], tone: 'amber' }
  ];

  lowStockColumns = [
    { key: 'productName', title: 'DASHBOARD.PRODUCT' },
    { key: 'quantity', title: 'DASHBOARD.QUANTITY' },
    { key: 'reorderLevel', title: 'DASHBOARD.REORDER_LEVEL' }
  ];

  employeeColumns = [
    { key: 'employeeName', title: 'DASHBOARD.EMPLOYEE' },
    { key: 'salesAmount', title: 'DASHBOARD.SALES_AMOUNT' },
    { key: 'performancePercent', title: 'DASHBOARD.PERFORMANCE' }
  ];

  constructor(
    private api: ErpApiService,
    private translationService: TranslationService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildCalendar();
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
    if (fromDate) filters.fromDate = fromDate;
    if (toDate) filters.toDate = toDate;

    this.api
      .getErpDashboard(filters)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (summary) => {
          if (!summary) {
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

  navigate(route: string[]): void {
    void this.router.navigate(route);
  }

  formatAmount(value: number | undefined | null): string {
    return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  growthClass(value: number | undefined | null): string {
    const v = Number(value || 0);
    if (v > 0) return 'dash-kpi-card__growth--up';
    if (v < 0) return 'dash-kpi-card__growth--down';
    return 'dash-kpi-card__growth--flat';
  }

  formatGrowth(value: number | undefined | null): string {
    const v = Number(value || 0);
    return `${v > 0 ? '+' : ''}${v.toFixed(1)}%`;
  }

  activityTimeLabel(iso?: string): string {
    if (!iso) return '';
    const date = new Date(iso);
    const diffMs = Date.now() - date.getTime();
    const mins = Math.floor(diffMs / 60000);
    if (mins < 60) {
      const count = Math.max(mins, 1);
      return this.translationService.instant('DASHBOARD.MINUTES_AGO').replace('{{count}}', String(count));
    }
    const hours = Math.floor(mins / 60);
    if (hours < 24) {
      return this.translationService.instant('DASHBOARD.HOURS_AGO').replace('{{count}}', String(hours));
    }
    const days = Math.floor(hours / 24);
    return this.translationService.instant('DASHBOARD.DAYS_AGO').replace('{{count}}', String(days));
  }

  lowStockRows(summary: ErpDashboardDto): Array<Record<string, unknown>> {
    return (summary.lowStockItems || []).map((item) => ({
      productName: item.productName,
      quantity: item.quantity,
      reorderLevel: item.reorderLevel
    }));
  }

  employeeRows(summary: ErpDashboardDto): Array<Record<string, unknown>> {
    return (summary.employeePerformance || []).map((item) => ({
      employeeName: item.employeeName,
      salesAmount: this.formatAmount(item.salesAmount),
      performancePercent: `${item.performancePercent}%`
    }));
  }

  private buildCalendar(): void {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const cells: Array<{ day: number; muted?: boolean; today?: boolean }> = [];
    for (let i = 0; i < firstDay; i++) {
      cells.push({ day: 0, muted: true });
    }
    for (let day = 1; day <= daysInMonth; day++) {
      cells.push({ day, today: day === now.getDate() });
    }
    this.calendarDays = cells;
  }

  private buildCharts(summary: ErpDashboardDto): void {
    const t = (key: string) => this.translationService.instant(key);
    const topProducts = summary.topProducts || [];
    const monthlySales = (summary.monthlySales || []).map((m) => m.amount);
    const monthlyExpenses = (summary.monthlyExpenses || []).map((m) => m.amount);
    const monthLabels = (summary.monthlySales || []).map((m) => m.month);
    const departments = summary.revenueByDepartment || [];

    const axisColor = '#6b7f96';
    const gridColor = 'rgba(14, 31, 51, 0.08)';

    this.topProductsChartConfig = {
      chart: { type: 'bar', height: 280, toolbar: { show: false }, fontFamily: 'inherit', background: 'transparent' },
      plotOptions: { bar: { horizontal: true, borderRadius: 6, barHeight: '58%', distributed: true } },
      series: [{ name: t('DASHBOARD.SALES'), data: topProducts.map((p) => p.totalRevenue) }],
      xaxis: { categories: topProducts.map((p) => p.productName), labels: { style: { colors: axisColor, fontSize: '11px' } } },
      yaxis: { labels: { style: { colors: axisColor, fontSize: '11px' } } },
      colors: ['#3d6290', '#2f6a3a', '#b48a40', '#2a5079', '#8e6a2b'],
      dataLabels: { enabled: false },
      grid: { borderColor: gridColor, strokeDashArray: 4 },
      theme: { mode: 'light' }
    };

    this.salesOverviewChartConfig = {
      chart: { type: 'line', height: 280, toolbar: { show: false }, fontFamily: 'inherit', background: 'transparent' },
      stroke: { width: 3, curve: 'smooth' },
      series: [
        { name: t('DASHBOARD.TOTAL_REVENUE'), data: monthlySales },
        { name: t('DASHBOARD.TOTAL_EXPENSES'), data: monthlyExpenses }
      ],
      xaxis: { categories: monthLabels, labels: { style: { colors: axisColor, fontSize: '11px' } } },
      yaxis: { labels: { style: { colors: axisColor, fontSize: '11px' } } },
      colors: ['#3d6290', '#2f6a3a'],
      dataLabels: { enabled: false },
      legend: { position: 'top', labels: { colors: axisColor } },
      grid: { borderColor: gridColor, strokeDashArray: 4 },
      theme: { mode: 'light' }
    };

    this.revenueDeptChartConfig = {
      chart: { type: 'donut', height: 280, fontFamily: 'inherit', background: 'transparent' },
      series: departments.map((d) => Number(d.percent || 0)),
      labels: departments.map((d) => d.departmentName),
      colors: ['#3d6290', '#b48a40', '#8e6a2b', '#2f6a3a', '#2a5079'],
      legend: { position: 'left', fontSize: '12px', labels: { colors: axisColor } },
      dataLabels: { enabled: true, style: { colors: ['#0e1f33'] }, formatter: (val: number) => `${val.toFixed(0)}%` },
      plotOptions: { pie: { donut: { size: '62%' } } },
      stroke: { colors: ['#fffdf8'] },
      theme: { mode: 'light' }
    };

    this.cashFlowChartConfig = {
      chart: { type: 'bar', height: 280, toolbar: { show: false }, fontFamily: 'inherit', background: 'transparent' },
      plotOptions: { bar: { borderRadius: 6, columnWidth: '36%' } },
      series: [
        { name: t('DASHBOARD.CASH_IN'), data: monthlySales },
        { name: t('DASHBOARD.CASH_OUT'), data: monthlyExpenses }
      ],
      xaxis: { categories: monthLabels, labels: { style: { colors: axisColor, fontSize: '11px' } } },
      yaxis: { labels: { style: { colors: axisColor, fontSize: '11px' } } },
      colors: ['#2f6a3a', '#a8332b'],
      dataLabels: { enabled: false },
      legend: { position: 'top', labels: { colors: axisColor } },
      grid: { borderColor: gridColor, strokeDashArray: 4 },
      theme: { mode: 'light' }
    };
  }
}
