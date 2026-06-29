export type AccountingType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
export type DocumentStatus = 'DRAFT' | 'APPROVED' | 'REVERSED' | 'CANCELLED';
export type BalanceSide = 'DEBIT' | 'CREDIT';

export interface RecentDocument {
  id: number;
  reference: string;
  date: string;
  amount: number;
  status: DocumentStatus | string;
}

export type RecentActivityKind = 'journals' | 'payments' | 'receipts';
export type SortDirection = 'asc' | 'desc';
export interface RecentActivityItem extends RecentDocument {}

export interface BudgetSnapshot {
  id: number;
  label: string;
  planned: number;
  actual: number;
  variance: number;
}

export interface BankBalance {
  id: number;
  bankName: string;
  accountNumber: string;
  balance: number;
  currency: string;
}

export interface DashboardSummary {
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  totalRevenue: number;
  totalExpenses: number;
  netProfit: number;
  weekDebitSeries: number[];
  weekCreditSeries: number[];
  rollingMonthRevenueSeries: number[];
  rollingMonthExpenseSeries: number[];
  rollingMonthNetProfitSeries: number[];
  rollingMonthDebitSeries: number[];
  rollingMonthCreditSeries: number[];
  recentJournals: RecentDocument[];
  recentPayments: RecentDocument[];
  recentReceipts: RecentDocument[];
  receivablesSummary: number;
  payablesOutstanding: number;
  payablesPaid: number;
  budgetSummaries: BudgetSnapshot[];
  bankBalances: BankBalance[];
}

export interface AccountDto {
  id: number;
  code: string;
  name: string;
  nameAr: string;
  nameEn: string;
  financialStatement?: 'BALANCE_SHEET' | 'INCOME_STATEMENT';
  parentId: number | null;
  parentCode: string | null;
  accountType: AccountingType;
  level: number;
  fullPath: string;
  active: boolean;
  openingBalance: number;
  openingBalanceSide: BalanceSide | null;
}

export interface AccountTreeDto {
  id: number;
  code: string;
  name: string;
  nameAr: string;
  nameEn: string;
  accountType: AccountingType;
  financialStatement?: 'BALANCE_SHEET' | 'INCOME_STATEMENT';
  level: number;
  active: boolean;
  children: AccountTreeDto[];
}

export interface AccountFormDto {
  code?: string;
  name: string;
  nameEn: string;
  nameAr?: string;
  parentId?: number | null;
  accountType: AccountingType;
  active?: boolean;
  openingBalance?: number;
  openingBalanceSide?: BalanceSide | null;
}

export interface JournalEntryLine {
  id?: number;
  accountId: number;
  accountCode?: string;
  accountNameEn?: string;
  accountNameAr?: string;
  description?: string;
  debit: number;
  credit: number;
  lineNumber?: number;
}

export interface JournalEntry {
  id: number;
  referenceNumber: string;
  entryDate: string;
  description?: string;
  externalReference?: string;
  currencyCode?: string;
  entryType?: string;
  status: DocumentStatus | string;
  totalDebit: number;
  totalCredit: number;
  balanced: boolean;
  postedAt?: string;
  postedBy?: string;
  reversedAt?: string;
  reversedBy?: string;
  reversalReference?: string;
  sourceModule?: string;
  sourceRecordId?: number;
  lines: JournalEntryLine[];
  createdBy?: string;
  createdAt?: string;
}

export interface JournalEntryLineForm {
  accountId: number;
  description?: string;
  debit: number;
  credit: number;
}

export interface JournalEntryForm {
  entryDate: string;
  description?: string;
  externalReference?: string;
  currencyCode?: string;
  entryType?: string;
  lines: JournalEntryLineForm[];
}

export interface BankAccountDto {
  id: number;
  bankName: string;
  accountNumber: string;
  iban?: string;
  currency: string;
  openingBalance: number;
  currentBalance: number;
  active: boolean;
  linkedAccountId: number;
  linkedAccountCode?: string;
  linkedAccountName?: string;
  linkedAccountNameEn?: string;
  linkedAccountNameAr?: string;
}

export interface VoucherBase {
  id: number;
  voucherDate: string;
  reference: string;
  description?: string;
  amount: number;
  status: DocumentStatus | string;
  paymentMethod: string;
  currencyCode: string;
  voucherType: string;
  partyName?: string;
  createdBy?: string;
  /** Server audit: when the voucher row was created (ISO instant) */
  createdAt?: string;
}

export interface PaymentVoucher extends VoucherBase {
  linkedDocumentReference?: string;
  billId?: number;
  cashAccountId: number;
  cashAccountCode?: string;
  cashAccountName?: string;
  cashAccountNameEn?: string;
  cashAccountNameAr?: string;
  expenseAccountId: number;
  expenseAccountCode?: string;
  expenseAccountName?: string;
  expenseAccountNameEn?: string;
  expenseAccountNameAr?: string;
}

