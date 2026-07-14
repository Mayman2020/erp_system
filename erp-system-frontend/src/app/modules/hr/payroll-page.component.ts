import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { EmployeeDto, PayrollLineDto, PayrollLineForm, PayrollRunDto, PayrollRunForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-payroll-page',
  templateUrl: './payroll-page.component.html',
  styleUrls: ['./payroll-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PayrollPageComponent extends ErpMasterPageBase<PayrollRunDto, PayrollRunForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = { titleKey: 'MENU.PAYROLL', createKey: 'ERP.CREATE_PAYROLL', editKey: 'COMMON.EDIT', viewKey: 'COMMON.VIEW', showStatus: true, statusOptions: ['DRAFT', 'APPROVED', 'CANCELLED'] };
  readonly columns: DataTableColumn[] = [
    { key: 'payrollNumber', title: 'ERP.NUMBER' },
    { key: 'periodStart', title: 'COMMON.FROM_DATE', kind: 'date' },
    { key: 'periodEnd', title: 'COMMON.TO_DATE', kind: 'date' },
    { key: 'totalAmount', title: 'ERP.AMOUNT', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];
  readonly lineColumns: DataTableColumn[] = [
    { key: 'employeeLabel', title: 'MENU.EMPLOYEES' },
    { key: 'basicSalary', title: 'ERP.BASIC_SALARY', align: 'end' },
    { key: 'allowances', title: 'ERP.ALLOWANCES', align: 'end' },
    { key: 'deductions', title: 'ERP.DEDUCTIONS', align: 'end' },
    { key: 'netSalary', title: 'ERP.NET_SALARY', align: 'end' }
  ];
  readonly form = this.fb.group({
    payrollNumber: [''],
    periodStart: ['', Validators.required],
    periodEnd: ['', Validators.required],
    totalAmount: [0, [Validators.required, Validators.min(0)]],
    notes: ['']
  });
  readonly lineForm = this.fb.group({
    employeeId: [null as number | null, Validators.required],
    basicSalary: [0, Validators.required],
    allowances: [0],
    deductions: [0],
    netSalary: [0, Validators.required]
  });

  employees: EmployeeDto[] = [];
  payrollLines: PayrollLineDto[] = [];
  linesPanelVisible = false;
  selectedPayrollId: number | null = null;
  currentJournalEntryId?: number;

  constructor(private api: ErpApiService, private fb: FormBuilder, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  ngOnInit(): void {
    forkJoin({ employees: this.api.getEmployees() }).subscribe({
      next: ({ employees }) => {
        this.employees = employees || [];
        this.initMasterPage();
      },
      error: () => this.initMasterPage()
    });
  }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  get employeeOptions() {
    return [{ id: null, label: '—' }, ...this.employees.map((e) => ({ id: e.id, label: `${e.employeeCode} - ${e.fullNameEn}` }))];
  }

  get tableActions(): DataTableAction[] {
    return [
      ...MASTER_CRUD_ACTIONS.slice(0, 2),
      { id: 'lines', labelKey: 'ERP.PAYROLL_LINES', className: 'erp-action-info' },
      { id: 'approve', labelKey: 'COMMON.APPROVE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) !== 'DRAFT' },
      { id: 'cancel', labelKey: 'COMMON.CANCEL', className: 'erp-action-warning', disabledWhen: (r) => String(r['status']) === 'CANCELLED' },
      MASTER_CRUD_ACTIONS[2]
    ];
  }

  get lineRows(): Array<Record<string, unknown>> {
    return this.payrollLines.map((l) => {
      const emp = this.employees.find((e) => e.id === l.employeeId);
      return {
        ...l,
        employeeLabel: emp ? `${emp.employeeCode} - ${emp.fullNameEn}` : `#${l.employeeId}`,
        basicSalary: Number(l.basicSalary).toLocaleString(undefined, { minimumFractionDigits: 2 }),
        allowances: Number(l.allowances || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }),
        deductions: Number(l.deductions || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }),
        netSalary: Number(l.netSalary).toLocaleString(undefined, { minimumFractionDigits: 2 })
      };
    });
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'approve' && id) { this.api.approvePayrollRun(id, this.actorEmail).subscribe({ next: () => this.load() }); return; }
    if (event.actionId === 'cancel' && id) { this.api.cancelPayrollRun(id, this.actorEmail).subscribe({ next: () => this.load() }); return; }
    if (event.actionId === 'lines' && id) { this.openLinesPanel(id, Number(event.row['journalEntryId'])); return; }
    super.onTableAction(event);
  }

  openLinesPanel(payrollId: number, journalEntryId?: number): void {
    this.selectedPayrollId = payrollId;
    this.currentJournalEntryId = journalEntryId;
    this.lineForm.reset({ employeeId: null, basicSalary: 0, allowances: 0, deductions: 0, netSalary: 0 });
    this.loadLines();
    this.linesPanelVisible = true;
    this.cdr.markForCheck();
  }

  closeLinesPanel(): void {
    this.linesPanelVisible = false;
    this.cdr.markForCheck();
  }

  loadLines(): void {
    if (!this.selectedPayrollId) return;
    this.api.getPayrollLines(this.selectedPayrollId).subscribe({
      next: (lines) => { this.payrollLines = lines || []; this.cdr.markForCheck(); }
    });
  }

  recalcNet(): void {
    const v = this.lineForm.getRawValue();
    const net = Number(v.basicSalary || 0) + Number(v.allowances || 0) - Number(v.deductions || 0);
    this.lineForm.patchValue({ netSalary: net });
  }

  addLine(): void {
    this.recalcNet();
    if (!this.selectedPayrollId || this.lineForm.invalid) {
      this.lineForm.markAllAsTouched();
      return;
    }
    const v = this.lineForm.getRawValue();
    const payload: PayrollLineForm = {
      payrollId: this.selectedPayrollId,
      employeeId: Number(v.employeeId),
      basicSalary: Number(v.basicSalary),
      allowances: Number(v.allowances || 0),
      deductions: Number(v.deductions || 0),
      netSalary: Number(v.netSalary)
    };
    this.api.createPayrollLine(payload).subscribe({
      next: () => {
        this.lineForm.reset({ employeeId: null, basicSalary: 0, allowances: 0, deductions: 0, netSalary: 0 });
        this.loadLines();
      }
    });
  }

  protected fetchList(f: Record<string, string>): Observable<PayrollRunDto[]> { return this.api.getPayrollRuns(f); }
  protected fetchOne(id: number): Observable<PayrollRunDto> { return this.api.getPayrollRun(id); }
  protected createItem(p: PayrollRunForm): Observable<PayrollRunDto> { return this.api.createPayrollRun(p); }
  protected updateItem(id: number, p: PayrollRunForm): Observable<PayrollRunDto> { return this.api.updatePayrollRun(id, p); }
  protected removeItem(id: number): Observable<void> { return this.api.deletePayrollRun(id); }
  protected defaultFormValues(): Record<string, unknown> { return { payrollNumber: '', periodStart: '', periodEnd: '', totalAmount: 0, notes: '' }; }
  protected patchForm(dto: PayrollRunDto): void {
    this.currentJournalEntryId = dto.journalEntryId;
    this.form.patchValue({ payrollNumber: dto.payrollNumber, periodStart: dto.periodStart, periodEnd: dto.periodEnd, totalAmount: dto.totalAmount, notes: dto.notes || '' });
  }
  protected toPayload(): PayrollRunForm {
    const v = this.form.getRawValue();
    return { payrollNumber: v.payrollNumber || undefined, periodStart: v.periodStart!, periodEnd: v.periodEnd!, totalAmount: Number(v.totalAmount), notes: v.notes || undefined };
  }
  protected mapRow(dto: PayrollRunDto): Record<string, unknown> {
    return { ...dto, journalEntryId: dto.journalEntryId, totalAmount: Number(dto.totalAmount).toLocaleString(undefined, { minimumFractionDigits: 2 }) };
  }
}
