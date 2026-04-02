import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  AccountDto,
  AccountFormDto,
  AccountingCheckDto,
  AccountingSettingsDto,
  AccountingTransactionDto,
  AccountTreeDto,
  BalanceSheetReportDto,
  BankAccountDto,
  DashboardSummary,
  FiscalPeriodDto,
  FiscalPeriodFormDto,
  FiscalYearDto,
  FiscalYearFormDto,
  JournalEntry,
  JournalEntryForm,
  PaymentVoucher,
  PaymentVoucherForm,
  ProfitLossReportDto,
  ReceiptVoucher,
  ReceiptVoucherForm,
  TransferDto,
  LedgerDto,
  CustomerInvoiceDto,
  ReconciliationBankAccountDto,
  ReconciliationDto,
  ReconciliationLineDto,
  ReconciliationSummaryDto,
  AccountingSettingsUpdateDto
} from '../models/accounting.models';
import { ApiResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AccountingApiService {
  private readonly base = `${environment.apiBaseUrl}/accounting`;

  constructor(private http: HttpClient) {}

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<ApiResponse<DashboardSummary>>(`${this.base}/dashboard/financial-stats`).pipe(map((res) => res.data));
  }

  getAccounts(filters: { search?: string; type?: string; active?: boolean | '' } = {}): Observable<AccountDto[]> {
    return this.http
      .get<ApiResponse<AccountDto[]>>(`${this.base}/accounts`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getAccountTree(): Observable<AccountTreeDto[]> {
    return this.http.get<ApiResponse<AccountTreeDto[]>>(`${this.base}/accounts/tree`).pipe(map((res) => res.data || []));
  }

  getAccount(id: number): Observable<AccountDto> {
    return this.http.get<ApiResponse<AccountDto>>(`${this.base}/accounts/${id}`).pipe(map((res) => res.data));
  }

  createAccount(payload: AccountFormDto): Observable<AccountDto> {
    return this.http.post<ApiResponse<AccountDto>>(`${this.base}/accounts`, payload).pipe(map((res) => res.data));
  }

  updateAccount(id: number, payload: AccountFormDto): Observable<AccountDto> {
    return this.http.put<ApiResponse<AccountDto>>(`${this.base}/accounts/${id}`, payload).pipe(map((res) => res.data));
  }

  activateAccount(id: number): Observable<void> {
    return this.http.put<ApiResponse<void>>(`${this.base}/accounts/${id}/activate`, {}).pipe(map(() => undefined));
  }

  deactivateAccount(id: number): Observable<void> {
    return this.http.put<ApiResponse<void>>(`${this.base}/accounts/${id}/deactivate`, {}).pipe(map(() => undefined));
  }

  getJournalEntries(filters: Record<string, string | number | boolean> = {}): Observable<JournalEntry[]> {
    return this.http
      .get<ApiResponse<JournalEntry[]>>(`${this.base}/journal-entries`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getJournalEntry(id: number): Observable<JournalEntry> {
    return this.http.get<ApiResponse<JournalEntry>>(`${this.base}/journal-entries/${id}`).pipe(map((res) => res.data));
  }

  createJournalEntry(payload: JournalEntryForm): Observable<JournalEntry> {
    return this.http.post<ApiResponse<JournalEntry>>(`${this.base}/journal-entries`, payload).pipe(map((res) => res.data));
  }

  updateJournalEntry(id: number, payload: JournalEntryForm): Observable<JournalEntry> {
    return this.http.put<ApiResponse<JournalEntry>>(`${this.base}/journal-entries/${id}`, payload).pipe(map((res) => res.data));
  }

  postJournalEntry(id: number, postedBy: string): Observable<JournalEntry> {
    const params = new HttpParams().set('postedBy', postedBy);
    return this.http.post<ApiResponse<JournalEntry>>(`${this.base}/journal-entries/${id}/post`, {}, { params }).pipe(map((res) => res.data));
  }

  reverseJournalEntry(id: number, reversedBy: string, reason: string): Observable<JournalEntry> {
    let params = new HttpParams().set('reversedBy', reversedBy);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<JournalEntry>>(`${this.base}/journal-entries/${id}/reverse`, {}, { params }).pipe(map((res) => res.data));
  }

  getPaymentVouchers(filters: Record<string, string | number | boolean> = {}): Observable<PaymentVoucher[]> {
    return this.http
      .get<ApiResponse<PaymentVoucher[]>>(`${this.base}/payment-vouchers`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getPaymentVoucher(id: number): Observable<PaymentVoucher> {
    return this.http.get<ApiResponse<PaymentVoucher>>(`${this.base}/payment-vouchers/${id}`).pipe(map((res) => res.data));
  }

  createPaymentVoucher(payload: PaymentVoucherForm): Observable<PaymentVoucher> {
    return this.http.post<ApiResponse<PaymentVoucher>>(`${this.base}/payment-vouchers`, payload).pipe(map((res) => res.data));
  }

  updatePaymentVoucher(id: number, payload: PaymentVoucherForm): Observable<PaymentVoucher> {
    return this.http.put<ApiResponse<PaymentVoucher>>(`${this.base}/payment-vouchers/${id}`, payload).pipe(map((res) => res.data));
  }

  approvePaymentVoucher(id: number, actor: string): Observable<PaymentVoucher> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<PaymentVoucher>>(`${this.base}/payment-vouchers/${id}/approve`, {}, { params }).pipe(map((res) => res.data));
  }

  postPaymentVoucher(id: number, actor: string): Observable<PaymentVoucher> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<PaymentVoucher>>(`${this.base}/payment-vouchers/${id}/post`, {}, { params }).pipe(map((res) => res.data));
  }

  cancelPaymentVoucher(id: number, actor: string, reason?: string): Observable<PaymentVoucher> {
    let params = new HttpParams().set('actor', actor);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<PaymentVoucher>>(`${this.base}/payment-vouchers/${id}/cancel`, {}, { params }).pipe(map((res) => res.data));
  }

  getReceiptVouchers(filters: Record<string, string | number | boolean> = {}): Observable<ReceiptVoucher[]> {
    return this.http
      .get<ApiResponse<ReceiptVoucher[]>>(`${this.base}/receipt-vouchers`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getReceiptVoucher(id: number): Observable<ReceiptVoucher> {
    return this.http.get<ApiResponse<ReceiptVoucher>>(`${this.base}/receipt-vouchers/${id}`).pipe(map((res) => res.data));
  }

  createReceiptVoucher(payload: ReceiptVoucherForm): Observable<ReceiptVoucher> {
    return this.http.post<ApiResponse<ReceiptVoucher>>(`${this.base}/receipt-vouchers`, payload).pipe(map((res) => res.data));
  }

  updateReceiptVoucher(id: number, payload: ReceiptVoucherForm): Observable<ReceiptVoucher> {
    return this.http.put<ApiResponse<ReceiptVoucher>>(`${this.base}/receipt-vouchers/${id}`, payload).pipe(map((res) => res.data));
  }

  approveReceiptVoucher(id: number, actor: string): Observable<ReceiptVoucher> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<ReceiptVoucher>>(`${this.base}/receipt-vouchers/${id}/approve`, {}, { params }).pipe(map((res) => res.data));
  }

  postReceiptVoucher(id: number, actor: string): Observable<ReceiptVoucher> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<ReceiptVoucher>>(`${this.base}/receipt-vouchers/${id}/post`, {}, { params }).pipe(map((res) => res.data));
  }

  cancelReceiptVoucher(id: number, actor: string, reason?: string): Observable<ReceiptVoucher> {
    let params = new HttpParams().set('actor', actor);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<ReceiptVoucher>>(`${this.base}/receipt-vouchers/${id}/cancel`, {}, { params }).pipe(map((res) => res.data));
  }

  getTransfers(filters: Record<string, string | number | boolean> = {}): Observable<TransferDto[]> {
    return this.http.get<ApiResponse<TransferDto[]>>(`${this.base}/transfers`, { params: this.toParams(filters) }).pipe(map((res) => res.data || []));
  }

  getTransactions(filters: Record<string, string | number | boolean> = {}): Observable<AccountingTransactionDto[]> {
    return this.http
      .get<ApiResponse<AccountingTransactionDto[]>>(`${this.base}/transactions`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getInvoices(filters: Record<string, string | number | boolean> = {}): Observable<CustomerInvoiceDto[]> {
    return this.http.get<ApiResponse<CustomerInvoiceDto[]>>(`${this.base}/invoices`, { params: this.toParams(filters) }).pipe(map((res) => res.data || []));
  }

  getChecks(filters: Record<string, string | number | boolean> = {}): Observable<AccountingCheckDto[]> {
    return this.http.get<ApiResponse<AccountingCheckDto[]>>(`${this.base}/checks`, { params: this.toParams(filters) }).pipe(map((res) => res.data || []));
  }

  getBankAccounts(filters: Record<string, string | number | boolean> = {}): Observable<BankAccountDto[]> {
    return this.http
      .get<ApiResponse<BankAccountDto[]>>(`${this.base}/bank-accounts`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getLedger(filters: Record<string, string | number | boolean> = {}): Observable<LedgerDto> {
    return this.http.get<ApiResponse<LedgerDto>>(`${this.base}/ledger`, { params: this.toParams(filters) }).pipe(map((res) => res.data));
  }

  getReconciliations(filters: Record<string, string | number | boolean> = {}): Observable<ReconciliationDto[]> {
    return this.http
      .get<ApiResponse<ReconciliationDto[]>>(`${this.base}/reconciliation`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getReconciliation(id: number): Observable<ReconciliationDto> {
    return this.http.get<ApiResponse<ReconciliationDto>>(`${this.base}/reconciliation/${id}`).pipe(map((res) => res.data));
  }

  getReconciliationBankAccounts(): Observable<ReconciliationBankAccountDto[]> {
    return this.http.get<ApiResponse<ReconciliationBankAccountDto[]>>(`${this.base}/reconciliation/bank-accounts`).pipe(map((res) => res.data || []));
  }

  getReconciliationStatementLines(reconciliationId: number): Observable<ReconciliationLineDto[]> {
    return this.http
      .get<ApiResponse<ReconciliationLineDto[]>>(`${this.base}/reconciliation/${reconciliationId}/statement-lines`)
      .pipe(map((res) => res.data || []));
  }

  getReconciliationSystemTransactions(reconciliationId: number): Observable<ReconciliationLineDto[]> {
    return this.http
      .get<ApiResponse<ReconciliationLineDto[]>>(`${this.base}/reconciliation/${reconciliationId}/system-transactions`)
      .pipe(map((res) => res.data || []));
  }

  getReconciliationSummary(reconciliationId: number): Observable<ReconciliationSummaryDto> {
    return this.http.get<ApiResponse<ReconciliationSummaryDto>>(`${this.base}/reconciliation/${reconciliationId}/summary`).pipe(map((res) => res.data));
  }

  createReconciliation(payload: unknown): Observable<ReconciliationDto> {
    return this.http.post<ApiResponse<ReconciliationDto>>(`${this.base}/reconciliation`, payload).pipe(map((res) => res.data));
  }

  matchReconciliationLines(reconciliationId: number, statementLineId: number, systemLineId: number): Observable<ReconciliationDto> {
    const params = new HttpParams().set('statementLineId', String(statementLineId)).set('systemLineId', String(systemLineId));
    return this.http.post<ApiResponse<ReconciliationDto>>(`${this.base}/reconciliation/${reconciliationId}/match`, {}, { params }).pipe(map((res) => res.data));
  }

  unmatchReconciliationLine(reconciliationId: number, lineId: number): Observable<ReconciliationDto> {
    const params = new HttpParams().set('lineId', String(lineId));
    return this.http.post<ApiResponse<ReconciliationDto>>(`${this.base}/reconciliation/${reconciliationId}/unmatch`, {}, { params }).pipe(map((res) => res.data));
  }

  finalizeReconciliation(reconciliationId: number, actor: string): Observable<ReconciliationDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<ReconciliationDto>>(`${this.base}/reconciliation/${reconciliationId}/finalize`, {}, { params }).pipe(map((res) => res.data));
  }

  cancelReconciliation(reconciliationId: number, actor: string): Observable<ReconciliationDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<ReconciliationDto>>(`${this.base}/reconciliation/${reconciliationId}/cancel`, {}, { params }).pipe(map((res) => res.data));
  }

  getProfitLoss(fromDate: string, toDate: string): Observable<ProfitLossReportDto> {
    const params = new HttpParams().set('fromDate', fromDate).set('toDate', toDate);
    return this.http.get<ApiResponse<ProfitLossReportDto>>(`${this.base}/reports/profit-loss`, { params }).pipe(map((res) => res.data));
  }

  getBalanceSheet(asOfDate: string): Observable<BalanceSheetReportDto> {
    const params = new HttpParams().set('asOfDate', asOfDate);
    return this.http.get<ApiResponse<BalanceSheetReportDto>>(`${this.base}/reports/balance-sheet`, { params }).pipe(map((res) => res.data));
  }

  getSettings(): Observable<AccountingSettingsDto> {
    return this.http.get<ApiResponse<AccountingSettingsDto>>(`${this.base}/settings`).pipe(map((res) => res.data));
  }

  updateSettings(payload: AccountingSettingsUpdateDto): Observable<AccountingSettingsDto> {
    return this.http.put<ApiResponse<AccountingSettingsDto>>(`${this.base}/settings`, payload).pipe(map((res) => res.data));
  }

  createFiscalYear(payload: FiscalYearFormDto): Observable<FiscalYearDto> {
    return this.http.post<ApiResponse<FiscalYearDto>>(`${this.base}/settings/fiscal-years`, payload).pipe(map((res) => res.data));
  }

  closeFiscalYear(id: number, actor: string): Observable<FiscalYearDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<FiscalYearDto>>(`${this.base}/settings/fiscal-years/${id}/close`, {}, { params }).pipe(map((res) => res.data));
  }

  openFiscalYear(id: number): Observable<FiscalYearDto> {
    return this.http.post<ApiResponse<FiscalYearDto>>(`${this.base}/settings/fiscal-years/${id}/open`, {}).pipe(map((res) => res.data));
  }

  createFiscalPeriod(fiscalYearId: number, payload: FiscalPeriodFormDto): Observable<FiscalPeriodDto> {
    return this.http
      .post<ApiResponse<FiscalPeriodDto>>(`${this.base}/settings/fiscal-years/${fiscalYearId}/periods`, payload)
      .pipe(map((res) => res.data));
  }

  closeFiscalPeriod(id: number, actor: string): Observable<FiscalPeriodDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<FiscalPeriodDto>>(`${this.base}/settings/fiscal-periods/${id}/close`, {}, { params }).pipe(map((res) => res.data));
  }

  openFiscalPeriod(id: number): Observable<FiscalPeriodDto> {
    return this.http.post<ApiResponse<FiscalPeriodDto>>(`${this.base}/settings/fiscal-periods/${id}/open`, {}).pipe(map((res) => res.data));
  }

  private toParams(filters: Record<string, string | number | boolean | ''>): HttpParams {
    let params = new HttpParams();
    Object.keys(filters || {}).forEach((key: string) => {
      const value = filters[key];
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return params;
  }
}
