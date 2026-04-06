-- V43: Arabic display labels for lookups and seeded demo/user-facing text.
-- Fills or corrects name_ar on lookup_values (often left NULL after V24 INITCAP name_en),
-- and translates Flyway demo rows so Arabic UI shows proper copy.
SET search_path TO erp_system, public;

-- ---------------------------------------------------------------------------
-- lookup_values: bilingual labels (English kept readable; Arabic for UI)
-- ---------------------------------------------------------------------------
UPDATE lookup_values lv
SET name_en = v.name_en,
    name_ar = v.name_ar,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          -- account-types (aligned with V37 bilingual labels)
          ('account-types', 'ASSET', 'Asset', 'الأصول'),
          ('account-types', 'LIABILITY', 'Liability', 'الالتزامات'),
          ('account-types', 'EQUITY', 'Equity', 'حقوق الملكية'),
          ('account-types', 'INCOME', 'Income', 'الإيرادات'),
          ('account-types', 'REVENUE', 'Revenue', 'الإيرادات'),
          ('account-types', 'EXPENSE', 'Expense', 'المصروفات'),
          -- voucher-statuses
          ('voucher-statuses', 'DRAFT', 'Draft', 'مسودة'),
          ('voucher-statuses', 'APPROVED', 'Approved', 'معتمد'),
          ('voucher-statuses', 'CANCELLED', 'Cancelled', 'ملغى'),
          -- voucher-types
          ('voucher-types', 'STANDARD', 'Standard', 'قياسي'),
          ('voucher-types', 'ADVANCE', 'Advance', 'سلفة'),
          ('voucher-types', 'BILL_PAYMENT', 'Bill payment', 'دفع فاتورة'),
          ('voucher-types', 'INVOICE_COLLECTION', 'Invoice collection', 'تحصيل فاتورة'),
          -- payment-methods
          ('payment-methods', 'CASH', 'Cash', 'نقدي'),
          ('payment-methods', 'BANK', 'Bank', 'بنك'),
          ('payment-methods', 'CHECK', 'Cheque', 'شيك'),
          -- currencies (codes stay Latin; labels Arabic)
          ('currencies', 'USD', 'US Dollar', 'دولار أمريكي'),
          ('currencies', 'EUR', 'Euro', 'يورو'),
          ('currencies', 'GBP', 'British Pound', 'جنيه إسترليني'),
          ('currencies', 'AED', 'UAE Dirham', 'درهم إماراتي'),
          ('currencies', 'SAR', 'Saudi Riyal', 'ريال سعودي'),
          ('currencies', 'EGP', 'Egyptian Pound', 'جنيه مصري'),
          ('currencies', 'OMR', 'Omani Rial', 'ريال عماني'),
          -- reconciliation
          ('reconciliation-statuses', 'OPEN', 'Open', 'مفتوحة'),
          ('reconciliation-statuses', 'IN_PROGRESS', 'In progress', 'قيد التنفيذ'),
          ('reconciliation-statuses', 'COMPLETED', 'Completed', 'مكتملة'),
          ('reconciliation-line-statuses', 'UNMATCHED', 'Unmatched', 'غير مطابق'),
          ('reconciliation-line-statuses', 'PARTIALLY_MATCHED', 'Partially matched', 'مطابق جزئياً'),
          ('reconciliation-line-statuses', 'MATCHED', 'Matched', 'مطابق'),
          -- report-periods
          ('report-periods', 'THIS_MONTH', 'This month', 'هذا الشهر'),
          ('report-periods', 'LAST_MONTH', 'Last month', 'الشهر الماضي'),
          ('report-periods', 'THIS_QUARTER', 'This quarter', 'هذا الربع'),
          ('report-periods', 'THIS_YEAR', 'This year', 'هذه السنة'),
          ('report-periods', 'CUSTOM', 'Custom', 'مخصص'),
          -- journal-entry-statuses
          ('journal-entry-statuses', 'DRAFT', 'Draft', 'مسودة'),
          ('journal-entry-statuses', 'POSTED', 'Posted', 'مرحّل'),
          ('journal-entry-statuses', 'APPROVED', 'Approved', 'معتمد'),
          ('journal-entry-statuses', 'REVERSED', 'Reversed', 'معكوس'),
          ('journal-entry-statuses', 'CANCELLED', 'Cancelled', 'ملغى'),
          -- entry-types
          ('entry-types', 'MANUAL', 'Manual', 'يدوي'),
          ('entry-types', 'ADJUSTMENT', 'Adjustment', 'تسوية'),
          ('entry-types', 'OPENING', 'Opening', 'افتتاحي'),
          ('entry-types', 'CLOSING', 'Closing', 'إغلاق'),
          ('entry-types', 'REVERSAL', 'Reversal', 'عكس قيد'),
          -- statuses
          ('statuses', 'ACTIVE', 'Active', 'نشط'),
          ('statuses', 'INACTIVE', 'Inactive', 'غير نشط'),
          -- accounting-methods
          ('accounting-methods', 'ACCRUAL', 'Accrual basis', 'على أساس الاستحقاق'),
          ('accounting-methods', 'CASH', 'Cash basis', 'على أساس النقدية'),
          -- transaction-types
          ('transaction-types', 'SALE', 'Sale', 'بيع'),
          ('transaction-types', 'PURCHASE', 'Purchase', 'شراء'),
          ('transaction-types', 'REFUND', 'Refund', 'استرداد'),
          ('transaction-types', 'ADJUSTMENT', 'Adjustment', 'تسوية'),
          -- transaction-statuses
          ('transaction-statuses', 'DRAFT', 'Draft', 'مسودة'),
          ('transaction-statuses', 'POSTED', 'Posted', 'مرحّل'),
          ('transaction-statuses', 'PENDING', 'Pending', 'قيد الانتظار'),
          ('transaction-statuses', 'COMPLETED', 'Completed', 'مكتمل'),
          ('transaction-statuses', 'CANCELLED', 'Cancelled', 'ملغى'),
          -- bill-statuses
          ('bill-statuses', 'DRAFT', 'Draft', 'مسودة'),
          ('bill-statuses', 'APPROVED', 'Approved', 'معتمد'),
          ('bill-statuses', 'POSTED', 'Posted', 'مرحّل'),
          ('bill-statuses', 'PARTIALLY_PAID', 'Partially paid', 'مدفوع جزئياً'),
          ('bill-statuses', 'PAID', 'Paid', 'مدفوع بالكامل'),
          ('bill-statuses', 'CANCELLED', 'Cancelled', 'ملغى'),
          -- budget-statuses
          ('budget-statuses', 'DRAFT', 'Draft', 'مسودة'),
          ('budget-statuses', 'APPROVED', 'Approved', 'معتمد'),
          ('budget-statuses', 'ACTIVE', 'Active', 'نشط'),
          ('budget-statuses', 'CLOSED', 'Closed', 'مغلق'),
          -- check-types
          ('check-types', 'ISSUED', 'Issued', 'صادر'),
          ('check-types', 'RECEIVED', 'Received', 'وارد'),
          -- check-statuses
          ('check-statuses', 'PENDING', 'Pending', 'قيد الانتظار'),
          ('check-statuses', 'DEPOSITED', 'Deposited', 'مودع'),
          ('check-statuses', 'CLEARED', 'Cleared', 'مسدد'),
          ('check-statuses', 'BOUNCED', 'Bounced', 'مرتجع'),
          ('check-statuses', 'CANCELLED', 'Cancelled', 'ملغى')
      ) AS v(type_code, code, name_en, name_ar)
