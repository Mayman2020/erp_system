import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { CrmLeadDto, CrmLeadForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-leads-page',
  templateUrl: './leads-page.component.html',
  styleUrls: ['./leads-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LeadsPageComponent extends ErpMasterPageBase<CrmLeadDto, CrmLeadForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.LEADS', createKey: 'ERP.CREATE_LEAD', editKey: 'COMMON.EDIT', viewKey: 'COMMON.VIEW',
    showStatus: true, statusOptions: ['NEW', 'CONTACTED', 'QUALIFIED', 'PROPOSAL', 'WON', 'LOST']
  };
  readonly columns: DataTableColumn[] = [
    { key: 'leadNumber', title: 'ERP.NUMBER' },
    { key: 'name', title: 'COMMON.NAME' },
    { key: 'company', title: 'ERP.COMPANY' },
    { key: 'email', title: 'ERP.EMAIL' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'LEAD_STATUS.' }
  ];
  readonly form = this.fb.group({
    leadNumber: ['', Validators.required],
    name: ['', Validators.required],
    company: [''],
    email: [''],
    phone: [''],
    source: [''],
    status: ['NEW', Validators.required],
    assignedTo: [''],
    notes: ['']
  });

  constructor(private api: ErpApiService, private fb: FormBuilder, private router: Router, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  ngOnInit(): void { this.initMasterPage(); }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'convert', labelKey: 'ERP.CONVERT_TO_CUSTOMER', className: 'erp-action-success', disabledWhen: (r) => !!r['customerId'] || String(r['status']) === 'LOST' },
      { id: 'quotation', labelKey: 'ERP.CREATE_QUOTATION', className: 'erp-action-info', disabledWhen: (r) => !r['customerId'] },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'convert' && id) {
      this.confirmDialog.confirmByKey({ messageKey: 'ERP.CONVERT_CONFIRM' }).subscribe((ok) => {
        if (!ok) return;
        this.api.convertLeadToCustomer(id).subscribe({
          next: (customer) => {
            this.showSuccess('ERP.CONVERT_SUCCESS');
            this.load();
            if (customer?.id) {
              this.router.navigate(['/sales/customers'], { queryParams: { highlight: customer.id } });
            }
          }
        });
      });
      return;
    }
    if (event.actionId === 'quotation' && id) {
      this.router.navigate(['/sales/quotations'], { queryParams: { create: 1, customerId: event.row['customerId'] } });
      return;
    }
    super.onTableAction(event);
  }

  protected fetchList(f: Record<string, string>): Observable<CrmLeadDto[]> { return this.api.getLeads(f); }
  protected fetchOne(id: number): Observable<CrmLeadDto> { return this.api.getLead(id); }
  protected createItem(p: CrmLeadForm): Observable<CrmLeadDto> { return this.api.createLead(p); }
  protected updateItem(id: number, p: CrmLeadForm): Observable<CrmLeadDto> { return this.api.updateLead(id, p); }
  protected removeItem(id: number): Observable<void> { return this.api.deleteLead(id); }
  protected defaultFormValues(): Record<string, unknown> {
    return { leadNumber: `L-${Date.now()}`, name: '', company: '', email: '', phone: '', source: '', status: 'NEW', assignedTo: '', notes: '' };
  }
  protected patchForm(dto: CrmLeadDto): void {
    this.form.patchValue({ leadNumber: dto.leadNumber, name: dto.name, company: dto.company || '', email: dto.email || '', phone: dto.phone || '', source: dto.source || '', status: dto.status, assignedTo: dto.assignedTo || '', notes: dto.notes || '' });
  }
  protected toPayload(): CrmLeadForm {
    const v = this.form.getRawValue();
    return { leadNumber: v.leadNumber!, name: v.name!, company: v.company || undefined, email: v.email || undefined, phone: v.phone || undefined, source: v.source || undefined, status: v.status!, assignedTo: v.assignedTo || undefined, notes: v.notes || undefined };
  }
  protected mapRow(dto: CrmLeadDto): Record<string, unknown> { return { ...dto }; }
}
