import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { PmoIssueDto, PmoIssueForm, PmoMilestoneDto, PmoMilestoneForm, PmoRiskDto, PmoRiskForm, ProjectDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

type PmoTab = 'milestones' | 'risks' | 'issues';

@Component({
  standalone: false,
  selector: 'app-pmo-page',
  templateUrl: './pmo-page.component.html',
  styleUrls: ['./pmo-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PmoPageComponent implements OnInit {
  projects: ProjectDto[] = [];
  selectedProjectId: number | null = null;
  activeTab: PmoTab = 'milestones';
  loading = false;
  formVisible = false;
  editingId: number | null = null;
  selectedAuditRecord: { createdAt?: string; updatedAt?: string; createdBy?: string; updatedBy?: string } | null = null;

  milestones: PmoMilestoneDto[] = [];
  risks: PmoRiskDto[] = [];
  issues: PmoIssueDto[] = [];

  readonly tableActions: DataTableAction[] = MASTER_CRUD_ACTIONS;
  readonly milestoneColumns: DataTableColumn[] = [
    { key: 'title', title: 'COMMON.TITLE' },
    { key: 'dueDate', title: 'COMMON.DUE_DATE', kind: 'date' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];
  readonly riskColumns: DataTableColumn[] = [
    { key: 'title', title: 'COMMON.TITLE' },
    { key: 'severity', title: 'PMO.SEVERITY', kind: 'status' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];
  readonly issueColumns: DataTableColumn[] = [
    { key: 'title', title: 'COMMON.TITLE' },
    { key: 'ownerName', title: 'PMO.OWNER' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];

  readonly milestoneForm = this.fb.group({ title: ['', Validators.required], dueDate: [''], status: ['PLANNED', Validators.required], sortOrder: [0] });
  readonly riskForm = this.fb.group({ title: ['', Validators.required], severity: ['MEDIUM', Validators.required], status: ['OPEN', Validators.required], mitigation: [''] });
  readonly issueForm = this.fb.group({ title: ['', Validators.required], status: ['OPEN', Validators.required], ownerName: [''], notes: [''] });

  constructor(private api: ErpApiService, private fb: FormBuilder, private confirmDialog: ConfirmDialogService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.api.getProjects().subscribe({ next: (rows) => { this.projects = rows; this.cdr.markForCheck(); } });
  }

  get rows(): Record<string, unknown>[] {
    if (this.activeTab === 'milestones') return this.milestones.map((r) => ({ ...r }));
    if (this.activeTab === 'risks') return this.risks.map((r) => ({ ...r }));
    return this.issues.map((r) => ({ ...r }));
  }

  get columns(): DataTableColumn[] {
    if (this.activeTab === 'milestones') return this.milestoneColumns;
    if (this.activeTab === 'risks') return this.riskColumns;
    return this.issueColumns;
  }

  get projectLovItems(): Array<{ id: number; label: string }> {
    return (this.projects || []).map((p) => ({
      id: p.id,
      label: `${p.projectCode || p.id} - ${p.nameEn || p.nameAr || ''}`
    }));
  }

  onProjectChange(value: number | string | null): void {
    this.selectedProjectId = value != null && value !== '' ? Number(value) : null;
    this.loadTab();
  }

  setTab(tab: PmoTab): void {
    this.activeTab = tab;
    this.closeForm();
    this.loadTab();
  }

  openCreate(): void {
    if (!this.selectedProjectId) return;
    this.editingId = null;
    this.selectedAuditRecord = null;
    this.resetForm();
    this.formVisible = true;
    this.cdr.markForCheck();
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
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
    if (event.actionId === 'delete' && id && this.selectedProjectId) {
      this.confirmDialog.confirmByKey({ messageKey: 'COMMON.DELETE_CONFIRM' }).subscribe((ok) => {
        if (!ok) return;
        const projectId = this.selectedProjectId!;
        const req = this.activeTab === 'milestones' ? this.api.deletePmoMilestone(projectId, id)
          : this.activeTab === 'risks' ? this.api.deletePmoRisk(projectId, id)
          : this.api.deletePmoIssue(projectId, id);
        req.subscribe({ next: () => this.loadTab() });
      });
    }
  }

  save(): void {
    if (!this.selectedProjectId) return;
    const form = this.activeTab === 'milestones' ? this.milestoneForm : this.activeTab === 'risks' ? this.riskForm : this.issueForm;
    if (form.invalid) { form.markAllAsTouched(); return; }
    const projectId = this.selectedProjectId;
    const payload = form.getRawValue();
    let req;
    if (this.activeTab === 'milestones') {
      req = this.editingId ? this.api.updatePmoMilestone(projectId, this.editingId, payload as PmoMilestoneForm) : this.api.createPmoMilestone(projectId, payload as PmoMilestoneForm);
    } else if (this.activeTab === 'risks') {
      req = this.editingId ? this.api.updatePmoRisk(projectId, this.editingId, payload as PmoRiskForm) : this.api.createPmoRisk(projectId, payload as PmoRiskForm);
    } else {
      req = this.editingId ? this.api.updatePmoIssue(projectId, this.editingId, payload as PmoIssueForm) : this.api.createPmoIssue(projectId, payload as PmoIssueForm);
    }
    req.subscribe({ next: () => { this.closeForm(); this.loadTab(); } });
  }

  closeForm(): void { this.formVisible = false; this.editingId = null; this.selectedAuditRecord = null; this.cdr.markForCheck(); }

  private loadTab(): void {
    if (!this.selectedProjectId) {
      this.milestones = []; this.risks = []; this.issues = [];
      this.cdr.markForCheck();
      return;
    }
    this.loading = true;
    this.cdr.markForCheck();
    const done = () => { this.loading = false; this.cdr.markForCheck(); };
    const projectId = this.selectedProjectId;
    if (this.activeTab === 'milestones') {
      this.api.getPmoMilestones(projectId).subscribe({ next: (rows) => { this.milestones = rows; done(); }, error: () => done() });
    } else if (this.activeTab === 'risks') {
      this.api.getPmoRisks(projectId).subscribe({ next: (rows) => { this.risks = rows; done(); }, error: () => done() });
    } else {
      this.api.getPmoIssues(projectId).subscribe({ next: (rows) => { this.issues = rows; done(); }, error: () => done() });
    }
  }

  private resetForm(): void {
    if (this.activeTab === 'milestones') this.milestoneForm.reset({ title: '', dueDate: '', status: 'PLANNED', sortOrder: 0 });
    else if (this.activeTab === 'risks') this.riskForm.reset({ title: '', severity: 'MEDIUM', status: 'OPEN', mitigation: '' });
    else this.issueForm.reset({ title: '', status: 'OPEN', ownerName: '', notes: '' });
  }

  private patchForm(row: Record<string, unknown>): void {
    if (this.activeTab === 'milestones') {
      this.milestoneForm.patchValue({ title: String(row['title'] || ''), dueDate: String(row['dueDate'] || ''), status: String(row['status'] || 'PLANNED'), sortOrder: Number(row['sortOrder'] || 0) });
    } else if (this.activeTab === 'risks') {
      this.riskForm.patchValue({ title: String(row['title'] || ''), severity: String(row['severity'] || 'MEDIUM'), status: String(row['status'] || 'OPEN'), mitigation: String(row['mitigation'] || '') });
    } else {
      this.issueForm.patchValue({ title: String(row['title'] || ''), status: String(row['status'] || 'OPEN'), ownerName: String(row['ownerName'] || ''), notes: String(row['notes'] || '') });
    }
  }
}
