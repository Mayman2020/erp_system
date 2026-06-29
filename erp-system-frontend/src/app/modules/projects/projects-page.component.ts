import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { ProjectDto, ProjectForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig, MASTER_CRUD_ACTIONS } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectsPageComponent extends ErpMasterPageBase<ProjectDto, ProjectForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.PROJECTS', createKey: 'ERP.CREATE_PROJECT', editKey: 'COMMON.EDIT', viewKey: 'COMMON.VIEW',
    showStatus: true, statusOptions: ['PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED']
  };
  readonly columns: DataTableColumn[] = [
    { key: 'projectCode', title: 'ERP.CODE' },
    { key: 'nameEn', title: 'COMMON.NAME' },
    { key: 'startDate', title: 'COMMON.FROM_DATE', kind: 'date' },
    { key: 'endDate', title: 'COMMON.TO_DATE', kind: 'date' },
    { key: 'budget', title: 'ERP.BUDGET', align: 'end' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status', prefix: 'PROJECT_STATUS.' }
  ];
  readonly form = this.fb.group({
    projectCode: ['', Validators.required],
    nameEn: ['', Validators.required],
    nameAr: [''],
    startDate: [''],
    endDate: [''],
    budget: [0, Validators.required],
    status: ['PLANNED', Validators.required],
    description: ['']
  });
  selectedProjectId: number | null = null;
  detailVisible = false;

  constructor(private api: ErpApiService, private fb: FormBuilder, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  ngOnInit(): void { this.initMasterPage(); }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  get tableActions(): DataTableAction[] {
    return [
      { id: 'detail', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' },
      ...MASTER_CRUD_ACTIONS.slice(1)
    ];
  }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'detail' && id) {
      this.selectedProjectId = id;
      this.detailVisible = true;
      this.cdr.markForCheck();
      return;
    }
    super.onTableAction(event);
  }

  closeDetail(): void {
    this.detailVisible = false;
    this.selectedProjectId = null;
    this.cdr.markForCheck();
  }

  protected fetchList(f: Record<string, string>): Observable<ProjectDto[]> { return this.api.getProjects(f); }
  protected fetchOne(id: number): Observable<ProjectDto> { return this.api.getProject(id); }
  protected createItem(p: ProjectForm): Observable<ProjectDto> { return this.api.createProject(p); }
  protected updateItem(id: number, p: ProjectForm): Observable<ProjectDto> { return this.api.updateProject(id, p); }
  protected removeItem(id: number): Observable<void> { return this.api.deleteProject(id); }
  protected defaultFormValues(): Record<string, unknown> {
    return { projectCode: '', nameEn: '', nameAr: '', startDate: '', endDate: '', budget: 0, status: 'PLANNED', description: '' };
  }
  protected patchForm(dto: ProjectDto): void {
    this.form.patchValue({ projectCode: dto.projectCode, nameEn: dto.nameEn, nameAr: dto.nameAr || '', startDate: dto.startDate || '', endDate: dto.endDate || '', budget: dto.budget || 0, status: dto.status, description: dto.description || '' });
  }
  protected toPayload(): ProjectForm {
    const v = this.form.getRawValue();
    return { projectCode: v.projectCode!, nameEn: v.nameEn!, nameAr: v.nameAr || undefined, startDate: v.startDate || undefined, endDate: v.endDate || undefined, budget: Number(v.budget), status: v.status!, description: v.description || undefined };
  }
  protected mapRow(dto: ProjectDto): Record<string, unknown> {
    return { ...dto, budget: Number(dto.budget).toLocaleString(undefined, { minimumFractionDigits: 2 }) };
  }
}
