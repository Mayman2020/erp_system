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
  RecentActivityItem,
  RecentActivityKind,
  ProfitLossReportDto,
  ReceiptVoucher,
  ReceiptVoucherForm,
  SortDirection,
  LedgerDto,
  AccountingCheckForm,
  BillDto,
  BillForm,
  BudgetDto,
  BudgetForm,
  CustomerInvoiceDto,
  CustomerInvoiceForm,
  ExchangeRateDto,
  ExchangeRateForm,
  BankAccountForm,
  AccountingTransactionForm,
  TransferDto,
  TransferForm,
  ReconciliationBankAccountDto,
  ReconciliationDto,
  ReconciliationLineDto,
  ReconciliationSummaryDto,
  AccountingSettingsUpdateDto
} from '../models/accounting.models';
import { ApiResponse, PagedResult } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AccountingApiService {
  private readonly base = `${environment.apiUrl}/accounting`;

  constructor(private http: HttpClient) {}

  getDashboardSummary(filters: { fromDate?: string; toDate?: string } = {}): Observable<DashboardSummary> {
    return this.http
      .get<ApiResponse<DashboardSummary>>(`${this.base}/dashboard`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data));
  }

  getRecentActivity(
    kind: RecentActivityKind,
    page = 0,
    size = 5,
    sortBy = 'date',
    sortDirection: SortDirection = 'desc'
  ): Observable<PagedResult<RecentActivityItem>> {
    return this.http
      .get<ApiResponse<PagedResult<RecentActivityItem>>>(`${this.base}/dashboard/recent-activity/${kind}`, {
        params: this.toParams({ page, size, sortBy, sortDirection })
      })
      .pipe(map((res) => res.data));
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

  approveJournalEntry(id: number, approvedBy: string): Observable<JournalEntry> {
    const params = new HttpParams().set('approvedBy', approvedBy);
    return this.http.post<ApiResponse<JournalEntry>>(`${this.base}/journal-entries/${id}/approve`, {}, { params }).pipe(map((res) => res.data));
  }

  /** @deprecated Use approveJournalEntry; kept for API compatibility. */
  postJournalEntry(id: number, postedBy: string): Observable<JournalEntry> {
    return this.approveJournalEntry(id, postedBy);
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

  getTransactions(filters: Record<string, string | number | boolean> = {}): Observable<AccountingTransactionDto[]> {
    return this.http
      .get<ApiResponse<AccountingTransactionDto[]>>(`${this.base}/transactions`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getInvoices(filters: Record<string, string | number | boolean> = {}): Observable<CustomerInvoiceDto[]> {
    return this.http.get<ApiResponse<CustomerInvoiceDto[]>>(`${this.base}/invoices`, { params: this.toParams(filters) }).pipe(map((res) => res.data || []));
  }

  getInvoice(id: number): Observable<CustomerInvoiceDto> {
    return this.http.get<ApiResponse<CustomerInvoiceDto>>(`${this.base}/invoices/${id}`).pipe(map((res) => res.data));
  }

  createInvoice(payload: CustomerInvoiceForm): Observable<CustomerInvoiceDto> {
    return this.http.post<ApiResponse<CustomerInvoiceDto>>(`${this.base}/invoices`, payload).pipe(map((res) => res.data));
  }

  updateInvoice(id: number, payload: CustomerInvoiceForm): Observable<CustomerInvoiceDto> {
    return this.http.put<ApiResponse<CustomerInvoiceDto>>(`${this.base}/invoices/${id}`, payload).pipe(map((res) => res.data));
  }

  approveInvoice(id: number, actor: string): Observable<CustomerInvoiceDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<CustomerInvoiceDto>>(`${this.base}/invoices/${id}/approve`, {}, { params }).pipe(map((res) => res.data));
  }

  cancelInvoice(id: number, actor: string, reason?: string): Observable<CustomerInvoiceDto> {
    let params = new HttpParams().set('actor', actor);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<CustomerInvoiceDto>>(`${this.base}/invoices/${id}/cancel`, {}, { params }).pipe(map((res) => res.data));
  }

  getChecks(filters: Record<string, string | number | boolean> = {}): Observable<AccountingCheckDto[]> {
    return this.http.get<ApiResponse<AccountingCheckDto[]>>(`${this.base}/checks`, { params: this.toParams(filters) }).pipe(map((res) => res.data || []));
  }

  getCheck(id: number): Observable<AccountingCheckDto> {
    return this.http.get<ApiResponse<AccountingCheckDto>>(`${this.base}/checks/${id}`).pipe(map((res) => res.data));
  }

  createCheck(payload: AccountingCheckForm): Observable<AccountingCheckDto> {
    return this.http.post<ApiResponse<AccountingCheckDto>>(`${this.base}/checks`, payload).pipe(map((res) => res.data));
  }

  updateCheck(id: number, payload: AccountingCheckForm): Observable<AccountingCheckDto> {
    return this.http.put<ApiResponse<AccountingCheckDto>>(`${this.base}/checks/${id}`, payload).pipe(map((res) => res.data));
  }

  depositCheck(id: number, actor: string): Observable<AccountingCheckDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<AccountingCheckDto>>(`${this.base}/checks/${id}/deposit`, {}, { params }).pipe(map((res) => res.data));
  }

  clearCheck(id: number, actor: string): Observable<AccountingCheckDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<AccountingCheckDto>>(`${this.base}/checks/${id}/clear`, {}, { params }).pipe(map((res) => res.data));
  }

  bounceCheck(id: number, actor: string, reason?: string): Observable<AccountingCheckDto> {
    let params = new HttpParams().set('actor', actor);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<AccountingCheckDto>>(`${this.base}/checks/${id}/bounce`, {}, { params }).pipe(map((res) => res.data));
  }

  cancelCheck(id: number, actor: string, reason?: string): Observable<AccountingCheckDto> {
    let params = new HttpParams().set('actor', actor);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<AccountingCheckDto>>(`${this.base}/checks/${id}/cancel`, {}, { params }).pipe(map((res) => res.data));
  }

  getBankAccounts(filters: Record<string, string | number | boolean> = {}): Observable<BankAccountDto[]> {
    return this.http
      .get<ApiResponse<BankAccountDto[]>>(`${this.base}/bank-accounts`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getBankAccount(id: number): Observable<BankAccountDto> {
    return this.http.get<ApiResponse<BankAccountDto>>(`${this.base}/bank-accounts/${id}`).pipe(map((res) => res.data));
  }

  createBankAccount(payload: BankAccountForm): Observable<BankAccountDto> {
    return this.http.post<ApiResponse<BankAccountDto>>(`${this.base}/bank-accounts`, payload).pipe(map((res) => res.data));
  }

  updateBankAccount(id: number, payload: BankAccountForm): Observable<BankAccountDto> {
    return this.http.put<ApiResponse<BankAccountDto>>(`${this.base}/bank-accounts/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteBankAccount(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/bank-accounts/${id}`).pipe(map(() => undefined));
  }

  getBills(filters: Record<string, string | number | boolean> = {}): Observable<BillDto[]> {
    return this.http.get<ApiResponse<BillDto[]>>(`${this.base}/bills`, { params: this.toParams(filters) }).pipe(map((res) => res.data || []));
  }

  getBill(id: number): Observable<BillDto> {
    return this.http.get<ApiResponse<BillDto>>(`${this.base}/bills/${id}`).pipe(map((res) => res.data));
  }

  createBill(payload: BillForm): Observable<BillDto> {
    return this.http.post<ApiResponse<BillDto>>(`${this.base}/bills`, payload).pipe(map((res) => res.data));
  }

  updateBill(id: number, payload: BillForm): Observable<BillDto> {
    return this.http.put<ApiResponse<BillDto>>(`${this.base}/bills/${id}`, payload).pipe(map((res) => res.data));
  }

  approveBill(id: number, actor: string): Observable<BillDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<BillDto>>(`${this.base}/bills/${id}/approve`, {}, { params }).pipe(map((res) => res.data));
  }

  cancelBill(id: number, actor: string, reason?: string): Observable<BillDto> {
    let params = new HttpParams().set('actor', actor);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<BillDto>>(`${this.base}/bills/${id}/cancel`, {}, { params }).pipe(map((res) => res.data));
  }

  getBudgets(filters: Record<string, string | number | boolean> = {}): Observable<BudgetDto[]> {
    return this.http.get<ApiResponse<BudgetDto[]>>(`${this.base}/budget`, { params: this.toParams(filters) }).pipe(map((res) => res.data || []));
  }

  getBudget(id: number): Observable<BudgetDto> {
    return this.http.get<ApiResponse<BudgetDto>>(`${this.base}/budget/${id}`).pipe(map((res) => res.data));
  }

  createBudget(payload: BudgetForm): Observable<BudgetDto> {
    return this.http.post<ApiResponse<BudgetDto>>(`${this.base}/budget`, payload).pipe(map((res) => res.data));
  }

  updateBudget(id: number, payload: BudgetForm): Observable<BudgetDto> {
    return this.http.put<ApiResponse<BudgetDto>>(`${this.base}/budget/${id}`, payload).pipe(map((res) => res.data));
  }

  changeBudgetStatus(id: number, status: string): Observable<BudgetDto> {
    const params = new HttpParams().set('status', status);
    return this.http.post<ApiResponse<BudgetDto>>(`${this.base}/budget/${id}/status`, {}, { params }).pipe(map((res) => res.data));
  }

  getExchangeRates(): Observable<ExchangeRateDto[]> {
    return this.http.get<ApiResponse<ExchangeRateDto[]>>(`${this.base}/exchange-rates`).pipe(map((res) => res.data || []));
  }

  createExchangeRate(payload: ExchangeRateForm): Observable<ExchangeRateDto> {
    return this.http.post<ApiResponse<ExchangeRateDto>>(`${this.base}/exchange-rates`, payload).pipe(map((res) => res.data));
  }

  getTransaction(id: number): Observable<AccountingTransactionDto> {
    return this.http.get<ApiResponse<AccountingTransactionDto>>(`${this.base}/transactions/${id}`).pipe(map((res) => res.data));
  }

  createTransaction(payload: AccountingTransactionForm): Observable<AccountingTransactionDto> {
    return this.http.post<ApiResponse<AccountingTransactionDto>>(`${this.base}/transactions`, payload).pipe(map((res) => res.data));
  }

  updateTransaction(id: number, payload: AccountingTransactionForm): Observable<AccountingTransactionDto> {
    return this.http.put<ApiResponse<AccountingTransactionDto>>(`${this.base}/transactions/${id}`, payload).pipe(map((res) => res.data));
  }

  approveTransaction(id: number, actor: string): Observable<AccountingTransactionDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http.post<ApiResponse<AccountingTransactionDto>>(`${this.base}/transactions/${id}/approve`, {}, { params }).pipe(map((res) => res.data));
  }

  cancelTransaction(id: number, actor: string, reason?: string): Observable<AccountingTransactionDto> {
    let params = new HttpParams().set('actor', actor);
    if (reason) {
      params = params.set('reason', reason);
    }
    return this.http.post<ApiResponse<AccountingTransactionDto>>(`${this.base}/transactions/${id}/cancel`, {}, { params }).pipe(map((res) => res.data));
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

  getTransfers(): Observable<TransferDto[]> {
    return this.http.get<ApiResponse<TransferDto[]>>(`${this.base}/transfers`).pipe(map((res) => res.data || []));
  }

  getTransfer(id: number): Observable<TransferDto> {
    return this.http.get<ApiResponse<TransferDto>>(`${this.base}/transfers/${id}`).pipe(map((res) => res.data));
  }

  createTransfer(payload: TransferForm): Observable<TransferDto> {
    return this.http.post<ApiResponse<TransferDto>>(`${this.base}/transfers`, payload).pipe(map((res) => res.data));
  }

  updateTransfer(id: number, payload: TransferForm): Observable<TransferDto> {
    return this.http.put<ApiResponse<TransferDto>>(`${this.base}/transfers/${id}`, payload).pipe(map((res) => res.data));
  }

  postTransfer(id: number, actor: string): Observable<TransferDto> {
    return this.http.post<ApiResponse<TransferDto>>(`${this.base}/transfers/${id}/post`, {}, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelTransfer(id: number, actor: string, reason?: string): Observable<TransferDto> {
    let params = new HttpParams().set('actor', actor);
    if (reason) params = params.set('reason', reason);
    return this.http.post<ApiResponse<TransferDto>>(`${this.base}/transfers/${id}/cancel`, {}, { params }).pipe(map((res) => res.data));
  }

  deleteTransfer(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/transfers/${id}`).pipe(map(() => undefined));
  }

  exportLedgerExcel(accountId: number, fromDate?: string, toDate?: string): Observable<Blob> {
    return this.http.get(`${this.base}/export/ledger/excel`, {
      params: this.toParams({ accountId, fromDate, toDate }),
      responseType: 'blob'
    });
  }

  exportLedgerPdf(accountId: number, fromDate?: string, toDate?: string): Observable<Blob> {
    return this.http.get(`${this.base}/export/ledger/pdf`, {
      params: this.toParams({ accountId, fromDate, toDate }),
      responseType: 'blob'
    });
  }

  exportProfitLossExcel(fromDate: string, toDate: string): Observable<Blob> {
    return this.http.get(`${this.base}/export/profit-loss/excel`, {
      params: this.toParams({ fromDate, toDate }),
      responseType: 'blob'
    });
  }

  exportProfitLossPdf(fromDate: string, toDate: string): Observable<Blob> {
    return this.http.get(`${this.base}/export/profit-loss/pdf`, {
      params: this.toParams({ fromDate, toDate }),
      responseType: 'blob'
    });
  }

  exportBalanceSheetExcel(asOfDate: string): Observable<Blob> {
    return this.http.get(`${this.base}/export/balance-sheet/excel`, {
      params: this.toParams({ asOfDate }),
      responseType: 'blob'
    });
  }

  exportBalanceSheetPdf(asOfDate: string): Observable<Blob> {
    return this.http.get(`${this.base}/export/balance-sheet/pdf`, {
      params: this.toParams({ asOfDate }),
      responseType: 'blob'
    });
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
