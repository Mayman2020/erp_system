import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { AttendanceRecordDto, AttendanceRecordForm, EmployeeDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-attendance-page',
  templateUrl: './attendance-page.component.html',
  styleUrls: ['./attendance-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AttendancePageComponent extends ErpMasterPageBase<AttendanceRecordDto, AttendanceRecordForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = { titleKey: 'MENU.ATTENDANCE', createKey: 'ERP.CREATE_ATTENDANCE', editKey: 'COMMON.EDIT', viewKey: 'COMMON.VIEW', showDateRange: true };
  readonly columns: DataTableColumn[] = [
    { key: 'attendanceDate', title: 'COMMON.DATE', kind: 'date' },
    { key: 'employeeId', title: 'MENU.EMPLOYEES' },
    { key: 'checkIn', title: 'ERP.CHECK_IN' },
    { key: 'checkOut', title: 'ERP.CHECK_OUT' },
    { key: 'status', title: 'COMMON.STATUS' }
  ];
  readonly form = this.fb.group({
    employeeId: [null as number | null, Validators.required],
    attendanceDate: [new Date().toISOString().slice(0, 10), Validators.required],
    checkIn: [''],
    checkOut: [''],
    status: ['PRESENT'],
    notes: ['']
  });
  employees: EmployeeDto[] = [];

  constructor(private api: ErpApiService, private fb: FormBuilder, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  get employeeOptions() {
    return [{ id: null, label: '—' }, ...(this.employees || []).map((e) => ({ id: e.id, label: `${e.employeeCode} - ${e.fullNameEn}` }))];
  }

  ngOnInit(): void {
    this.api.getEmployees().subscribe({ next: (rows) => { this.employees = rows || []; this.initMasterPage(); } });
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  protected fetchList(f: Record<string, string>): Observable<AttendanceRecordDto[]> { return this.api.getAttendanceRecords(f); }
  protected fetchOne(id: number): Observable<AttendanceRecordDto> { return this.api.getAttendanceRecord(id); }
  protected createItem(p: AttendanceRecordForm): Observable<AttendanceRecordDto> { return this.api.createAttendanceRecord(p); }
  protected updateItem(id: number, p: AttendanceRecordForm): Observable<AttendanceRecordDto> { return this.api.updateAttendanceRecord(id, p); }
  protected removeItem(id: number): Observable<void> { return this.api.deleteAttendanceRecord(id); }
  protected defaultFormValues(): Record<string, unknown> {
    return { employeeId: null, attendanceDate: new Date().toISOString().slice(0, 10), checkIn: '', checkOut: '', status: 'PRESENT', notes: '' };
  }
  protected patchForm(dto: AttendanceRecordDto): void {
    this.form.patchValue({ employeeId: dto.employeeId, attendanceDate: dto.attendanceDate, checkIn: dto.checkIn || '', checkOut: dto.checkOut || '', status: dto.status || 'PRESENT', notes: dto.notes || '' });
  }
  protected toPayload(): AttendanceRecordForm {
    const v = this.form.getRawValue();
    return { employeeId: Number(v.employeeId), attendanceDate: v.attendanceDate!, checkIn: v.checkIn || undefined, checkOut: v.checkOut || undefined, status: v.status || undefined, notes: v.notes || undefined };
  }
  protected mapRow(dto: AttendanceRecordDto): Record<string, unknown> { return { ...dto }; }
}
