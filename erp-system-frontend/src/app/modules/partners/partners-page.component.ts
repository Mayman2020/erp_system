import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { AccountDto } from '../../core/models/accounting.models';
import { PartnerDto, PartnerForm } from '../../core/models/partners.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { PartnersApiService } from '../../core/services/partners-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-partners-page',
  templateUrl: './partners-page.component.html',
  styleUrls: ['./partners-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PartnersPageComponent extends ErpMasterPageBase<PartnerDto, PartnerForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'PARTNERS.TITLE',
    createKey: 'PARTNERS.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['ACTIVE', 'INACTIVE']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'code', title: 'PARTNERS.CODE' },
    { key: 'name', title: 'COMMON.NAME' },
    { key: 'sharePercent', title: 'PARTNERS.SHARE', align: 'end' },
    { key: 'capitalAccountName', title: 'PARTNERS.CAPITAL' },
    { key: 'drawingAccountName', title: 'PARTNERS.DRAWING' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly form = this.fb.group({
    code: ['', Validators.required],
    name: ['', Validators.required],
    sharePercent: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    capitalAccountId: [null as number | null],
    drawingAccountId: [null as number | null],
    active: [true]
  });

  accounts: AccountDto[] = [];

  get tableActions(): DataTableAction[] {
    return MASTER_CRUD_ACTIONS;
  }

  constructor(
    private api: PartnersApiService,
    private accountingApi: AccountingApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get accountOptions() {
    return [{ id: null, label: '—' }, ...(this.accounts || []).map((a) => ({ id: a.id, label: `${a.code} - ${a.nameEn || a.name}` }))];
  }

  ngOnInit(): void {
    forkJoin({ accounts: this.accountingApi.getAccounts({ active: true }) }).subscribe({
      next: ({ accounts }) => {
        this.accounts = accounts || [];
        this.initMasterPage();
      },
      error: () => this.initMasterPage()
    });
  }

  ngOnDestroy(): void {
    this.destroyMasterPage();
  }

  protected fetchList(_filters?: Record<string, string>): Observable<PartnerDto[]> {
    return this.api.getPartners();
  }

  protected fetchOne(id: number): Observable<PartnerDto> {
    return this.api.getPartner(id);
  }

  protected createItem(payload: PartnerForm): Observable<PartnerDto> {
    return this.api.createPartner(payload);
  }

  protected updateItem(id: number, payload: PartnerForm): Observable<PartnerDto> {
    return this.api.updatePartner(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deletePartner(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { code: '', name: '', sharePercent: 0, capitalAccountId: null, drawingAccountId: null, active: true };
  }

  protected patchForm(dto: PartnerDto): void {
    this.form.patchValue({
      code: dto.code,
      name: dto.name,
      sharePercent: dto.sharePercent,
      capitalAccountId: dto.capitalAccountId ?? null,
      drawingAccountId: dto.drawingAccountId ?? null,
      active: dto.active
    });
  }

  protected toPayload(): PartnerForm {
    const v = this.form.getRawValue();
    return {
      code: String(v.code || '').trim(),
      name: String(v.name || '').trim(),
      sharePercent: Number(v.sharePercent),
      capitalAccountId: v.capitalAccountId ?? undefined,
      drawingAccountId: v.drawingAccountId ?? undefined,
      active: !!v.active
    };
  }

  protected mapRow(dto: PartnerDto): Record<string, unknown> {
    return {
      ...dto,
      active: dto.active ? 'ACTIVE' : 'INACTIVE',
      capitalAccountName: dto.capitalAccountCode ? `${dto.capitalAccountCode} - ${dto.capitalAccountName || ''}` : '—',
      drawingAccountName: dto.drawingAccountCode ? `${dto.drawingAccountCode} - ${dto.drawingAccountName || ''}` : '—'
    };
  }
}
