export type AccountingType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'INCOME' | 'EXPENSE';
export type DocumentStatus = 'DRAFT' | 'APPROVED' | 'POSTED' | 'REVERSED' | 'CANCELLED';
export type BalanceSide = 'DEBIT' | 'CREDIT';

export interface RecentDocument {
  id: number;
  reference: string;
  date: string;
  amount: number;
  status: DocumentStatus | string;
}

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
  monthDebitTotal: number;
  monthCreditTotal: number;
  weekDebitSeries: number[];
  weekCreditSeries: number[];
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
  parentId: number | null;
  parentCode: string | null;
  accountType: AccountingType;
  level: number;
  fullPath: string;
  active: boolean;
  postable: boolean;
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
  level: number;
  active: boolean;
  children: AccountTreeDto[];
}

export interface AccountFormDto {
  code?: string;
  nameEn: string;
  nameAr?: string;
  parentId?: number | null;
  accountType: AccountingType;
  active?: boolean;
  postable?: boolean;
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
  lines: JournalEntryLine[];
  createdBy?: string;
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
}

export interface PaymentVoucher extends VoucherBase {
  linkedDocumentReference?: string;
  billId?: number;
  cashAccountId: number;
  cashAccountCode?: string;
  cashAccountName?: string;
  expenseAccountId: number;
  expenseAccountCode?: string;
  expenseAccountName?: string;
}

export interface ReceiptVoucher extends VoucherBase {
  invoiceReference?: string;
  cashAccountId: number;
  cashAccountCode?: string;
  cashAccountName?: string;
  revenueAccountId: number;
  revenueAccountCode?: string;
  revenueAccountName?: string;
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

export interface TransferDto {
  id: number;
  transferDate: string;
  reference: string;
  description?: string;
  amount: number;
  status: string;
  sourceAccountName?: string;
  destinationAccountName?: string;
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
  creditAccountName?: string;
}

export interface CustomerInvoiceDto {
  id: number;
  invoiceNumber: string;
  invoiceDate: string;
  dueDate: string;
  customerName: string;
  totalAmount: number;
  outstandingAmount: number;
  status: string;
}

export interface AccountingCheckDto {
  id: number;
  checkNumber: string;
  checkType: string;
  bankName: string;
  dueDate: string;
  amount: number;
  status: string;
  partyName?: string;
}

export interface LedgerLineDto {
  journalEntryId: number;
  journalReference: string;
  entryDate: string;
  lineNumber: number;
  description?: string;
  debit: number;
  credit: number;
  runningBalance: number;
}

export interface LedgerDto {
  accountId: number;
  accountCode: string;
  accountName: string;
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
}

export interface FiscalYearDto {
  id: number;
  year: number;
  startDate: string;
  endDate: string;
  open: boolean;
  periods: FiscalPeriodDto[];
}

export interface AccountingSettingsDto {
  accountingMethod: string;
  baseCurrency: string;
  allowedCurrencies: string;
  sequences: NumberingSequenceDto[];
  fiscalYears: FiscalYearDto[];
}