WHERE lv.type_code = v.type_code
  AND lv.code = v.code;

-- ---------------------------------------------------------------------------
-- Demo users: Arabic names for profile display
-- ---------------------------------------------------------------------------
UPDATE user_profiles p
SET full_name = v.full_name,
    company_name = COALESCE(v.company_name, p.company_name),
    updated_by = 'flyway',
    updated_at = NOW()
FROM users u,
     (VALUES
          ('chief.accountant', 'المحاسب الرئيسي', 'شركة تجريبية للنظام'),
          ('treasury.user', 'مسؤول الخزينة', 'شركة تجريبية للنظام'),
          ('report.viewer', 'مستعرض التقارير', 'شركة تجريبية للنظام'),
          ('finance.manager', 'مدير الشؤون المالية', 'شركة تجريبية للنظام')
      ) AS v(username, full_name, company_name)
WHERE p.user_id = u.id
  AND u.username = v.username;

-- ---------------------------------------------------------------------------
-- Customer invoices (demo)
-- ---------------------------------------------------------------------------
UPDATE customer_invoices
SET customer_name = v.customer_name,
    description = v.description,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('INV-2026-001', 'شركة التجارة العالمية ذ.م.م.', 'خدمات استشارية للربع الأول 2026'),
          ('INV-2026-002', 'تقنيات المحيط الأزرق', 'تطوير برمجيات - المرحلة الأولى'),
          ('INV-2026-003', 'مؤسسة نجم الصحراء', 'تجهيز بنية تقنية المعلومات'),
          ('INV-2026-004', 'مجموعة المنارة', 'عقد صيانة شهري'),
          ('INV-2026-005', 'صناعات العنقاء', 'طلب ملغى - توريد معدات')
      ) AS v(invoice_number, customer_name, description)
