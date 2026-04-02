import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { BalanceSheetReportDto, ProfitLossReportDto } from '../../core/models/accounting.models';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';

@Component({
  selector: 'app-reports-page',
  templateUrl: './reports-page.component.html',
  styleUrls: ['./reports-page.component.scss']
})
export class ReportsPageComponent implements OnInit {
  readonly titleKey = 'REPORTS.TITLE';
  loading = false;
  activeReport: 'profitLoss' | 'balanceSheet' = 'profitLoss';
  periodOptions: string[] = [];
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

  constructor(private api: AccountingApiService, private lookupService: LookupService, private fb: FormBuilder) {}

  ngOnInit(): void {
    const today = new Date().toISOString().slice(0, 10);
    const monthStart = `${today.slice(0, 8)}01`;
    this.form.patchValue({ fromDate: monthStart, toDate: today, asOfDate: today });
    this.lookupService.getLookup('report-periods').subscribe((items) => (this.periodOptions = items.map((item) => item.code)), () => undefined);
    this.runReport('profitLoss');
  }

  runReport(report: 'profitLoss' | 'balanceSheet'): void {
    this.activeReport = report;
    this.loading = true;
    this.errorKey = '';
    const raw = this.form.getRawValue();
    if (report === 'profitLoss') {
      this.api
        .getProfitLoss(String(raw.fromDate || ''), String(raw.toDate || ''))
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({
          next: (result) => (this.profitLoss = result),
          error: () => (this.errorKey = 'COMMON.ERROR_LOADING')
        });
      return;
    }

    this.api
      .getBalanceSheet(String(raw.asOfDate || ''))
      .pipe(finalize(() => (this.loading = false)))
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
}

