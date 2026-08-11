import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HrCandidateDto, HrCandidateForm, HrInterviewDto, HrInterviewForm, HrVacancyDto, HrVacancyForm, LeaveBalanceDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

type RecruitmentTab = 'vacancies' | 'candidates' | 'interviews' | 'balances';

@Component({
  standalone: false,
  selector: 'app-recruitment-page',
  templateUrl: './recruitment-page.component.html',
  styleUrls: ['./recruitment-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecruitmentPageComponent implements OnInit {
  activeTab: RecruitmentTab = 'vacancies';
  loading = false;
  formVisible = false;
  formMode: 'create' | 'edit' = 'create';
  editingId: number | null = null;
  selectedAuditRecord: { createdAt?: string; updatedAt?: string; createdBy?: string; updatedBy?: string } | null = null;

  vacancies: HrVacancyDto[] = [];
  candidates: HrCandidateDto[] = [];
  interviews: HrInterviewDto[] = [];
  leaveBalances: LeaveBalanceDto[] = [];

  readonly vacancyColumns: DataTableColumn[] = [
    { key: 'title', title: 'COMMON.TITLE' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'openings', title: 'RECRUITMENT.OPENINGS', align: 'end' }
  ];
  readonly candidateColumns: DataTableColumn[] = [
    { key: 'fullName', title: 'COMMON.NAME' },
    { key: 'email', title: 'ERP.EMAIL' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'score', title: 'RECRUITMENT.SCORE', align: 'end' }
  ];
  readonly interviewColumns: DataTableColumn[] = [
    { key: 'candidateId', title: 'RECRUITMENT.CANDIDATES' },
    { key: 'scheduledAt', title: 'RECRUITMENT.SCHEDULED_AT', kind: 'date' },
    { key: 'interviewer', title: 'RECRUITMENT.INTERVIEWER' },
    { key: 'result', title: 'RECRUITMENT.RESULT' }
  ];
  readonly balanceColumns: DataTableColumn[] = [
    { key: 'employeeId', title: 'MENU.EMPLOYEES' },
    { key: 'leaveType', title: 'RECRUITMENT.LEAVE_TYPE' },
    { key: 'balanceDays', title: 'RECRUITMENT.BALANCE_DAYS', align: 'end' },
    { key: 'year', title: 'RECRUITMENT.YEAR', align: 'end' }
  ];
  readonly tableActions: DataTableAction[] = MASTER_CRUD_ACTIONS;

  readonly vacancyForm = this.fb.group({
    title: ['', Validators.required],
    departmentId: [null as number | null],
    status: ['OPEN', Validators.required],
    openings: [1, [Validators.required, Validators.min(1)]],
    description: ['']
  });
  readonly candidateForm = this.fb.group({
    fullName: ['', Validators.required],
    email: [''],
    phone: [''],
    vacancyId: [null as number | null],
    status: ['APPLIED', Validators.required],
    score: [null as number | null],
    notes: ['']
  });
  readonly interviewForm = this.fb.group({
    candidateId: [null as number | null, Validators.required],
    scheduledAt: ['', Validators.required],
    interviewer: [''],
    result: [''],
    notes: ['']
  });

  constructor(private api: ErpApiService, private fb: FormBuilder, private confirmDialog: ConfirmDialogService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadActiveTab(); }

  setTab(tab: RecruitmentTab): void {
    this.activeTab = tab;
    this.closeForm();
    this.loadActiveTab();
  }

  get rows(): Record<string, unknown>[] {
    if (this.activeTab === 'vacancies') return this.vacancies.map((row) => ({ ...row }));
    if (this.activeTab === 'candidates') return this.candidates.map((row) => ({ ...row }));
    if (this.activeTab === 'interviews') return this.interviews.map((row) => ({ ...row }));
    return this.leaveBalances.map((row) => ({ ...row }));
  }

  get columns(): DataTableColumn[] {
    if (this.activeTab === 'vacancies') return this.vacancyColumns;
    if (this.activeTab === 'candidates') return this.candidateColumns;
    if (this.activeTab === 'interviews') return this.interviewColumns;
    return this.balanceColumns;
  }

  get canMutate(): boolean { return this.activeTab !== 'balances'; }

  get candidateLovItems(): Array<{ id: number; label: string }> {
    return (this.candidates || []).map((c) => ({ id: c.id, label: c.fullName || `#${c.id}` }));
  }

  openCreate(): void {
    this.formMode = 'create';
    this.editingId = null;
    this.selectedAuditRecord = null;
    this.resetForm();
    this.formVisible = true;
    this.cdr.markForCheck();
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'view' || event.actionId === 'edit') {
      this.formMode = event.actionId === 'view' ? 'edit' : 'edit';
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
        const req = this.activeTab === 'vacancies' ? this.api.deleteVacancy(id)
          : this.activeTab === 'candidates' ? this.api.deleteCandidate(id)
          : this.api.deleteInterview(id);
        req.subscribe({ next: () => this.loadActiveTab() });
      });
    }
  }

  save(): void {
    const form = this.activeTab === 'vacancies' ? this.vacancyForm
      : this.activeTab === 'candidates' ? this.candidateForm
      : this.interviewForm;
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    const payload = form.getRawValue();
    let req;
    if (this.activeTab === 'vacancies') {
      const body = payload as HrVacancyForm;
      req = this.editingId ? this.api.updateVacancy(this.editingId, body) : this.api.createVacancy(body);
    } else if (this.activeTab === 'candidates') {
      const body = payload as HrCandidateForm;
      req = this.editingId ? this.api.updateCandidate(this.editingId, body) : this.api.createCandidate(body);
    } else {
      const body = payload as HrInterviewForm;
      req = this.editingId ? this.api.updateInterview(this.editingId, body) : this.api.createInterview(body);
    }
    req.subscribe({
      next: () => {
        this.closeForm();
        this.loadActiveTab();
      }
    });
  }

  closeForm(): void {
    this.formVisible = false;
    this.editingId = null;
    this.selectedAuditRecord = null;
    this.cdr.markForCheck();
  }

  private loadActiveTab(): void {
    this.loading = true;
    this.cdr.markForCheck();
    const done = () => { this.loading = false; this.cdr.markForCheck(); };
    if (this.activeTab === 'vacancies') {
      this.api.getVacancies().subscribe({ next: (rows) => { this.vacancies = rows; done(); }, error: () => done() });
      return;
    }
    if (this.activeTab === 'candidates') {
      this.api.getCandidates().subscribe({ next: (rows) => { this.candidates = rows; done(); }, error: () => done() });
      return;
    }
    if (this.activeTab === 'interviews') {
      this.api.getInterviews().subscribe({ next: (rows) => { this.interviews = rows; done(); }, error: () => done() });
      this.api.getCandidates().subscribe({ next: (rows) => { this.candidates = rows; this.cdr.markForCheck(); } });
      return;
    }
    this.api.getLeaveBalances().subscribe({ next: (rows) => { this.leaveBalances = rows; done(); }, error: () => done() });
  }

  private resetForm(): void {
    if (this.activeTab === 'vacancies') {
      this.vacancyForm.reset({ title: '', departmentId: null, status: 'OPEN', openings: 1, description: '' });
    } else if (this.activeTab === 'candidates') {
      this.candidateForm.reset({ fullName: '', email: '', phone: '', vacancyId: null, status: 'APPLIED', score: null, notes: '' });
    } else {
      this.interviewForm.reset({ candidateId: null, scheduledAt: '', interviewer: '', result: '', notes: '' });
    }
  }

  private patchForm(row: Record<string, unknown>): void {
    if (this.activeTab === 'vacancies') {
      this.vacancyForm.patchValue({
        title: String(row['title'] || ''),
        departmentId: row['departmentId'] == null ? null : Number(row['departmentId']),
        status: String(row['status'] || 'OPEN'),
        openings: Number(row['openings'] || 1),
        description: String(row['description'] || '')
      });
    } else if (this.activeTab === 'candidates') {
      this.candidateForm.patchValue({
        fullName: String(row['fullName'] || ''),
        email: String(row['email'] || ''),
        phone: String(row['phone'] || ''),
        vacancyId: row['vacancyId'] == null ? null : Number(row['vacancyId']),
        status: String(row['status'] || 'APPLIED'),
        score: row['score'] == null ? null : Number(row['score']),
        notes: String(row['notes'] || '')
      });
    } else {
      this.interviewForm.patchValue({
        candidateId: row['candidateId'] == null ? null : Number(row['candidateId']),
        scheduledAt: String(row['scheduledAt'] || '').slice(0, 10),
        interviewer: String(row['interviewer'] || ''),
        result: String(row['result'] || ''),
        notes: String(row['notes'] || '')
      });
    }
  }
}
