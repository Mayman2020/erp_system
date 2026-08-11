import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import {
  MaintenanceAssetDto,
  MaintenanceChecklistDto,
  MaintenanceSparePartDto,
  MaintenanceTechnicianDto,
  MaintenanceTicketDto,
  MaintenanceTicketForm,
  ProductDto,
  WarehouseDto
} from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-tickets-page',
  templateUrl: './tickets-page.component.html',
  styleUrls: ['./tickets-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketsPageComponent implements OnInit, OnDestroy {
  readonly titleKey = 'MENU.MAINTENANCE_TICKETS';
  readonly statusOptions = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'CLOSED', 'CANCELLED'];
  readonly priorityOptions = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly typeOptions = ['CORRECTIVE', 'PREVENTIVE', 'INSPECTION'];
  readonly columns: DataTableColumn[] = [
    { key: 'ticketNo', title: 'ERP.NUMBER' },
    { key: 'title', title: 'COMMON.TITLE', align: 'start' },
    { key: 'assetName', title: 'MAINTENANCE.ASSETS', align: 'start' },
    { key: 'technicianName', title: 'MAINTENANCE.TECHNICIAN', align: 'start' },
    { key: 'priority', title: 'COMMON.PRIORITY', kind: 'status', prefix: 'MAINTENANCE_PRIORITY.' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'MAINTENANCE_STATUS.' },
    { key: 'openedAt', title: 'MAINTENANCE.OPENED_AT', kind: 'date' }
  ];
  readonly actions: DataTableAction[] = [
    { id: 'view', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' },
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-info', disabledWhen: (row) => !['OPEN', 'ASSIGNED'].includes(String(row['status'])) },
    { id: 'assign', labelKey: 'MAINTENANCE.ASSIGN', className: 'erp-action-info', disabledWhen: (row) => ['CLOSED', 'CANCELLED'].includes(String(row['status'])) },
    { id: 'start', labelKey: 'MAINTENANCE.START', className: 'erp-action-info', disabledWhen: (row) => !['OPEN', 'ASSIGNED'].includes(String(row['status'])) },
    { id: 'complete', labelKey: 'MAINTENANCE.COMPLETE', className: 'erp-action-success', disabledWhen: (row) => !['ASSIGNED', 'IN_PROGRESS'].includes(String(row['status'])) },
    { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (row) => ['CLOSED', 'CANCELLED'].includes(String(row['status'])) },
    { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-danger', disabledWhen: (row) => !['OPEN', 'ASSIGNED', 'CANCELLED'].includes(String(row['status'])) }
  ];

  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  rows: Array<Record<string, unknown>> = [];
  formVisible = false;
  assignVisible = false;
  formMode: 'create' | 'edit' | 'view' = 'create';
  selectedId: number | null = null;
  selectedTicket: MaintenanceTicketDto | null = null;
  selectedAuditRecord: Record<string, unknown> | null = null;
  actorEmail = 'system@erp.local';

  assets: MaintenanceAssetDto[] = [];
  technicians: MaintenanceTechnicianDto[] = [];
  products: ProductDto[] = [];
  warehouses: WarehouseDto[] = [];
  checklists: MaintenanceChecklistDto[] = [];
  spareParts: MaintenanceSparePartDto[] = [];

  private readonly destroy$ = new Subject<void>();
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;

  readonly form = this.fb.group({
    ticketNo: [''],
    assetId: [null as number | null],
    customerId: [null as number | null],
    title: ['', Validators.required],
    description: [''],
    priority: ['MEDIUM'],
    ticketType: ['CORRECTIVE'],
    technicianId: [null as number | null],
    slaHours: [null as number | null]
  });

  readonly assignForm = this.fb.group({
    technicianId: [null as number | null, Validators.required]
  });

  readonly checklistForm = this.fb.group({
    itemText: ['', Validators.required]
  });

  readonly spareForm = this.fb.group({
    productId: [null as number | null, Validators.required],
    warehouseId: [null as number | null, Validators.required],
    quantity: [1, [Validators.required, Validators.min(0.0001)]],
    unitCost: [null as number | null]
  });

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    private authService: AuthService,
    private confirmDialog: ConfirmDialogService,
    private cdr: ChangeDetectorRef
  ) {}

  get readOnly(): boolean {
    return this.formMode === 'view';
  }

  get assetOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...this.assets.map((a) => ({ id: a.id, label: `${a.assetCode} - ${a.name}` }))];
  }

  get technicianOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...this.technicians.map((t) => ({ id: t.id, label: t.displayName }))];
  }

  get productOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...this.products.map((p) => ({ id: p.id, label: `${p.code} - ${p.name || p.nameEn || ''}` }))];
  }

  get warehouseOptions(): Array<{ id: number | null; label: string }> {
    return [{ id: null, label: '—' }, ...this.warehouses.map((w) => ({ id: w.id, label: `${w.code} - ${w.nameEn || w.name || ''}` }))];
  }

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.actorEmail = user?.email || user?.username || 'system@erp.local';
      this.cdr.markForCheck();
    });
    this.authService.refreshCurrentUser();
    this.bootstrapLookups();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
  }

  openCreate(): void {
    this.formMode = 'create';
    this.selectedId = null;
    this.selectedTicket = null;
    this.selectedAuditRecord = null;
    this.checklists = [];
    this.spareParts = [];
    this.form.reset({
      ticketNo: '',
      assetId: null,
      customerId: null,
      title: '',
      description: '',
      priority: 'MEDIUM',
      ticketType: 'CORRECTIVE',
      technicianId: null,
      slaHours: null
    });
    this.form.enable();
    this.formVisible = true;
    this.errorKey = '';
    this.cdr.markForCheck();
  }

  closeForm(): void {
    this.formVisible = false;
    this.cdr.markForCheck();
  }

  closeAssign(): void {
    this.assignVisible = false;
    this.cdr.markForCheck();
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (!id) {
      return;
    }
    if (event.actionId === 'view') {
      this.openDocument(id, 'view');
      return;
    }
    if (event.actionId === 'edit') {
      this.openDocument(id, 'edit');
      return;
    }
    if (event.actionId === 'assign') {
      this.openAssign(id);
      return;
    }
    if (event.actionId === 'start') {
      this.runAction(id, 'start');
      return;
    }
    if (event.actionId === 'complete') {
      this.runAction(id, 'complete');
      return;
    }
    if (event.actionId === 'cancel') {
      this.runAction(id, 'cancel');
      return;
    }
    if (event.actionId === 'delete') {
      this.confirmDelete(id);
    }
  }

  save(): void {
    if (this.readOnly || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const payload = this.toPayload();
    const request$ = this.formMode === 'edit' && this.selectedId
      ? this.api.updateMaintenanceTicket(this.selectedId, payload)
      : this.api.createMaintenanceTicket(payload);
    request$
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.showSuccess('MAINTENANCE.TICKET_SAVE_SUCCESS');
          this.formVisible = false;
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
  }

  saveAssign(): void {
    if (this.assignForm.invalid || !this.selectedId) {
      this.assignForm.markAllAsTouched();
      return;
    }
    const technicianId = Number(this.assignForm.getRawValue().technicianId);
    this.saving = true;
    this.api.assignMaintenanceTicket(this.selectedId, { technicianId }, this.actorEmail)
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.showSuccess('MAINTENANCE.ASSIGN_SUCCESS');
          this.assignVisible = false;
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
  }

  addChecklistItem(): void {
    if (!this.selectedId || this.checklistForm.invalid || this.readOnly) {
      this.checklistForm.markAllAsTouched();
      return;
    }
    const itemText = String(this.checklistForm.getRawValue().itemText || '').trim();
    this.api.addMaintenanceChecklistItem(this.selectedId, { itemText }).subscribe({
      next: () => {
        this.checklistForm.reset({ itemText: '' });
        this.reloadTicketDetail(this.selectedId);
        this.showSuccess('MAINTENANCE.CHECKLIST_ADDED');
      },
      error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
    });
  }

  addSparePart(): void {
    if (!this.selectedId || this.spareForm.invalid || this.readOnly) {
      this.spareForm.markAllAsTouched();
      return;
    }
    const raw = this.spareForm.getRawValue();
    this.api.addMaintenanceSparePart(this.selectedId, {
      productId: Number(raw.productId),
      warehouseId: Number(raw.warehouseId),
      quantity: Number(raw.quantity),
      unitCost: raw.unitCost ? Number(raw.unitCost) : undefined
    }).subscribe({
      next: () => {
        this.spareForm.reset({ productId: null, warehouseId: null, quantity: 1, unitCost: null });
        this.reloadTicketDetail(this.selectedId!);
        this.showSuccess('MAINTENANCE.SPARE_ADDED');
      },
      error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
    });
  }

  issueSparePart(sparePartId: number): void {
    if (!this.selectedId) {
      return;
    }
    this.confirmDialog.confirmByKey({ messageKey: 'MAINTENANCE.ISSUE_SPARE_CONFIRM' }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.api.issueMaintenanceSparePart(this.selectedId!, sparePartId, this.actorEmail).subscribe({
        next: () => {
          this.reloadTicketDetail(this.selectedId!);
          this.showSuccess('MAINTENANCE.ISSUE_SPARE_SUCCESS');
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  private bootstrapLookups(): void {
    this.loading = true;
    forkJoin({
      assets: this.api.getMaintenanceAssets(),
      technicians: this.api.getMaintenanceTechnicians(true),
      products: this.api.getProducts(),
      warehouses: this.api.getWarehouses()
    })
      .pipe(finalize(() => {
        this.loading = false;
        this.load();
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: ({ assets, technicians, products, warehouses }) => {
          this.assets = assets || [];
          this.technicians = technicians || [];
          this.products = products || [];
          this.warehouses = warehouses || [];
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
        }
      });
  }

  private load(): void {
    this.loading = true;
    this.api.getMaintenanceTickets()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (rows) => {
          this.rows = (rows || []).map((row) => ({ ...row }));
        },
        error: () => {
          this.errorKey = 'COMMON.ERROR_LOADING';
          this.rows = [];
        }
      });
  }

  private openDocument(id: number, mode: 'edit' | 'view'): void {
    this.api.getMaintenanceTicket(id).subscribe({
      next: (ticket) => {
        this.formMode = mode;
        this.selectedId = ticket.id;
        this.selectedTicket = ticket;
        this.selectedAuditRecord = ticket as unknown as Record<string, unknown>;
        this.checklists = ticket.checklists || [];
        this.spareParts = ticket.spareParts || [];
        this.form.reset({
          ticketNo: ticket.ticketNo,
          assetId: ticket.assetId || null,
          customerId: ticket.customerId || null,
          title: ticket.title,
          description: ticket.description || '',
          priority: ticket.priority || 'MEDIUM',
          ticketType: ticket.ticketType || 'CORRECTIVE',
          technicianId: ticket.technicianId || null,
          slaHours: ticket.slaHours || null
        });
        if (mode === 'view') {
          this.form.disable();
        } else {
          this.form.enable();
        }
        this.formVisible = true;
        this.cdr.markForCheck();
      },
      error: () => this.showError('COMMON.ERROR_LOADING')
    });
  }

  private openAssign(id: number): void {
    this.selectedId = id;
    this.assignForm.reset({ technicianId: null });
    this.assignVisible = true;
    this.cdr.markForCheck();
  }

  private reloadTicketDetail(id: number): void {
    this.api.getMaintenanceTicket(id).subscribe({
      next: (ticket) => {
        this.selectedTicket = ticket;
        this.checklists = ticket.checklists || [];
        this.spareParts = ticket.spareParts || [];
        this.cdr.markForCheck();
      }
    });
  }

  private runAction(id: number, action: 'start' | 'complete' | 'cancel'): void {
    const messageKey = action === 'start'
      ? 'MAINTENANCE.START_CONFIRM'
      : action === 'complete'
        ? 'MAINTENANCE.COMPLETE_CONFIRM'
        : 'MAINTENANCE.CANCEL_CONFIRM';
    this.confirmDialog.confirmByKey({ messageKey, danger: action === 'cancel' }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      const request$ = action === 'start'
        ? this.api.startMaintenanceTicket(id, this.actorEmail)
        : action === 'complete'
          ? this.api.completeMaintenanceTicket(id, this.actorEmail)
          : this.api.cancelMaintenanceTicket(id, this.actorEmail);
      request$.subscribe({
        next: () => {
          this.showSuccess(`MAINTENANCE.${action.toUpperCase()}_SUCCESS`);
          this.formVisible = false;
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  private confirmDelete(id: number): void {
    this.confirmDialog.confirmByKey({ messageKey: 'MAINTENANCE.DELETE_TICKET_CONFIRM', danger: true }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.api.deleteMaintenanceTicket(id).subscribe({
        next: () => {
          this.showSuccess('MAINTENANCE.TICKET_DELETE_SUCCESS');
          this.load();
        },
        error: (err) => this.showError(err?.error?.message || 'COMMON.UNEXPECTED_ERROR')
      });
    });
  }

  private toPayload(): MaintenanceTicketForm {
    const raw = this.form.getRawValue();
    return {
      ticketNo: raw.ticketNo || undefined,
      assetId: raw.assetId ? Number(raw.assetId) : undefined,
      customerId: raw.customerId ? Number(raw.customerId) : undefined,
      title: raw.title,
      description: raw.description || undefined,
      priority: raw.priority || 'MEDIUM',
      ticketType: raw.ticketType || 'CORRECTIVE',
      technicianId: raw.technicianId ? Number(raw.technicianId) : undefined,
      slaHours: raw.slaHours ? Number(raw.slaHours) : undefined,
      checklists: this.checklists.map((item, index) => ({
        id: item.id,
        itemText: item.itemText,
        done: item.done,
        sortOrder: item.sortOrder ?? index
      }))
    };
  }

  private showError(key: string): void {
    this.errorKey = key;
    this.successKey = '';
    this.cdr.markForCheck();
  }

  private showSuccess(key: string): void {
    this.successKey = key;
    this.errorKey = '';
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
    }
    this.feedbackTimer = setTimeout(() => {
      this.successKey = '';
      this.cdr.markForCheck();
    }, 4000);
    this.cdr.markForCheck();
  }
}
