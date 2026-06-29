import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { EmployeeDto, ProjectExpenseDto, ProjectMemberDto, ProjectTaskDto } from '../../core/models/erp.models';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';

@Component({
  standalone: false,
  selector: 'app-project-detail-panel',
  templateUrl: './project-detail-panel.component.html',
  styleUrls: ['./project-detail-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectDetailPanelComponent implements OnChanges {
  @Input() projectId!: number;
  @Output() closed = new EventEmitter<void>();

  loading = false;
  tasks: ProjectTaskDto[] = [];
  members: ProjectMemberDto[] = [];
  expenses: ProjectExpenseDto[] = [];
  employees: EmployeeDto[] = [];
  activeTab: 'tasks' | 'members' | 'expenses' = 'tasks';
  actorEmail = 'system@erp.local';
  editingTaskId: number | null = null;

  readonly taskForm = this.fb.group({ title: ['', Validators.required], description: [''], dueDate: [''], status: ['OPEN'] });
  readonly memberForm = this.fb.group({ employeeId: [null as number | null, Validators.required], role: ['MEMBER'] });
  readonly expenseForm = this.fb.group({ expenseDate: [new Date().toISOString().slice(0, 10), Validators.required], description: [''], amount: [0, Validators.required] });

  constructor(
    private api: ErpApiService,
    private fb: FormBuilder,
    private authService: AuthService,
    private confirmDialog: ConfirmDialogService,
    private cdr: ChangeDetectorRef
  ) {
    this.authService.currentUser$.subscribe((u) => {
      this.actorEmail = u?.email || u?.username || 'system@erp.local';
    });
  }

  get employeeOptions() {
    return [{ id: null, label: '—' }, ...this.employees.map((e) => ({ id: e.id, label: `${e.employeeCode} - ${e.fullNameEn}` }))];
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projectId'] && this.projectId) {
      this.api.getEmployees().subscribe({ next: (e) => { this.employees = e || []; this.cdr.markForCheck(); } });
      this.load();
    }
  }

  load(): void {
    this.loading = true;
    this.api.getProjectTasks(this.projectId).subscribe({ next: (t) => { this.tasks = t || []; this.cdr.markForCheck(); } });
    this.api.getProjectMembers(this.projectId).subscribe({ next: (m) => { this.members = m || []; this.cdr.markForCheck(); } });
    this.api.getProjectExpenses(this.projectId).subscribe({
      next: (e) => { this.expenses = e || []; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  addTask(): void {
    if (this.taskForm.invalid) return;
    const v = this.taskForm.getRawValue();
    if (this.editingTaskId) {
      this.api.updateProjectTask(this.editingTaskId, { projectId: this.projectId, title: v.title!, description: v.description || undefined, dueDate: v.dueDate || undefined, status: v.status || undefined }).subscribe({
        next: () => { this.editingTaskId = null; this.taskForm.reset({ title: '', description: '', dueDate: '', status: 'OPEN' }); this.load(); }
      });
      return;
    }
    this.api.createProjectTask({ projectId: this.projectId, title: v.title!, description: v.description || undefined, dueDate: v.dueDate || undefined, status: v.status || undefined }).subscribe({
      next: () => { this.taskForm.reset({ title: '', description: '', dueDate: '', status: 'OPEN' }); this.load(); }
    });
  }

  editTask(task: ProjectTaskDto): void {
    this.editingTaskId = task.id;
    this.taskForm.patchValue({ title: task.title, description: task.description || '', dueDate: task.dueDate || '', status: task.status || 'OPEN' });
    this.cdr.markForCheck();
  }

  deleteTask(id: number): void {
    this.confirmDialog.confirmByKey({ messageKey: 'COMMON.DELETE_CONFIRM' }).subscribe((ok) => {
      if (!ok) return;
      this.api.deleteProjectTask(id).subscribe({ next: () => this.load() });
    });
  }

  addMember(): void {
    if (this.memberForm.invalid) return;
    const v = this.memberForm.getRawValue();
    this.api.createProjectMember({ projectId: this.projectId, employeeId: Number(v.employeeId), role: v.role || undefined }).subscribe({
      next: () => { this.memberForm.reset({ employeeId: null, role: 'MEMBER' }); this.load(); }
    });
  }

  removeMember(id: number): void {
    this.api.deleteProjectMember(id).subscribe({ next: () => this.load() });
  }

  addExpense(): void {
    if (this.expenseForm.invalid) return;
    const v = this.expenseForm.getRawValue();
    this.api.createProjectExpense({ projectId: this.projectId, expenseDate: v.expenseDate!, description: v.description || undefined, amount: Number(v.amount) }).subscribe({
      next: () => { this.expenseForm.reset({ expenseDate: new Date().toISOString().slice(0, 10), description: '', amount: 0 }); this.load(); }
    });
  }

  approveExpense(id: number): void {
    this.api.approveProjectExpense(id, this.actorEmail).subscribe({ next: () => this.load() });
  }

  cancelExpense(id: number): void {
    this.api.cancelProjectExpense(id, this.actorEmail).subscribe({ next: () => this.load() });
  }

  deleteExpense(id: number): void {
    this.api.deleteProjectExpense(id).subscribe({ next: () => this.load() });
  }

  employeeLabel(employeeId?: number): string {
    const emp = this.employees.find((e) => e.id === employeeId);
    return emp ? `${emp.employeeCode} - ${emp.fullNameEn}` : employeeId ? `#${employeeId}` : '—';
  }
}
