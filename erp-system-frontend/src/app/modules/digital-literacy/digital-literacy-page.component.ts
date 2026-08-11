import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DigitalCourseDto, DigitalCourseForm, DigitalEnrollmentDto, DigitalEnrollmentForm, EmployeeDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

type DigitalTab = 'courses' | 'enrollments';

@Component({
  standalone: false,
  selector: 'app-digital-literacy-page',
  templateUrl: './digital-literacy-page.component.html',
  styleUrls: ['./digital-literacy-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DigitalLiteracyPageComponent implements OnInit {
  activeTab: DigitalTab = 'courses';
  loading = false;
  formVisible = false;
  editingId: number | null = null;
  selectedAuditRecord: { createdAt?: string; updatedAt?: string; createdBy?: string; updatedBy?: string } | null = null;
  courses: DigitalCourseDto[] = [];
  enrollments: DigitalEnrollmentDto[] = [];
  employees: EmployeeDto[] = [];
  readonly tableActions: DataTableAction[] = [
    ...MASTER_CRUD_ACTIONS.slice(0, 2),
    { id: 'progress', labelKey: 'DIGITAL.UPDATE_PROGRESS', className: 'erp-action-info' },
    MASTER_CRUD_ACTIONS[2]
  ];
  readonly courseColumns: DataTableColumn[] = [
    { key: 'code', title: 'ERP.CODE' },
    { key: 'title', title: 'COMMON.TITLE' },
    { key: 'active', title: 'COMMON.ACTIVE', kind: 'boolean' }
  ];
  readonly enrollmentColumns: DataTableColumn[] = [
    { key: 'courseId', title: 'DIGITAL.COURSES' },
    { key: 'employeeId', title: 'MENU.EMPLOYEES' },
    { key: 'progressPct', title: 'DIGITAL.PROGRESS', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'certificateNo', title: 'DIGITAL.CERTIFICATE' }
  ];
  readonly courseForm = this.fb.group({ code: ['', Validators.required], title: ['', Validators.required], description: [''], active: [true] });
  readonly enrollmentForm = this.fb.group({ courseId: [null as number | null, Validators.required], employeeId: [null as number | null, Validators.required], progressPct: [0], score: [null as number | null], status: ['ENROLLED'] });

  constructor(private api: ErpApiService, private fb: FormBuilder, private confirmDialog: ConfirmDialogService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.api.getEmployees().subscribe({ next: (rows) => { this.employees = rows; this.cdr.markForCheck(); } });
    this.loadTab();
  }

  get rows(): Record<string, unknown>[] {
    return this.activeTab === 'courses' ? this.courses.map((r) => ({ ...r })) : this.enrollments.map((r) => ({ ...r }));
  }

  get columns(): DataTableColumn[] { return this.activeTab === 'courses' ? this.courseColumns : this.enrollmentColumns; }
  get actions(): DataTableAction[] { return this.activeTab === 'courses' ? MASTER_CRUD_ACTIONS : this.tableActions; }

  get courseLovItems(): Array<{ id: number; label: string }> {
    return (this.courses || []).map((c) => ({ id: c.id, label: `${c.code} - ${c.title}` }));
  }

  get employeeLovItems(): Array<{ id: number; label: string }> {
    return (this.employees || []).map((e) => ({
      id: e.id,
      label: `${e.employeeCode} - ${e.fullNameEn || e.fullNameAr || e.email || e.id}`
    }));
  }

  setTab(tab: DigitalTab): void { this.activeTab = tab; this.closeForm(); this.loadTab(); }
  openCreate(): void { this.editingId = null; this.selectedAuditRecord = null; this.resetForm(); this.formVisible = true; this.cdr.markForCheck(); }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'progress' && id) {
      this.api.updateDigitalEnrollmentProgress(id, 100).subscribe({ next: () => this.loadTab() });
      return;
    }
    if ((event.actionId === 'view' || event.actionId === 'edit') && id) {
      this.editingId = id;
      this.selectedAuditRecord = {
        createdAt: event.row['createdAt'] != null ? String(event.row['createdAt']) : undefined,
        updatedAt: event.row['updatedAt'] != null ? String(event.row['updatedAt']) : undefined,
        createdBy: event.row['createdBy'] != null ? String(event.row['createdBy']) : undefined,
        updatedBy: event.row['updatedBy'] != null ? String(event.row['updatedBy']) : undefined
      };
      this.patchForm(event.row);
      this.formVisible = true;
      this.cdr.markForCheck();
      return;
    }
    if (event.actionId === 'delete' && id) {
      this.confirmDialog.confirmByKey({ messageKey: 'COMMON.DELETE_CONFIRM' }).subscribe((ok) => {
        if (!ok) return;
        const req = this.activeTab === 'courses' ? this.api.deleteDigitalCourse(id) : this.api.deleteDigitalEnrollment(id);
        req.subscribe({ next: () => this.loadTab() });
      });
    }
  }

  save(): void {
    const form = this.activeTab === 'courses' ? this.courseForm : this.enrollmentForm;
    if (form.invalid) { form.markAllAsTouched(); return; }
    const payload = form.getRawValue();
    let req;
    if (this.activeTab === 'courses') {
      req = this.editingId ? this.api.updateDigitalCourse(this.editingId, payload as DigitalCourseForm) : this.api.createDigitalCourse(payload as DigitalCourseForm);
    } else {
      req = this.editingId ? this.api.updateDigitalEnrollment(this.editingId, payload as DigitalEnrollmentForm) : this.api.createDigitalEnrollment(payload as DigitalEnrollmentForm);
    }
    req.subscribe({ next: () => { this.closeForm(); this.loadTab(); } });
  }

  closeForm(): void { this.formVisible = false; this.editingId = null; this.selectedAuditRecord = null; this.cdr.markForCheck(); }

  private loadTab(): void {
    this.loading = true;
    this.cdr.markForCheck();
    const done = () => { this.loading = false; this.cdr.markForCheck(); };
    if (this.activeTab === 'courses') this.api.getDigitalCourses().subscribe({ next: (rows) => { this.courses = rows; done(); }, error: () => done() });
    else {
      this.api.getDigitalEnrollments().subscribe({ next: (rows) => { this.enrollments = rows; done(); }, error: () => done() });
      if (!this.courses.length) {
        this.api.getDigitalCourses().subscribe({ next: (rows) => { this.courses = rows; this.cdr.markForCheck(); } });
      }
    }
  }

  private resetForm(): void {
    if (this.activeTab === 'courses') this.courseForm.reset({ code: '', title: '', description: '', active: true });
    else this.enrollmentForm.reset({ courseId: null, employeeId: null, progressPct: 0, score: null, status: 'ENROLLED' });
  }

  private patchForm(row: Record<string, unknown>): void {
    if (this.activeTab === 'courses') {
      this.courseForm.patchValue({ code: String(row['code'] || ''), title: String(row['title'] || ''), description: String(row['description'] || ''), active: !!row['active'] });
    } else {
      this.enrollmentForm.patchValue({ courseId: Number(row['courseId']), employeeId: Number(row['employeeId']), progressPct: Number(row['progressPct'] || 0), score: row['score'] == null ? null : Number(row['score']), status: String(row['status'] || 'ENROLLED') });
    }
  }
}
