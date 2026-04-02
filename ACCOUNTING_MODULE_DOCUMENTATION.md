# Accounting Module — Complete Documentation

## A) BUSINESS OVERVIEW

### What the Accounting Module Does
The Accounting Module is the financial backbone of the CoreERP system. It provides full double-entry bookkeeping, financial reporting, bank reconciliation, and fiscal period management for organizations operating in bilingual (English/Arabic) environments.

### Who Uses It
- **Accountants** (role: ACCOUNTANT) — day-to-day journal entries, vouchers, reconciliation, ledger review, and report generation.
- **Administrators** (role: ADMIN) — full access plus accounting settings, fiscal year/period management, user/role management, and lookup configuration.

### Core Business Value
- Enforces double-entry accounting integrity at every level
- Provides real-time financial dashboards with KPIs
- Supports full journal lifecycle: Draft → Posted → Reversed
- Delivers Profit & Loss and Balance Sheet reports with date filtering
- Handles bank reconciliation workflow end-to-end
- Manages fiscal calendar with year/period open/close controls
- Bilingual (EN/AR) with RTL/LTR support

### Major Workflows
1. **Chart of Accounts Management** — Create, edit, activate/deactivate account tree
2. **Journal Entry Lifecycle** — Create draft → validate balance → post → optionally reverse
3. **Voucher Processing** — Payment/receipt vouchers with approve → post → cancel flow
4. **Transfer Management** — Inter-account transfers with posting
5. **Bank Reconciliation** — Statement vs system matching workflow
6. **Financial Reporting** — P&L by period, Balance Sheet as-of-date
7. **Ledger Inquiry** — Per-account transaction history with running balance
8. **Fiscal Administration** — Year/period creation, close/reopen

---

## B) SCREEN-BY-SCREEN DOCUMENTATION

### 1. Dashboard
- **Purpose:** Executive financial overview with KPIs, charts, and recent documents
- **Users:** Accountants, Admins
- **Actions:** View-only; auto-refreshes on language change
- **Content:**
  - KPI cards: Total Revenue, Total Expenses, Net Profit
  - Assets/Liabilities/Equity bar
  - Monthly debit/credit trend charts (ApexCharts)
  - Recent journals, payment vouchers, receipt vouchers tables
  - Receivables/Payables summary
  - Bank balances table
  - Budget summaries
- **API:** `GET /accounting/dashboard/financial-stats`
- **States:** Loading spinner, error with retry, empty state for each section

### 2. Chart of Accounts
- **Purpose:** Manage the hierarchical account structure
- **Users:** Accountants, Admins
- **Actions:** Create, Edit, View, Activate, Deactivate, Switch between Table/Tree views
- **Fields:** Code, Name EN, Name AR, Parent Account, Account Type, Postable, Opening Balance, Balance Side
- **Validations:**
  - Code must be unique
  - Parent must be a valid existing account
  - Cannot deactivate account with posted journal lines
- **Filters:** Search by name/code, filter by type and active status
- **API:** `GET/POST/PUT /accounting/accounts`, `PUT /{id}/activate|deactivate`

### 3. Journal Entries
- **Purpose:** Create and manage double-entry journal entries
- **Users:** Accountants, Admins
- **Actions:** Create, Edit (draft only), Post, Reverse, Cancel, Delete (draft only), View details
- **Fields:** Entry Date, Description, External Reference, Currency, Entry Type, Lines (Account + Debit/Credit)
- **Validations:**
  - Minimum 2 lines required
  - Each line: either debit or credit, not both
  - Total debit must equal total credit (balanced)
  - Only draft entries can be edited/posted/deleted
  - Only posted entries can be reversed
  - Posting validates against open fiscal period
- **Filters:** Search, status filter, date range, account filter
- **API:** `GET/POST/PUT/DELETE /accounting/journal-entries`, `POST /{id}/post|reverse|cancel`

### 4. Payment & Receipt Vouchers
- **Purpose:** Manage cash/bank payment and receipt transactions
- **Users:** Accountants, Admins
- **Actions:** Create, Edit (draft only), Approve, Post, Cancel
- **Types:** Payment (standard, bill payment, advance) and Receipt (standard, invoice collection, advance)
- **Fields:** Date, Reference, Amount, Payment Method, Currency, Party, Bank/Cash Account, Expense/Revenue Account, Voucher Type
- **Validations:**
  - Amount > 0
  - Valid accounts required
  - Reference must be unique
  - Only draft can be edited
  - Approval required before posting (configurable)
- **Filters:** Status, payment method, bank account, amount range, date range, search
- **API:** `GET/POST/PUT /accounting/payment-vouchers|receipt-vouchers`, `POST /{id}/approve|post|cancel`