export interface ReceiptVoucher extends VoucherBase {
  invoiceReference?: string;
  cashAccountId: number;
  cashAccountCode?: string;
  cashAccountName?: string;
  cashAccountNameEn?: string;
  cashAccountNameAr?: string;
  revenueAccountId: number;
  revenueAccountCode?: string;
  revenueAccountName?: string;
  revenueAccountNameEn?: string;
  revenueAccountNameAr?: string;
}

export interface PaymentVoucherForm {
  voucherDate: string;
  reference?: string;
  description?: string;
  amount: number;
  cashAccountId: number;
  expenseAccountId: number;
  paymentMethod: string;
  currencyCode: string;
  voucherType: string;
  billId?: number | null;
  partyName?: string;
  linkedDocumentReference?: string;
}

export interface ReceiptVoucherForm {
  voucherDate: string;
  reference?: string;
  description?: string;
  amount: number;
  cashAccountId: number;
  revenueAccountId: number;
  paymentMethod: string;
  currencyCode: string;
  voucherType: string;
  partyName?: string;
  invoiceReference?: string;
}

export interface ReconciliationLineDto {
  id: number;
  transactionDate: string;
  description?: string;
  amount: number;
  transactionType: 'BANK_STATEMENT' | 'SYSTEM_TRANSACTION';
  status: string;
  sourceReference?: string;
  journalEntryLineId?: number | null;
  matchedLineId?: number | null;
  matchedAmount?: number | null;
}

export interface ReconciliationDto {
  id: number;
  bankAccountId: number;
  bankAccountNumber: string;
  statementStartDate: string;
  statementEndDate: string;
  openingBalance: number;
  closingBalance: number;
  systemEndingBalance: number;
  difference: number;
  status: string;
  matchedCount: number;
  partiallyMatchedCount: number;
  unmatchedCount: number;
  lines: ReconciliationLineDto[];
}

export interface ReconciliationSummaryDto {
  reconciliationId: number;
  openingBalance: number;
  closingBalance: number;
  systemEndingBalance: number;
  difference: number;
  matchedCount: number;
  partiallyMatchedCount: number;
  unmatchedCount: number;
}

export interface ReconciliationBankAccountDto {
  id: number;
  bankName: string;
  accountNumber: string;
  currency: string;
  currentBalance: number;
}

export interface ProfitLossLineDto {
  accountId: number;
  accountCode: string;
  accountNameEn: string;
  accountNameAr?: string;
  amount: number;
}

export interface ProfitLossReportDto {
  fromDate: string;
  toDate: string;
  revenues: ProfitLossLineDto[];
  expenses: ProfitLossLineDto[];
  totalRevenue: number;
  totalExpenses: number;
  netProfit: number;
}

export interface BalanceSheetLineDto {
  accountId: number;
  accountCode: string;
  accountNameEn: string;
  accountNameAr?: string;
  balance: number;
}

export interface BalanceSheetReportDto {
  asOfDate: string;
  assets: BalanceSheetLineDto[];
  liabilities: BalanceSheetLineDto[];
  equity: BalanceSheetLineDto[];
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  liabilitiesAndEquity: number;
  balanced: boolean;
}

export interface AccountingTransactionDto {
  id: number;
  transactionDate: string;
  reference: string;
  description?: string;
  transactionType: string;
  status: string;
  amount: number;
  debitAccountName?: string;
  debitAccountNameEn?: string;
  debitAccountNameAr?: string;
  creditAccountName?: string;
  creditAccountNameEn?: string;
  creditAccountNameAr?: string;
}

export interface DocumentLineDto {
  id?: number;
  accountId: number;
  accountCode?: string;
  accountName?: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  lineTotal?: number;
}

export interface CustomerInvoiceDto {
  id: number;
  invoiceNumber: string;
  invoiceDate: string;
  dueDate: string;
  customerName: string;
  customerReference?: string;
  description?: string;
  subtotal?: number;
  taxAmount?: number;
  totalAmount: number;
  paidAmount?: number;
  outstandingAmount: number;
  status: string;
  receivableAccountId?: number;
  receivableAccountCode?: string;
  receivableAccountName?: string;
  revenueAccountId?: number;
  revenueAccountCode?: string;
  revenueAccountName?: string;
  journalEntryId?: number;
  lines?: DocumentLineDto[];
}

export interface CustomerInvoiceLineForm {
  accountId: number;
  description?: string;
  quantity: number;
  unitPrice: number;
}

export interface CustomerInvoiceForm {
  invoiceNumber?: string;
  invoiceDate: string;
  dueDate: string;
  customerName?: string;
  customerReference?: string;
  description?: string;
  receivableAccountId: number;
  revenueAccountId: number;
  taxAmount: number;
  lines: CustomerInvoiceLineForm[];
}

export interface AccountingCheckDto {
  id: number;
  checkNumber: string;
  checkType: string;
  bankName: string;
  issueDate?: string;
  dueDate: string;
  amount: number;
  status: string;
  partyName?: string;
  linkedDocumentReference?: string;
  bankAccountId?: number;
  bankAccountNumber?: string;
  holdingAccountId?: number;
  holdingAccountCode?: string;
  holdingAccountName?: string;
}

