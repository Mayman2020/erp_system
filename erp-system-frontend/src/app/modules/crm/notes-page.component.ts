import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { CrmLeadDto, CrmNoteDto, CrmNoteForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-notes-page',
  templateUrl: './notes-page.component.html',
  styleUrls: ['./notes-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotesPageComponent extends ErpMasterPageBase<CrmNoteDto, CrmNoteForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = { titleKey: 'MENU.CRM_NOTES', createKey: 'ERP.CREATE_NOTE', editKey: 'COMMON.EDIT', viewKey: 'COMMON.VIEW' };
  readonly columns: DataTableColumn[] = [
    { key: 'entityType', title: 'ERP.ENTITY_TYPE' },
    { key: 'entityId', title: 'COMMON.ID' },
    { key: 'content', title: 'ERP.NOTE_CONTENT' },
    { key: 'createdBy', title: 'ERP.CREATED_BY' },
    { key: 'createdAt', title: 'COMMON.DATE', kind: 'date' }
  ];
  readonly form = this.fb.group({
    entityType: ['LEAD', Validators.required],
    entityId: [null as number | null, Validators.required],
    content: ['', Validators.required]
  });

  leads: CrmLeadDto[] = [];

  constructor(private api: ErpApiService, private fb: FormBuilder, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  ngOnInit(): void {
    this.api.getLeads().subscribe({ next: (leads) => { this.leads = leads || []; this.cdr.markForCheck(); } });
    this.initMasterPage();
  }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  get leadOptions() {
    return [{ id: null, label: '—' }, ...this.leads.map((l) => ({ id: l.id, label: `${l.leadNumber} - ${l.name}` }))];
  }

  protected fetchList(f: Record<string, string>): Observable<CrmNoteDto[]> { return this.api.getCrmNotes(f); }
  protected fetchOne(id: number): Observable<CrmNoteDto> { return this.api.getCrmNote(id); }
  protected createItem(p: CrmNoteForm): Observable<CrmNoteDto> { return this.api.createCrmNote(p); }
  protected updateItem(id: number, p: CrmNoteForm): Observable<CrmNoteDto> { return this.api.updateCrmNote(id, p); }
  protected removeItem(id: number): Observable<void> { return this.api.deleteCrmNote(id); }
  protected defaultFormValues(): Record<string, unknown> { return { entityType: 'LEAD', entityId: null, content: '' }; }
  protected patchForm(dto: CrmNoteDto): void { this.form.patchValue({ entityType: dto.entityType, entityId: dto.entityId, content: dto.content }); }
  protected toPayload(): CrmNoteForm {
    const v = this.form.getRawValue();
    return { entityType: v.entityType!, entityId: Number(v.entityId), content: v.content! };
  }
  protected mapRow(dto: CrmNoteDto): Record<string, unknown> { return { ...dto }; }
}