### 5. Transfers
- **Purpose:** Record inter-account fund transfers
- **Users:** Accountants, Admins
- **Actions:** Create, Edit, Post, Cancel
- **Fields:** Date, Reference, Description, Amount, Source Account, Destination Account
- **API:** `GET/POST/PUT /accounting/transfers`, `POST /{id}/post|cancel`

### 6. Transactions
- **Purpose:** View and manage accounting transactions
- **Users:** Accountants, Admins
- **Actions:** Create, Edit, Post, Cancel
- **Fields:** Date, Reference, Type (Sale/Purchase/etc.), Amount, Debit/Credit Accounts
- **API:** `GET/POST/PUT /accounting/transactions`, `POST /{id}/post|cancel`

### 7. Customer Invoices
- **Purpose:** Manage customer invoices and track outstanding amounts
- **Users:** Accountants, Admins
- **Actions:** Create, Edit, Post, Cancel
- **Fields:** Invoice Number, Date, Due Date, Customer, Lines, Total, Outstanding
- **API:** `GET/POST/PUT /accounting/invoices`, `POST /{id}/post|cancel`

### 8. Checks
- **Purpose:** Manage issued and received checks with lifecycle tracking
- **Users:** Accountants, Admins
- **Actions:** Create, Edit, Deposit, Clear, Bounce, Cancel
- **Fields:** Check Number, Type (Issued/Received), Bank, Due Date, Amount, Party
- **API:** `GET/POST/PUT /accounting/checks`, `POST /{id}/deposit|clear|bounce|cancel`

### 9. Bank Accounts
- **Purpose:** Manage bank account master data
- **Users:** Accountants, Admins
- **Actions:** Create, Edit, Delete
- **Fields:** Bank Name, Account Number, IBAN, Currency, Opening Balance, Linked GL Account
- **API:** `GET/POST/PUT/DELETE /accounting/bank-accounts`

### 10. Ledger
- **Purpose:** View per-account transaction history with running balance
- **Users:** Accountants, Admins
- **Actions:** Select account, set date range, run query
- **Output:** Opening balance, transaction lines (date, reference, debit, credit, running balance), closing balance
- **Validations:** Account selection required
- **API:** `GET /accounting/ledger?accountId=&fromDate=&toDate=`

### 11. Financial Reports
- **Purpose:** Generate P&L and Balance Sheet reports
- **Users:** Accountants, Admins
- **Reports:**
  - **Profit & Loss:** Revenue vs expenses for a date range → net profit/loss
  - **Balance Sheet:** Assets, liabilities, equity as of a date with accounting equation check
- **Period Presets:** This Month, Last Month, This Quarter, This Year, Custom
- **API:** `GET /accounting/reports/profit-loss?fromDate=&toDate=`, `GET /accounting/reports/balance-sheet?asOfDate=`

### 12. Bank Reconciliation
- **Purpose:** Match bank statement lines against system (ERP) transactions
- **Users:** Accountants, Admins
- **Actions:**
  - Create reconciliation (select bank account, date range, opening/closing balances)
  - Select statement line + system line → Match
  - Unmatch previously matched lines
  - Finalize when all matched and difference = 0
  - Cancel an open/in-progress reconciliation
- **States:** OPEN → IN_PROGRESS → COMPLETED (or CANCELLED)
- **Validations:**
  - Cannot match already-matched lines
  - Must pair statement line with system line (not same type)
  - All statement lines must be matched before finalize
  - Difference must be zero before finalize
  - Cannot modify completed or cancelled reconciliation
- **UI Features:**
  - Left panel: list of all reconciliations with status badges
  - Summary cards: opening balance, closing balance, matched count, difference
  - Side-by-side tables: statement lines vs system transactions
  - Unmatch buttons on both tables (for matched lines)
  - Action buttons disabled when not editable
- **API:** `GET/POST /accounting/reconciliation`, `GET /{id}/statement-lines|system-transactions|summary`, `POST /{id}/match|unmatch|finalize|cancel`

### 13. Accounting Settings
- **Purpose:** Configure accounting policies and fiscal calendar
- **Users:** Admins
- **Sections:**
  - **Accounting Policies:** Method (Accrual/Cash), Base Currency, Allowed Currencies
  - **Numbering Sequences:** View auto-numbering configuration
  - **Fiscal Calendar:** Create fiscal years and periods, close/reopen them
- **Actions:** Save settings, create fiscal year, create fiscal period, close/reopen year/period
- **API:** `GET/PUT /accounting/settings`, `POST /settings/fiscal-years`, `POST /settings/fiscal-years/{id}/close|open`, `POST /settings/fiscal-years/{id}/periods`, `POST /settings/fiscal-periods/{id}/close|open`

