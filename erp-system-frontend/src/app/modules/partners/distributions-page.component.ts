import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { ProfitDistributionDto, ProfitDistributionForm } from '../../core/models/partners.models';
import { AuthService } from '../../core/auth/auth.service';
import { PartnersApiService } from '../../core/services/partners-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-distributions-page',
  templateUrl: './distributions-page.component.html',
  styleUrls: ['./distributions-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DistributionsPageComponent extends ErpMasterPageBase<ProfitDistributionDto, ProfitDistributionForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'PARTNERS.DISTRIBUTIONS_TITLE',
    createKey: 'PARTNERS.DISTRIBUTIONS_CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['DRAFT', 'APPROVED', 'CANCELLED']
  };

  readonly columns: DataTableColumn[] = [
    { key: 'distributionNo', title: 'PARTNERS.DISTRIBUTION_NO' },
    { key: 'periodLabel', title: 'PARTNERS.PERIOD_LABEL' },
    { key: 'totalProfit', title: 'PARTNERS.TOTAL_PROFIT', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly form = this.fb.group({
    distributionNo: [''],
    periodLabel: ['', Validators.required],
    totalProfit: [null as number | null],
    profitFromDate: [''],
    profitToDate: [''],
    useAccountingProfit: [false]
  });

  distributionLines: ProfitDistributionDto['lines'] = [];
  currentJournalEntryId?: number;

  constructor(
    private api: PartnersApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'approve', labelKey: 'PARTNERS.APPROVE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DRAFT' },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  ngOnInit(): void {
    this.initMasterPage();
  }

  ngOnDestroy(): void {
    this.destroyMasterPage();
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) return;
    if (event.actionId === 'approve') {
      this.api.approveDistribution(id, this.actorEmail).subscribe({ next: () => this.load() });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(_filters?: Record<string, string>): Observable<ProfitDistributionDto[]> {
    return this.api.getDistributions();
  }

  protected fetchOne(id: number): Observable<ProfitDistributionDto> {
    return this.api.getDistribution(id);
  }

  protected createItem(payload: ProfitDistributionForm): Observable<ProfitDistributionDto> {
    return this.api.createDistribution(payload);
  }

  protected updateItem(id: number, payload: ProfitDistributionForm): Observable<ProfitDistributionDto> {
    return this.api.updateDistribution(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteDistribution(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    const today = new Date().toISOString().slice(0, 10);
    const monthStart = `${today.slice(0, 8)}01`;
    return { distributionNo: '', periodLabel: '', totalProfit: null, profitFromDate: monthStart, profitToDate: today, useAccountingProfit: true };
  }

  protected patchForm(dto: ProfitDistributionDto): void {
    this.currentJournalEntryId = dto.journalEntryId;
    this.distributionLines = dto.lines || [];
    this.form.patchValue({
      distributionNo: dto.distributionNo,
      periodLabel: dto.periodLabel,
      totalProfit: dto.totalProfit,
      useAccountingProfit: false
    });
  }

  protected toPayload(): ProfitDistributionForm {
    const v = this.form.getRawValue();
    if (v.useAccountingProfit) {
      return {
        periodLabel: String(v.periodLabel || '').trim(),
        distributionNo: v.distributionNo || undefined,
        profitFromDate: String(v.profitFromDate || ''),
        profitToDate: String(v.profitToDate || '')
      };
    }
    return {
      periodLabel: String(v.periodLabel || '').trim(),
      distributionNo: v.distributionNo || undefined,
      totalProfit: Number(v.totalProfit)
    };
  }

  protected mapRow(dto: ProfitDistributionDto): Record<string, unknown> {
    return { ...dto };
  }
}