WHERE customer_invoices.invoice_number = v.invoice_number;

UPDATE customer_invoices
SET customer_name = 'عميل تجريبي',
    description = 'فاتورة أولية للاختبار',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE invoice_number = 'INV-000001';

UPDATE customer_invoice_lines
SET description = 'سطر فاتورة تجريبي',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE description = 'Seeded invoice line';

-- ---------------------------------------------------------------------------
-- Transactions (demo)
-- ---------------------------------------------------------------------------
UPDATE transactions
SET description = v.description,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('TX-DEMO-0001', 'معاملة إيداع بنكي'),
          ('TX-DEMO-0002', 'معاملة دفع رواتب'),
          ('TX-DEMO-0003', 'تحصيل دفعة فاتورة من شركة التجارة العالمية'),
          ('TX-DEMO-0004', 'دفعة جزئية من تقنيات المحيط الأزرق'),
          ('TX-DEMO-0005', 'دفع إيجار المكتب'),
          ('TX-DEMO-0006', 'دفع فواتير الكهرباء والماء'),
          ('TX-DEMO-0007', 'شراء لإعادة تخزين المخزون'),
          ('TX-DEMO-0008', 'شراء مستلزمات مكتبية'),
          ('TX-DEMO-0009', 'أتعاب استشارية - عميل نجم الصحراء'),
          ('TX-DEMO-0010', 'دفع رواتب الموظفين - مارس 2026')
      ) AS v(reference, description)
WHERE transactions.reference = v.reference;

-- ---------------------------------------------------------------------------
-- Journal entries & lines (demo)
-- ---------------------------------------------------------------------------
UPDATE journal_entries
SET description = v.description,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('JE-DEMO-0005', 'تسوية مخزون معلقة'),
          ('JE-DEMO-0006', 'تسوية مستحقات معتمدة'),
          ('JE-DEMO-0007', 'إثبات إيرادات - استشارات'),
          ('JE-DEMO-0008', 'استحقاق رواتب - مارس 2026'),
          ('JE-DEMO-0009', 'تسوية الرصيد الافتتاحي')
      ) AS v(reference_number, description)
WHERE journal_entries.reference_number = v.reference_number;

UPDATE journal_entry_lines jel
SET description = 'زيادة المخزون',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0005'
  AND jel.line_number = 1
  AND jel.description = 'Inventory increase';

UPDATE journal_entry_lines jel
SET description = 'زيادة الذمم الدائنة',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0005'
  AND jel.line_number = 2
  AND jel.description = 'Accounts payable increase';

UPDATE journal_entry_lines jel
SET description = 'إثبات مصروف مستحق',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0006'
  AND jel.line_number = 1
  AND jel.description = 'Accrued expense recognition';

UPDATE journal_entry_lines jel
SET description = 'التزامات مستحقة',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0006'
  AND jel.line_number = 2
  AND jel.description = 'Accrued liabilities';

UPDATE journal_entry_lines jel
SET description = 'ذمم مدينة - نجم الصحراء',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0007'
  AND jel.line_number = 1
  AND jel.description = 'Accounts receivable - Desert Star';

UPDATE journal_entry_lines jel
SET description = 'إيراد خدمات - استشارات',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0007'
  AND jel.line_number = 2
  AND jel.description = 'Service revenue - consulting';

UPDATE journal_entry_lines jel
SET description = 'مصروف رواتب',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0008'
  AND jel.line_number = 1
  AND jel.description = 'Salary expense';

UPDATE journal_entry_lines jel
SET description = 'رواتب مستحقة الدفع',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0008'
  AND jel.line_number = 2
  AND jel.description = 'Salary payable';

UPDATE journal_entry_lines jel
SET description = 'رصيد نقدية افتتاحي',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0009'
  AND jel.line_number = 1
  AND jel.description = 'Cash opening balance';

UPDATE journal_entry_lines jel
SET description = 'مساهمة رأس مال المالك',
    updated_by = 'flyway',
    updated_at = NOW()
FROM journal_entries je
WHERE je.id = jel.journal_entry_id
  AND je.reference_number = 'JE-DEMO-0009'
  AND jel.line_number = 2
  AND jel.description = 'Owner capital contribution';

-- V17 legacy journal descriptions (if still present)
UPDATE journal_entries
SET description = 'ترحيل فاتورة بيع تجريبية',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0001' AND description = 'Demo sale invoice posting';

UPDATE journal_entries
SET description = 'دفع راتب تجريبي',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0002' AND description = 'Demo salary payment';

UPDATE journal_entries
SET description = 'إيجار ومرافق تجريبية',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0003' AND description = 'Demo rent and utilities';