---

## C) BUSINESS FLOW DOCUMENTATION

### Chart of Accounts Management Flow
1. Admin creates root accounts (Assets, Liabilities, Equity, Revenue, Expenses)
2. Sub-accounts are created under parents with proper type inheritance
3. Leaf accounts are marked as "postable" for journal entries
4. Opening balances are set with debit/credit side
5. Accounts can be deactivated (if not used in posted journals) and reactivated

### Journal Entry Lifecycle
1. **Create** → Entry saved as DRAFT with auto-generated reference number
2. **Validate** → System checks debit = credit balance
3. **Post** → Status changes to POSTED; fiscal period validated; timestamp recorded
4. **Reverse** → Creates a new POSTED entry with swapped debit/credit; original marked REVERSED
5. **Cancel** → Draft entries can be cancelled (terminal state)
6. **Delete** → Draft entries can be permanently deleted

### Voucher Processing Flow
1. **Draft** → Voucher created with amount, accounts, payment method
2. **Approve** → Manager approval recorded
3. **Post** → Journal entry auto-created; accounts debited/credited
4. **Cancel** → Cancellation with reason; reversal journal if posted

### Bank Reconciliation Flow
1. Select bank account and statement period
2. Enter statement opening/closing balances
3. System auto-loads journal lines for the linked GL account within the period
4. Match statement lines with system lines (full or partial matching)
5. Difference recalculated after each match/unmatch
6. Finalize when all statement lines matched and difference = 0
7. Cancel if reconciliation is abandoned

### Financial Reporting Flow
1. Select report type (P&L or Balance Sheet)
2. Choose date range or period preset
3. System aggregates posted + reversed journal entries
4. P&L: Revenue accounts (credit - debit) vs Expense accounts (debit - credit)
5. Balance Sheet: Opening balances + journal movements; synthetic "Current Period Earnings" line in equity
6. Accounting equation verified: Assets = Liabilities + Equity

### Ledger Inquiry Flow
1. Select an account from the chart of accounts
2. Optionally set date range
3. System calculates opening balance (signed opening + prior movements if date filtered)
4. Displays each journal line with running balance
5. Shows closing balance

---

## D) RULES & VALIDATIONS

### Accounting Rules
- **Double-entry enforcement:** Every journal entry must have total debit = total credit
- **Account posting restriction:** Only postable (leaf) accounts accept journal lines
- **Inactive account restriction:** Cannot post to inactive accounts
- **Minimum lines:** Every journal must have at least 2 lines

### Posting Rules
- Only DRAFT entries can be posted
- Posting date must fall within an open fiscal period
- Posted entries cannot be edited or deleted
- Posting sets timestamp and actor

### Reversal Rules
- Only POSTED entries can be reversed
- Reversal creates a new POSTED journal with swapped debit/credit amounts
- Original entry status changes to REVERSED with cross-reference to reversal
- Reversal preserves the net-zero effect on all account balances

### Reconciliation Rules
- Must pair a BANK_STATEMENT line with a SYSTEM_TRANSACTION line
- Cannot match lines that are already matched
- Partial matching supported (when amounts differ, matched amount = min of both)
- All statement lines must be matched before finalization
- Reconciliation difference must be zero before finalization
- Cannot modify a COMPLETED or CANCELLED reconciliation
- Difference is recalculated after every match/unmatch operation

### Date / Fiscal Rules
- Fiscal years have start/end dates
- Fiscal periods belong to a fiscal year
- Posting requires the entry date to fall within an open period
- Years and periods can be closed/reopened by authorized users
- Closing prevents further posting to that period

### Balance Constraints
- Opening balances must specify a side (DEBIT or CREDIT)
- Asset and Expense accounts have normal DEBIT balance
- Liability, Equity, and Income accounts have normal CREDIT balance
- Balance Sheet equation: Total Assets = Total Liabilities + Total Equity

---

## E) API / TECHNICAL SUMMARY

### Key Endpoints (Base: `/api/v1/accounting`)