export interface AccountingCheckForm {
  checkNumber?: string;
  checkType: string;
  issueDate: string;
  dueDate: string;
  bankName: string;
  amount: number;
  partyName?: string;
  linkedDocumentReference?: string;
  bankAccountId: number;
  holdingAccountId: number;
}

export interface BankAccountForm {
  bankName: string;
  accountNumber: string;
  iban?: string;
  currency: string;
  openingBalance: number;
  linkedAccountId: number;
  active: boolean;
}

export interface BillDto {
  id: number;
  billNumber: string;
  billDate: string;
  dueDate: string;
  supplierName?: string;
  supplierReference?: string;
  description?: string;
  subtotal?: number;
  taxAmount?: number;
  totalAmount: number;
  paidAmount?: number;
  outstandingAmount?: number;
  status: string;
  payableAccountId?: number;
  payableAccountCode?: string;
  payableAccountName?: string;
  taxAccountId?: number;
  lines?: DocumentLineDto[];
}

export interface BillLineForm {
  accountId: number;
  description?: string;
  quantity: number;
  unitPrice: number;
}

export interface BillForm {
  billNumber?: string;
  billDate: string;
  dueDate: string;
  supplierName?: string;
  supplierReference?: string;
  description?: string;
  payableAccountId: number;
  taxAccountId?: number | null;
  taxAmount: number;
  lines: BillLineForm[];
}

export interface BudgetDto {
  id: number;
  accountId: number;
  accountCode?: string;
  accountName?: string;
  budgetName?: string;
  budgetYear: number;
  budgetMonth?: number | null;
  plannedAmount: number;
  actualAmount?: number;
  variance?: number;
  variancePercentage?: number;
  overBudget?: boolean;
  status: string;
  notes?: string;
}

export interface BudgetForm {
  accountId: number;
  budgetName?: string;
  budgetYear: number;
  budgetMonth?: number | null;
  plannedAmount: number;
  status: string;
  notes?: string;
}

export interface ExchangeRateDto {
  id: number;
  sourceCurrency: string;
  targetCurrency: string;
  rate: number;
  effectiveDate: string;
  expiryDate?: string;
}

export interface ExchangeRateForm {
  sourceCurrency: string;
  targetCurrency: string;
  rate: number;
  effectiveDate: string;
  expiryDate?: string;
}

export interface AccountingTransactionForm {
  transactionDate: string;
  reference?: string;
  description?: string;
  transactionType: string;
  amount: number;
  debitAccountId?: number | null;
  creditAccountId?: number | null;
  originalTransactionId?: number | null;
  relatedDocumentReference?: string;
}

export interface LedgerLineDto {
  journalEntryId: number;
  journalReference: string;
  entryDate: string;
  lineNumber: number;
  transactionType?: string;
  sourceReference?: string;
  description?: string;
  debit: number;
  credit: number;
  runningBalance: number;
}

export interface LedgerDto {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountNameAr?: string;
  accountNameEn?: string;
  openingBalance: number;
  closingBalance: number;
  lines: LedgerLineDto[];
}

export interface NumberingSequenceDto {
  id: number;
  sequenceName: string;
  prefix: string;
  currentNumber: number;
  paddingLength: number;
}

export interface FiscalPeriodDto {
  id: number;
  fiscalYearId: number;
  periodName: string;
  startDate: string;
  endDate: string;
  open: boolean;
  closedAt?: string;
  closedBy?: string;
}

export interface FiscalYearDto {
  id: number;
  year: number;
  startDate: string;
  endDate: string;
  open: boolean;
  closedAt?: string;
  closedBy?: string;
  periods: FiscalPeriodDto[];
}

export interface AccountingSettingsDto {
  accountingMethod: string;
  baseCurrency: string;
  allowedCurrencies: string;
  sequences: NumberingSequenceDto[];
  fiscalYears: FiscalYearDto[];
}

export interface AccountingSettingsUpdateDto {
  accountingMethod: string;
  baseCurrency: string;
  allowedCurrencies: string;
}

export interface FiscalYearFormDto {
  year: number;
  startDate: string;
  endDate: string;
}

export interface FiscalPeriodFormDto {
  periodName: string;
  startDate: string;
  endDate: string;
}

export interface TransferDto {
  id: number;
  transferDate: string;
  reference?: string;
  description?: string;
  amount: number;
  sourceAccountId: number;
  sourceAccountCode?: string;
  sourceAccountName?: string;
  destinationAccountId: number;
  destinationAccountCode?: string;
  destinationAccountName?: string;
  status: string;
  postedAt?: string;
  postedBy?: string;
  journalEntryId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface TransferForm {
  transferDate: string;
  reference?: string;
  description?: string;
  amount: number;
  sourceAccountId: number;
  destinationAccountId: number;
}
