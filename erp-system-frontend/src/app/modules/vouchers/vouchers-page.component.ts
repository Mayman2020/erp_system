import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { distinctUntilChanged, finalize, map } from 'rxjs/operators';
import { AccountDto, BankAccountDto, PaymentVoucher, PaymentVoucherForm, ReceiptVoucher, ReceiptVoucherForm } from '../../core/models/accounting.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { LookupService } from '../../core/services/lookup.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  selector: 'app-vouchers-page',
  templateUrl: './vouchers-page.component.html',
  styleUrls: ['./vouchers-page.component.scss']
})
export class VouchersPageComponent implements OnInit {
  readonly titleKey = 'VOUCHERS.TITLE';
  readonly columns: DataTableColumn[] = [
    { key: 'reference', title: 'VOUCHERS.LIST.NUMBER', align: 'start' },
    { key: 'voucherDate', title: 'VOUCHERS.LIST.DATE' },
    { key: 'partyName', title: 'VOUCHERS.LIST.PARTY', align: 'start' },
    { key: 'paymentMethod', title: 'VOUCHERS.LIST.METHOD', prefix: 'PAYMENT_METHOD.', kind: 'type' },
    { key: 'cashAccountName', title: 'VOUCHERS.LIST.BANK_ACCOUNT', align: 'start' },
    { key: 'amountDisplay', title: 'VOUCHERS.LIST.AMOUNT', align: 'end' },
    { key: 'currencyCode', title: 'VOUCHERS.LIST.CURRENCY' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'createdBy', title: 'VOUCHERS.LIST.CREATED_BY', align: 'start' }
  ];
  readonly actions: DataTableAction[] = [
    { id: 'view', labelKey: 'COMMON.VIEW', className: 'btn-outline-secondary' },
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'btn-outline-primary' },
    { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'btn-outline-info' },
    { id: 'post', labelKey: 'COMMON.POST', className: 'btn-outline-success' },
    { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'btn-outline-danger' }
  ];

  activeVoucherType: 'payment' | 'receipt' = 'payment';
  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  rows: Array<Record<string, unknown>> = [];
  paymentVouchers: PaymentVoucher[] = [];
  receiptVouchers: ReceiptVoucher[] = [];

  paymentMethods: string[] = [];
  voucherStatuses: string[] = [];
  currencies: string[] = [];
  voucherTypes: string[] = [];
  bankAccounts: BankAccountDto[] = [];
  accountOptions: AccountDto[] = [];

  selectedId: number | null = null;
  dialogVisible = false;
  dialogTitle = 'VOUCHERS.PAYMENT.CREATE';
  readOnlyMode = false;
  actorEmail = 'frontend.user';

  filters = {
    query: '',
    status: '',
    fromDate: '',
    toDate: '',
    paymentMethod: '',
    bankAccountId: '',
    minAmount: '',
    maxAmount: ''
  };

  readonly form = this.fb.group({
    voucherDate: ['', Validators.required],
    reference: [''],
    description: [''],
    partyName: ['', Validators.required],
    paymentMethod: ['', Validators.required],
    currencyCode: ['', Validators.required],
    voucherType: ['', Validators.required],
    cashAccountId: [null as number | null, Validators.required],
    targetAccountId: [null as number | null, Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    linkedReference: ['']
  });

  constructor(
    private api: AccountingApiService,
    private lookupService: LookupService,
    private fb: FormBuilder,
    private i18n: TranslationService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.actorEmail = user?.email || user?.username || 'frontend.user';
    });
    this.authService.refreshCurrentUser();
    this.bootstrapLookups();
    this.route.paramMap
      .pipe(
        map((p) => p.get('kind')),
        distinctUntilChanged()
      )
      .subscribe((kind) => {
        if (kind !== 'payment' && kind !== 'receipt') {
          this.router.navigate(['/vouchers', 'payment'], { replaceUrl: true });
          return;
        }
        this.activeVoucherType = kind === 'receipt' ? 'receipt' : 'payment';
        this.load();
      });
  }

  onSearch(searchValue?: Record<string, string>): void {
    this.filters.query = searchValue?.query || '';
    this.filters.status = searchValue?.status || '';
    this.filters.fromDate = searchValue?.fromDate || '';
    this.filters.toDate = searchValue?.toDate || '';
    this.filters.minAmount = searchValue?.minAmount || '';
    this.filters.maxAmount = searchValue?.maxAmount || '';
    this.load();
  }

  goToVoucherKind(type: 'payment' | 'receipt'): void {
    this.router.navigate(['/vouchers', type]);
  }

  createVoucher(): void {
    this.errorKey = '';
    this.selectedId = null;
    this.readOnlyMode = false;
    this.dialogTitle = this.activeVoucherType === 'payment' ? 'VOUCHERS.PAYMENT.CREATE' : 'VOUCHERS.RECEIPT.CREATE';
    this.form.enable();
    this.form.reset({
      voucherDate: new Date().toISOString().slice(0, 10),
      paymentMethod: this.paymentMethods[0] || '',
      currencyCode: this.currencies[0] || '',
      voucherType: this.voucherTypes[0] || ''
    });
    this.dialogVisible = true;
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) {
      return;
    }
    if (event.actionId === 'view') {
      this.openEdit(id, true);
      return;
    }
    if (event.actionId === 'edit') {
      const status = String(event.row['status'] || '');
      if (status !== 'DRAFT') {
        return;
      }
      this.openEdit(id, false);
      return;
    }
    if (event.actionId === 'approve') {
      if (!confirm(this.i18n.instant('VOUCHERS.CONFIRM_APPROVE'))) {
        return;
      }
      this.runWorkflow(id, 'approve');
      return;
    }
    if (event.actionId === 'post') {
      if (!confirm(this.i18n.instant('VOUCHERS.CONFIRM_POST'))) {
        return;
      }
      this.runWorkflow(id, 'post');
      return;
    }
    if (event.actionId === 'cancel') {
      if (!confirm(this.i18n.instant('VOUCHERS.CONFIRM_CANCEL'))) {
        return;
      }
      this.runWorkflow(id, 'cancel');
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorKey = 'VOUCHERS.SAVE_ERROR';
      return;
    }
    const raw = this.form.getRawValue();
    this.saving = true;
    const commonPayload = {
      voucherDate: raw.voucherDate || '',
      reference: raw.reference || undefined,
      description: raw.description || undefined,
      amount: Number(raw.amount || 0),
      cashAccountId: Number(raw.cashAccountId || 0),
      paymentMethod: String(raw.paymentMethod || ''),
      currencyCode: String(raw.currencyCode || ''),
      voucherType: String(raw.voucherType || ''),
      partyName: raw.partyName || undefined
    };

    if (this.activeVoucherType === 'payment') {
      const payload: PaymentVoucherForm = {
        ...commonPayload,
        expenseAccountId: Number(raw.targetAccountId || 0),
        linkedDocumentReference: raw.linkedReference || undefined
      };
      const request$ = this.selectedId ? this.api.updatePaymentVoucher(this.selectedId, payload) : this.api.createPaymentVoucher(payload);
      request$.pipe(finalize(() => (this.saving = false))).subscribe(
        () => {
          this.successKey = 'VOUCHERS.SAVE_SUCCESS';
          this.errorKey = '';
          this.dialogVisible = false;
          this.load();
        },
        () => {
          this.errorKey = 'VOUCHERS.SAVE_ERROR';
        }
      );
      return;
    }

    const payload: ReceiptVoucherForm = {
      ...commonPayload,
      revenueAccountId: Number(raw.targetAccountId || 0),
      invoiceReference: raw.linkedReference || undefined
    };
    const request$ = this.selectedId ? this.api.updateReceiptVoucher(this.selectedId, payload) : this.api.createReceiptVoucher(payload);
    request$.pipe(finalize(() => (this.saving = false))).subscribe(
      () => {
        this.successKey = 'VOUCHERS.SAVE_SUCCESS';
        this.errorKey = '';
        this.dialogVisible = false;
        this.load();
      },
      () => {
        this.errorKey = 'VOUCHERS.SAVE_ERROR';
      }
    );
  }

  load(): void {
    this.loading = true;
    if (this.errorKey === 'COMMON.ERROR_LOADING') {
      this.errorKey = '';
    }
    const filters: Record<string, string> = {
      search: this.filters.query,
      status: this.filters.status,
      fromDate: this.filters.fromDate,
      toDate: this.filters.toDate,
      paymentMethod: this.filters.paymentMethod,
      bankAccountId: this.filters.bankAccountId,
      minAmount: this.filters.minAmount,
      maxAmount: this.filters.maxAmount
    };

    if (this.activeVoucherType === 'payment') {
      this.api
        .getPaymentVouchers(filters)
        .pipe(finalize(() => (this.loading = false)))
        .subscribe(
          (items: PaymentVoucher[]) => {
            this.paymentVouchers = items;
            this.rows = items.map((voucher) => this.toRow(voucher));
          },
          () => {
            this.errorKey = 'COMMON.ERROR_LOADING';
            this.rows = [];
          }
        );
      return;
    }

    this.api
      .getReceiptVouchers(filters)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe(
        (items: ReceiptVoucher[]) => {
          this.receiptVouchers = items;
          this.rows = items.map((voucher) => this.toRow(voucher));
        },
        () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      );
  }

  private openEdit(id: number, readOnly: boolean): void {
    this.errorKey = '';
    this.selectedId = id;
    this.readOnlyMode = readOnly;
    this.dialogTitle = readOnly ? 'VOUCHERS.VIEW_TITLE' : 'VOUCHERS.EDIT_TITLE';
    if (this.activeVoucherType === 'payment') {
      this.api.getPaymentVoucher(id).subscribe(
        (voucher: PaymentVoucher) => this.patchDialogForm(voucher, voucher.expenseAccountId, voucher.linkedDocumentReference, readOnly),
        () => undefined
      );
      return;
    }
    this.api.getReceiptVoucher(id).subscribe(
      (voucher: ReceiptVoucher) => this.patchDialogForm(voucher, voucher.revenueAccountId, voucher.invoiceReference, readOnly),
      () => undefined
    );
  }

  private runWorkflow(id: number, action: 'approve' | 'post' | 'cancel'): void {
    const actor = this.currentUser();
    if (this.activeVoucherType === 'payment') {
      if (action === 'approve') {
        this.api.approvePaymentVoucher(id, actor).subscribe(
          () => {
            this.successKey = 'VOUCHERS.WORKFLOW_SUCCESS';
            this.errorKey = '';
            this.load();
          },
          () => (this.errorKey = 'VOUCHERS.WORKFLOW_ERROR')
        );
      } else if (action === 'post') {
        this.api.postPaymentVoucher(id, actor).subscribe(
          () => {
            this.successKey = 'VOUCHERS.WORKFLOW_SUCCESS';
            this.errorKey = '';
            this.load();
          },
          () => (this.errorKey = 'VOUCHERS.WORKFLOW_ERROR')
        );
      } else {
        this.api.cancelPaymentVoucher(id, actor).subscribe(
          () => {
            this.successKey = 'VOUCHERS.WORKFLOW_SUCCESS';
            this.errorKey = '';
            this.load();
          },
          () => (this.errorKey = 'VOUCHERS.WORKFLOW_ERROR')
        );
      }
      return;
    }
    if (action === 'approve') {
      this.api.approveReceiptVoucher(id, actor).subscribe(
        () => {
          this.successKey = 'VOUCHERS.WORKFLOW_SUCCESS';
          this.errorKey = '';
          this.load();
        },
        () => (this.errorKey = 'VOUCHERS.WORKFLOW_ERROR')
      );
    } else if (action === 'post') {
      this.api.postReceiptVoucher(id, actor).subscribe(
        () => {
          this.successKey = 'VOUCHERS.WORKFLOW_SUCCESS';
          this.errorKey = '';
          this.load();
        },
        () => (this.errorKey = 'VOUCHERS.WORKFLOW_ERROR')
      );
    } else {
      this.api.cancelReceiptVoucher(id, actor).subscribe(
        () => {
          this.successKey = 'VOUCHERS.WORKFLOW_SUCCESS';
          this.errorKey = '';
          this.load();
        },
        () => (this.errorKey = 'VOUCHERS.WORKFLOW_ERROR')
      );
    }
  }

  private bootstrapLookups(): void {
    this.lookupService.getLookup('payment-methods').subscribe((items) => (this.paymentMethods = items.map((item) => item.code)), () => undefined);
    this.lookupService.getLookup('voucher-statuses').subscribe((items) => (this.voucherStatuses = items.map((item) => item.code)), () => undefined);
    this.lookupService.getLookup('currencies').subscribe((items) => (this.currencies = items.map((item) => item.code)), () => undefined);
    this.lookupService.getLookup('voucher-types').subscribe((items) => (this.voucherTypes = items.map((item) => item.code)), () => undefined);
    this.api.getBankAccounts({ active: true }).subscribe((items) => (this.bankAccounts = items), () => undefined);
    this.api.getAccounts({ active: true }).subscribe((items) => (this.accountOptions = items.filter((item) => item.postable)), () => undefined);
  }

  private currentUser(): string {
    return this.actorEmail || 'frontend.user';
  }

  private toRow(voucher: PaymentVoucher | ReceiptVoucher): Record<string, unknown> {
    return {
      id: voucher.id,
      reference: voucher.reference,
      voucherDate: voucher.voucherDate,
      partyName: voucher.partyName || '-',
      paymentMethod: voucher.paymentMethod,
      cashAccountName: voucher.cashAccountName || '-',
      amountDisplay: `${Number(voucher.amount || 0).toLocaleString()} ${voucher.currencyCode || ''}`.trim(),
      currencyCode: voucher.currencyCode,
      status: voucher.status,
      createdBy: voucher.createdBy || '-'
    };
  }

  private patchDialogForm(voucher: PaymentVoucher | ReceiptVoucher, targetId: number, linkedReference?: string, readOnly = false): void {
    this.form.reset({
      voucherDate: voucher.voucherDate,
      reference: voucher.reference,
      description: voucher.description || '',
      partyName: voucher.partyName || '',
      paymentMethod: voucher.paymentMethod,
      currencyCode: voucher.currencyCode,
      voucherType: voucher.voucherType,
      cashAccountId: voucher.cashAccountId,
      targetAccountId: targetId,
      amount: Number(voucher.amount || 0),
      linkedReference: linkedReference || ''
    });
    if (readOnly) {
      this.form.disable();
    } else {
      this.form.enable();
    }
    this.dialogVisible = true;
  }
}