| Area | Endpoints |
|------|-----------|
| Dashboard | `GET /dashboard/financial-stats` |
| Accounts | `GET/POST/PUT /accounts`, `PUT /{id}/activate\|deactivate`, `GET /accounts/tree` |
| Journal Entries | `GET/POST/PUT/DELETE /journal-entries`, `POST /{id}/post\|reverse\|cancel` |
| Vouchers | `GET/POST/PUT /payment-vouchers\|receipt-vouchers`, `POST /{id}/approve\|post\|cancel` |
| Transfers | `GET/POST/PUT /transfers`, `POST /{id}/post\|cancel` |
| Transactions | `GET/POST/PUT /transactions`, `POST /{id}/post\|cancel` |
| Invoices | `GET/POST/PUT /invoices`, `POST /{id}/post\|cancel` |
| Checks | `GET/POST/PUT /checks`, `POST /{id}/deposit\|clear\|bounce\|cancel` |
| Bank Accounts | `GET/POST/PUT/DELETE /bank-accounts` |
| Ledger | `GET /ledger` |
| Reports | `GET /reports/profit-loss`, `GET /reports/balance-sheet` |
| Reconciliation | `GET/POST /reconciliation`, `GET /{id}/statement-lines\|system-transactions\|summary`, `POST /{id}/match\|unmatch\|finalize\|cancel` |
| Settings | `GET/PUT /settings`, fiscal year/period CRUD |
| Metadata | `GET /metadata` |

### Key Entities
- `Account` — Chart of accounts with hierarchy (self-referencing parent)
- `JournalEntry` + `JournalEntryLine` — Double-entry journal with line items
- `PaymentVoucher` / `ReceiptVoucher` — Cash/bank payment and receipt documents
- `Transfer` — Inter-account transfers
- `AccountingTransaction` — General transaction records
- `CustomerInvoice` + `CustomerInvoiceLine` — Customer billing
- `Bill` + `BillLine` — Supplier billing
- `AccountingCheck` — Check lifecycle management
- `BankAccount` — Bank account master data linked to GL accounts
- `Reconciliation` + `ReconciliationLine` — Bank reconciliation with matching
- `Budget` — Budget planning per account/period
- `FiscalYear` + `FiscalPeriod` — Fiscal calendar management
- `AccountingSettings` — Global accounting configuration

### Key Services
- `JournalEntryService` — Journal lifecycle management
- `LedgerService` — Ledger inquiry with running balance calculation
- `AccountingReportService` — P&L and Balance Sheet generation
- `ReconciliationService` — Bank reconciliation workflow
- `AccountingPostingService` — Centralized journal creation for sub-modules
- `AccountingDashboardService` — Dashboard KPI aggregation
- `AccountingAdministrationService` — Settings and fiscal management
- `PostingPeriodService` — Fiscal period validation for posting dates

### Important Relationships
- `BankAccount` → links to `Account` (GL account)
- `Reconciliation` → belongs to `BankAccount`; has many `ReconciliationLine`
- `JournalEntry` → has many `JournalEntryLine`; each line → `Account`
- `PaymentVoucher` / `ReceiptVoucher` → references `JournalEntry`
- `FiscalPeriod` → belongs to `FiscalYear`

---

## F) KNOWN RISKS / LIMITATIONS

### Honest Assessment

1. **Multi-currency support is declarative only:** The system stores `currencyCode` on journals and vouchers but does not perform exchange rate conversion or multi-currency balancing. All reports calculate in the stored amounts without currency normalization.

2. **Concurrent editing:** No optimistic locking (`@Version`) on journal entries or reconciliation. Two users editing the same draft journal simultaneously could overwrite each other's changes.

3. **Reconciliation partial matching:** While partial matching sets status to `PARTIALLY_MATCHED`, the remaining unmatched portion of a partially matched line cannot be matched to another line in the current implementation.

4. **Report performance at scale:** P&L and Balance Sheet load all journal entries for the period into memory. For large datasets (100K+ entries), this could cause performance issues. Consider adding database-level aggregation queries.

5. **Lookup type code alignment:** Some frontend lookup calls may use singular vs plural forms. A mismatch will result in empty filter dropdowns. This has been partially fixed but should be verified against the actual `lookup_types` table content.

6. **Dashboard language subscription:** The dashboard component subscribes to language changes without explicit unsubscription, which could cause a minor memory leak in single-page navigation scenarios.

7. **Fiscal period validation:** Posting is validated against fiscal periods, but if no fiscal periods are configured, the system allows posting to any date (fail-open). Consider whether fail-closed is more appropriate.

8. **Demo data in database:** Demo/seed data is now tagged with `is_demo_data = TRUE` via migration V26. For production deployment, these records should be deleted or the V17 migration should be excluded. The `SPRING_PROFILES_ACTIVE=prod` profile is supported but no automatic purge runs on prod startup.

9. **No audit trail for reconciliation:** Reconciliation match/unmatch/finalize actions are logged via `createdAt`/`updatedAt` timestamps but no detailed audit log captures who matched which lines and when.

10. **No export functionality:** Financial reports and ledger views do not have PDF/Excel export. The UI has no export buttons currently.
