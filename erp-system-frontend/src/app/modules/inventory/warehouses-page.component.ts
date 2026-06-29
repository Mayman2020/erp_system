import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { WarehouseDto, WarehouseForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-warehouses-page',
  templateUrl: './warehouses-page.component.html',
  styleUrls: ['./warehouses-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WarehousesPageComponent extends ErpMasterPageBase<WarehouseDto, WarehouseForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.WAREHOUSES',
    createKey: 'ERP.CREATE_WAREHOUSE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'code', title: 'ERP.CODE' },
    { key: 'name', title: 'COMMON.NAME' },
    { key: 'location', title: 'ERP.LOCATION' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ];

  readonly form = this.fb.group({ code: ['', Validators.required], nameEn: ['', Validators.required], nameAr: [''], location: [''], active: [true] });
  
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

  protected fetchList(filters: Record<string, string>): Observable<WarehouseDto[]> {
    return this.api.getWarehouses(filters);
  }

  protected fetchOne(id: number): Observable<WarehouseDto> {
    return this.api.getWarehouse(id);
  }

  protected createItem(payload: WarehouseForm): Observable<WarehouseDto> {
    return this.api.createWarehouse(payload);
  }

  protected updateItem(id: number, payload: WarehouseForm): Observable<WarehouseDto> {
    return this.api.updateWarehouse(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteWarehouse(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return this.form.getRawValue();
  }

  protected patchForm(dto: WarehouseDto): void {
    this.form.patchValue({ code: dto.code, nameEn: dto.name || dto.nameEn, nameAr: dto.nameAr || '', location: dto.location || '', active: dto.active !== false });
  }

  protected toPayload(): WarehouseForm {
    const v = this.form.getRawValue(); return { code: v.code, nameEn: v.nameEn, nameAr: v.nameAr || undefined, location: v.location || undefined, active: v.active !== false };
  }

  protected mapRow(dto: WarehouseDto): Record<string, unknown> {
    return { ...dto, name: dto.name || dto.nameEn };
  }
}
