import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { CustomerDto, CustomerForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-customers-page',
  templateUrl: './customers-page.component.html',
  styleUrls: ['./customers-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CustomersPageComponent extends ErpMasterPageBase<CustomerDto, CustomerForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.CUSTOMERS',
    createKey: 'ERP.CREATE_CUSTOMER',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'code', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'email', title: 'ERP.EMAIL' },
    { key: 'phone', title: 'ERP.PHONE' },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean' }
  ];

  readonly form = this.fb.group({ code: [''], nameEn: ['', Validators.required], nameAr: [''], email: [''], phone: [''], taxNumber: [''], address: [''], creditLimit: [0], active: [true] });
  
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

  protected fetchList(filters: Record<string, string>): Observable<CustomerDto[]> {
    return this.api.getCustomers(filters);
  }

  protected fetchOne(id: number): Observable<CustomerDto> {
    return this.api.getCustomer(id);
  }

  protected createItem(payload: CustomerForm): Observable<CustomerDto> {
    return this.api.createCustomer(payload);
  }

  protected updateItem(id: number, payload: CustomerForm): Observable<CustomerDto> {
    return this.api.updateCustomer(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteCustomer(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return this.form.getRawValue();
  }

  protected patchForm(dto: CustomerDto): void {
    this.form.patchValue({ code: dto.code, nameEn: dto.nameEn, nameAr: dto.nameAr || '', email: dto.email || '', phone: dto.phone || '', taxNumber: dto.taxNumber || '', address: dto.address || '', creditLimit: dto.creditLimit || 0, active: dto.active !== false });
  }

  protected toPayload(): CustomerForm {
    const v = this.form.getRawValue(); return { code: v.code || undefined, nameEn: v.nameEn, nameAr: v.nameAr || undefined, email: v.email || undefined, phone: v.phone || undefined, taxNumber: v.taxNumber || undefined, address: v.address || undefined, creditLimit: Number(v.creditLimit || 0), active: v.active !== false };
  }

  protected mapRow(dto: CustomerDto): Record<string, unknown> {
    return { ...dto };
  }
}
