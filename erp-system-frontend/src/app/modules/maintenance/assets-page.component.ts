import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { MaintenanceAssetDto, MaintenanceAssetForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-assets-page',
  templateUrl: './assets-page.component.html',
  styleUrls: ['./assets-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssetsPageComponent extends ErpMasterPageBase<MaintenanceAssetDto, MaintenanceAssetForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.MAINTENANCE_ASSETS',
    createKey: 'MAINTENANCE.CREATE_ASSET',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: true,
    statusOptions: ['ACTIVE', 'INACTIVE'],
    deleteConfirmKey: 'MAINTENANCE.DELETE_ASSET_CONFIRM',
    saveSuccessKey: 'MAINTENANCE.ASSET_SAVE_SUCCESS',
    deleteSuccessKey: 'MAINTENANCE.ASSET_DELETE_SUCCESS'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'assetCode', title: 'ERP.CODE' },
    { key: 'name', title: 'COMMON.NAME', align: 'start' },
    { key: 'serialNo', title: 'MAINTENANCE.SERIAL_NO', align: 'start' },
    { key: 'customerName', title: 'ERP.CUSTOMER', align: 'start' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'MAINTENANCE_ASSET_STATUS.' }
  ];

  readonly form = this.fb.group({
    assetCode: ['', Validators.required],
    name: ['', Validators.required],
    serialNo: [''],
    customerId: [null as number | null],
    status: ['ACTIVE'],
    notes: ['']
  });

  get tableActions() { return this.actions; }

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  ngOnInit(): void { this.initMasterPage(); }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  protected fetchList(filters: Record<string, string>): Observable<MaintenanceAssetDto[]> {
    return this.api.getMaintenanceAssets(filters['status']);
  }

  protected fetchOne(id: number): Observable<MaintenanceAssetDto> {
    return this.api.getMaintenanceAsset(id);
  }

  protected createItem(payload: MaintenanceAssetForm): Observable<MaintenanceAssetDto> {
    return this.api.createMaintenanceAsset(payload);
  }

  protected updateItem(id: number, payload: MaintenanceAssetForm): Observable<MaintenanceAssetDto> {
    return this.api.updateMaintenanceAsset(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteMaintenanceAsset(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { assetCode: '', name: '', serialNo: '', customerId: null, status: 'ACTIVE', notes: '' };
  }

  protected patchForm(dto: MaintenanceAssetDto): void {
    this.form.patchValue({
      assetCode: dto.assetCode,
      name: dto.name,
      serialNo: dto.serialNo || '',
      customerId: dto.customerId || null,
      status: dto.status || 'ACTIVE',
      notes: dto.notes || ''
    });
  }

  protected toPayload(): MaintenanceAssetForm {
    const v = this.form.getRawValue();
    return {
      assetCode: v.assetCode,
      name: v.name,
      serialNo: v.serialNo || undefined,
      customerId: v.customerId || undefined,
      status: v.status || 'ACTIVE',
      notes: v.notes || undefined
    };
  }

  protected mapRow(row: MaintenanceAssetDto): Record<string, unknown> {
    return { ...row };
  }
}
