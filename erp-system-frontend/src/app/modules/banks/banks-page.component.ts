import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { AccountDto, BankAccountDto, BankAccountForm } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-banks-page',
  templateUrl: './banks-page.component.html',
  styleUrls: ['./banks-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BanksPageComponent extends ErpMasterPageBase<BankAccountDto, BankAccountForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'BANKS.TITLE',
    createKey: 'BANKS.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['YES', 'NO']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'bankName', title: 'BANKS.BANK_NAME', align: 'start' },
    { key: 'accountNumber', title: 'BANKS.ACCOUNT_NUMBER', align: 'start' },
    { key: 'currency', title: 'BANKS.CURRENCY' },
    { key: 'currentBalance', title: 'BANKS.CURRENT_BALANCE', align: 'end' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' },
    { key: 'openingBalance', title: 'BANKS.OPENING_BALANCE', align: 'end' },
    { key: 'linkedAccountName', title: 'BANKS.LINKED_ACCOUNT', align: 'start' }
  ];

  readonly form = this.fb.group({
    bankName: ['', Validators.required],
    accountNumber: ['', Validators.required],
    iban: [''],
    currency: ['USD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    openingBalance: [0, Validators.required],
    linkedAccountId: [null as number | null, Validators.required],
    active: [true, Validators.required]
  });

  assetAccounts: AccountDto[] = [];

  constructor(
    private api: AccountingApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get accountOptions() {
    return [{ id: null, label: '—' }, ...(this.assetAccounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  ngOnInit(): void {
    this.api.getAccounts({ type: 'ASSET', active: true }).subscribe({
      next: (accounts) => {
        this.assetAccounts = accounts || [];
        this.initMasterPage();
      },
      error: () => this.initMasterPage()
    });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  protected override buildListParams(): Record<string, string> {
    const params: Record<string, string> = { search: this.filters.query || '' };
    if (this.filters.status === 'YES') {
      params.active = 'true';
    }
    if (this.filters.status === 'NO') {
      params.active = 'false';
    }
    return params;
  }

  protected fetchList(filters: Record<string, string>): Observable<BankAccountDto[]> {
    return this.api.getBankAccounts(filters);
  }

  protected fetchOne(id: number): Observable<BankAccountDto> {
    return this.api.getBankAccount(id);
  }

  protected createItem(payload: BankAccountForm): Observable<BankAccountDto> {
    return this.api.createBankAccount(payload);
  }

  protected updateItem(id: number, payload: BankAccountForm): Observable<BankAccountDto> {
    return this.api.updateBankAccount(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteBankAccount(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return {
      bankName: '',
      accountNumber: '',
      iban: '',
      currency: 'USD',
      openingBalance: 0,
      linkedAccountId: null,
      active: true
    };
  }

  protected patchForm(dto: BankAccountDto): void {
    this.form.reset({
      bankName: dto.bankName || '',
      accountNumber: dto.accountNumber || '',
      iban: dto.iban || '',
      currency: dto.currency || 'USD',
      openingBalance: Number(dto.openingBalance || 0),
      linkedAccountId: dto.linkedAccountId || null,
      active: dto.active
    });
  }

  protected toPayload(): BankAccountForm {
    const v = this.form.getRawValue();
    return {
      bankName: v.bankName!,
      accountNumber: v.accountNumber!,
      iban: v.iban || undefined,
      currency: (v.currency || 'USD').toUpperCase(),
      openingBalance: Number(v.openingBalance),
      linkedAccountId: Number(v.linkedAccountId),
      active: !!v.active
    };
  }

  protected mapRow(dto: BankAccountDto): Record<string, unknown> {
    return {
      ...dto,
      openingBalance: Number(dto.openingBalance || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      currentBalance: Number(dto.currentBalance || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    };
  }
}
