import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { EmployeeDto, LeaveRequestDto, LeaveRequestForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-leave-requests-page',
  templateUrl: './leave-requests-page.component.html',
  styleUrls: ['./leave-requests-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LeaveRequestsPageComponent extends ErpMasterPageBase<LeaveRequestDto, LeaveRequestForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = { titleKey: 'MENU.LEAVE_REQUESTS', createKey: 'ERP.CREATE_LEAVE', editKey: 'COMMON.EDIT', viewKey: 'COMMON.VIEW', showStatus: true, statusOptions: ['DRAFT', 'APPROVED', 'CANCELLED'] };
  readonly columns: DataTableColumn[] = [
    { key: 'employeeId', title: 'MENU.EMPLOYEES' },
    { key: 'leaveType', title: 'ERP.LEAVE_TYPE' },
    { key: 'startDate', title: 'COMMON.FROM_DATE', kind: 'date' },
    { key: 'endDate', title: 'COMMON.TO_DATE', kind: 'date' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];
  readonly form = this.fb.group({
    employeeId: [null as number | null, Validators.required],
    leaveType: ['ANNUAL', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    reason: ['']
  });
  employees: EmployeeDto[] = [];

  constructor(private api: ErpApiService, private fb: FormBuilder, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  get tableActions(): DataTableAction[] {
    return [...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DRAFT' },
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (r) => ['APPROVED', 'CANCELLED'].includes(String(r['status'])) },
      MASTER_CRUD_ACTIONS[2]];
  }

  get employeeOptions() {
    return [{ id: null, label: '—' }, ...(this.employees || []).map((e) => ({ id: e.id, label: `${e.employeeCode} - ${e.fullNameEn}` }))];
  }

  ngOnInit(): void {
    this.api.getEmployees().subscribe({ next: (rows) => { this.employees = rows || []; this.initMasterPage(); } });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'approve' && id) { this.api.approveLeaveRequest(id, this.actorEmail).subscribe({ next: () => this.load() }); return; }
    if (event.actionId === 'cancel' && id) { this.api.cancelLeaveRequest(id, this.actorEmail).subscribe({ next: () => this.load() }); return; }
    super.onTableAction(event);
  }

  protected fetchList(f: Record<string, string>): Observable<LeaveRequestDto[]> { return this.api.getLeaveRequests(f); }
  protected fetchOne(id: number): Observable<LeaveRequestDto> { return this.api.getLeaveRequest(id); }
  protected createItem(p: LeaveRequestForm): Observable<LeaveRequestDto> { return this.api.createLeaveRequest(p); }
  protected updateItem(id: number, p: LeaveRequestForm): Observable<LeaveRequestDto> { return this.api.updateLeaveRequest(id, p); }
  protected removeItem(id: number): Observable<void> { return this.api.deleteLeaveRequest(id); }
  protected defaultFormValues(): Record<string, unknown> { return { employeeId: null, leaveType: 'ANNUAL', startDate: '', endDate: '', reason: '' }; }
  protected patchForm(dto: LeaveRequestDto): void {
    this.form.patchValue({ employeeId: dto.employeeId, leaveType: dto.leaveType, startDate: dto.startDate, endDate: dto.endDate, reason: dto.reason || '' });
  }
  protected toPayload(): LeaveRequestForm {
    const v = this.form.getRawValue();
    return { employeeId: Number(v.employeeId), leaveType: v.leaveType!, startDate: v.startDate!, endDate: v.endDate!, reason: v.reason || undefined };
  }
  protected mapRow(dto: LeaveRequestDto): Record<string, unknown> { return { ...dto }; }
}