UPDATE journal_entries
SET description = 'إيصال خدمة تجريبي',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE reference_number = 'JE-DEMO-0004' AND description = 'Demo service receipt';

-- ---------------------------------------------------------------------------
-- Checks (demo): Arabic bank / party labels
-- ---------------------------------------------------------------------------
UPDATE checks
SET bank_name = v.bank_name,
    party_name = v.party_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('CHK-ISS-001', 'بنك الإمارات دبي الوطني', 'رواتب الفريق'),
          ('CHK-ISS-002', 'بنك الإمارات دبي الوطني', 'السلام العقارية'),
          ('CHK-ISS-003', 'مصرف أبوظبي الإسلامي', 'أوفيس مارت ذ.م.م.'),
          ('CHK-RCV-001', 'بنك أبوظبي الوطني', 'عميل خدمات أكمي'),
          ('CHK-RCV-002', 'مصرف دبي الإسلامي', 'المحيط الأزرق للتجارة'),
          ('CHK-RCV-003', 'بنك الإمارات دبي الوطني', 'مورد الإصلاح السريع'),
          ('CHK-ISS-004', 'بنك الإمارات دبي الوطني', 'مورد ملغى'),
          ('CHK-RCV-004', 'مصرف أبوظبي الإسلامي', 'مؤسسة نجم الصحراء')
      ) AS v(check_number, bank_name, party_name)
WHERE checks.check_number = v.check_number;

-- ---------------------------------------------------------------------------
-- Bank accounts: Arabic display names (keep account_number / IBAN as-is)
-- ---------------------------------------------------------------------------
UPDATE bank_accounts
SET bank_name = v.ar_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('ERP Demo Bank', 'بنك تجريبي للنظام'),
          ('Main Operating Bank', 'البنك التشغيلي الرئيسي'),
          ('Emirates NBD', 'بنك الإمارات دبي الوطني'),
          ('Abu Dhabi Islamic Bank', 'مصرف أبوظبي الإسلامي'),
          ('Bank Muscat', 'بنك مسقط')
      ) AS v(en_name, ar_name)
WHERE TRIM(bank_accounts.bank_name) = v.en_name;

-- ---------------------------------------------------------------------------
-- Payment / receipt vouchers (V17 demo; no-op if V32 truncate removed rows)
-- ---------------------------------------------------------------------------
UPDATE payment_vouchers
SET description = v.description,
    party_name = v.party_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('PV-DEMO-0001', 'دفع رواتب مارس', 'رواتب الفريق'),
          ('PV-DEMO-0002', 'مستحقات قرطاسية معلقة', 'أوفيس مارت ذ.م.م.'),
          ('PV-DEMO-0003', 'تسوية مصروف مستحق مسودة', 'مورد المرافق')
      ) AS v(reference, description, party_name)
WHERE payment_vouchers.reference = v.reference;

UPDATE receipt_vouchers
SET description = v.description,
    party_name = v.party_name,
    updated_by = 'flyway',
    updated_at = NOW()
FROM (VALUES
          ('RV-DEMO-0001', 'إيصال إيراد خدمات', 'عميل خدمات أكمي'),
          ('RV-DEMO-0002', 'تحصيل سلفة عميل', 'المحيط الأزرق للتجارة'),
          ('RV-DEMO-0003', 'تحصيل نقدي مسودة', 'عميل نقدي')
      ) AS v(reference, description, party_name)
WHERE receipt_vouchers.reference = v.reference;

-- V17 journal line descriptions (if those rows still exist)
UPDATE journal_entry_lines
SET description = CASE BTRIM(description)
                      WHEN 'Accounts receivable debit' THEN 'مدين الذمم المدينة'
                      WHEN 'Sales revenue credit' THEN 'دائن إيراد المبيعات'
                      WHEN 'Salary expense debit' THEN 'مدين مصروف الرواتب'
                      WHEN 'Bank credit' THEN 'دائن البنك'
                      WHEN 'Rent expense debit' THEN 'مدين مصروف الإيجار'
                      WHEN 'Utilities expense debit' THEN 'مدين مصروف المرافق'
                      WHEN 'Bank debit' THEN 'مدين البنك'
                      WHEN 'Service revenue credit' THEN 'دائن إيراد الخدمات'
                      ELSE description
    END,
    updated_by = 'flyway',
    updated_at = NOW()
WHERE BTRIM(description) IN (
                             'Accounts receivable debit',
                             'Sales revenue credit',
                             'Salary expense debit',
                             'Bank credit',
                             'Rent expense debit',
                             'Utilities expense debit',
                             'Bank debit',
                             'Service revenue credit'
    );
