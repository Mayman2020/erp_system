import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { EmployeeDocumentDto, EmployeeDocumentForm, EmployeeDto } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-documents-page',
  templateUrl: './documents-page.component.html',
  styleUrls: ['./documents-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocumentsPageComponent extends ErpMasterPageBase<EmployeeDocumentDto, EmployeeDocumentForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.EMPLOYEE_DOCUMENTS',
    createKey: 'ERP.CREATE_DOCUMENT',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW'
  };

  readonly columns: DataTableColumn[] = [
    { key: 'employeeLabel', title: 'MENU.EMPLOYEES' },
    { key: 'documentType', title: 'ERP.DOCUMENT_TYPE' },
    { key: 'fileName', title: 'ERP.FILE_NAME' },
    { key: 'expiryDate', title: 'ERP.EXPIRY_DATE', kind: 'date' }
  ];

  readonly form = this.fb.group({
    employeeId: [null as number | null, Validators.required],
    documentType: ['', Validators.required],
    fileName: ['', Validators.required],
    filePath: [''],
    expiryDate: ['']
  });

  employees: EmployeeDto[] = [];
  filterEmployeeId: number | null = null;

  constructor(private api: ErpApiService, private fb: FormBuilder, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  get employeeOptions() {
    return [{ id: null, label: '—' }, ...this.employees.map((e) => ({ id: e.id, label: `${e.employeeCode} - ${e.fullNameEn}` }))];
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

  ngOnDestroy(): void {
    this.destroyMasterPage();
  }

  get tableActions() {
    return this.actions;
  }

  onEmployeeFilterChange(employeeId: number | null): void {
    this.filterEmployeeId = employeeId;
    this.load();
  }

  protected fetchList(_filters?: Record<string, string>): Observable<EmployeeDocumentDto[]> {
    return this.api.getEmployeeDocuments(this.filterEmployeeId || undefined);
  }

  protected fetchOne(id: number): Observable<EmployeeDocumentDto> {
    return this.api.getEmployeeDocument(id);
  }

  protected createItem(payload: EmployeeDocumentForm): Observable<EmployeeDocumentDto> {
    return this.api.createEmployeeDocument(payload);
  }

  protected updateItem(id: number, payload: EmployeeDocumentForm): Observable<EmployeeDocumentDto> {
    return this.api.updateEmployeeDocument(id, payload);
  }

  protected removeItem(id: number): Observable<void> {
    return this.api.deleteEmployeeDocument(id);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return { employeeId: this.filterEmployeeId, documentType: '', fileName: '', filePath: '', expiryDate: '' };
  }

  protected patchForm(dto: EmployeeDocumentDto): void {
    this.form.patchValue({
      employeeId: dto.employeeId,
      documentType: dto.documentType,
      fileName: dto.fileName,
      filePath: dto.filePath || '',
      expiryDate: dto.expiryDate || ''
    });
  }

  protected toPayload(): EmployeeDocumentForm {
    const v = this.form.getRawValue();
    return {
      employeeId: Number(v.employeeId),
      documentType: v.documentType!,
      fileName: v.fileName!,
      filePath: v.filePath || undefined,
      expiryDate: v.expiryDate || undefined
    };
  }

  protected mapRow(dto: EmployeeDocumentDto): Record<string, unknown> {
    const emp = this.employees.find((e) => e.id === dto.employeeId);
    return { ...dto, employeeLabel: emp ? `${emp.employeeCode} - ${emp.fullNameEn}` : `#${dto.employeeId}` };
  }
}
