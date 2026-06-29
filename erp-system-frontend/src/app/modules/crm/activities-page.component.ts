import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { CrmActivityDto, CrmActivityForm } from '../../core/models/erp.models';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-activities-page',
  templateUrl: './activities-page.component.html',
  styleUrls: ['./activities-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActivitiesPageComponent extends ErpMasterPageBase<CrmActivityDto, CrmActivityForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'MENU.ACTIVITIES', createKey: 'ERP.CREATE_ACTIVITY', editKey: 'COMMON.EDIT', viewKey: 'COMMON.VIEW',
    showStatus: true, statusOptions: ['PLANNED', 'COMPLETED', 'CANCELLED']
  };
  readonly columns: DataTableColumn[] = [
    { key: 'activityType', title: 'ERP.ACTIVITY_TYPE' },
    { key: 'subject', title: 'ERP.SUBJECT' },
    { key: 'activityDate', title: 'COMMON.DATE', kind: 'date' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' }
  ];
  readonly form = this.fb.group({
    activityType: ['CALL', Validators.required],
    subject: ['', Validators.required],
    leadId: [null as number | null],
    activityDate: [new Date().toISOString().slice(0, 10), Validators.required],
    status: ['PLANNED', Validators.required],
    notes: ['']
  });

  constructor(private api: ErpApiService, private fb: FormBuilder, authService: AuthService, confirmDialog: ConfirmDialogService, cdr: ChangeDetectorRef) {
    super(authService, confirmDialog, cdr);
  }

  ngOnInit(): void { this.initMasterPage(); }
  ngOnDestroy(): void { this.destroyMasterPage(); }

  protected fetchList(f: Record<string, string>): Observable<CrmActivityDto[]> { return this.api.getCrmActivities(f); }
  protected fetchOne(id: number): Observable<CrmActivityDto> { return this.api.getCrmActivity(id); }
  protected createItem(p: CrmActivityForm): Observable<CrmActivityDto> { return this.api.createCrmActivity(p); }
  protected updateItem(id: number, p: CrmActivityForm): Observable<CrmActivityDto> { return this.api.updateCrmActivity(id, p); }
  protected removeItem(id: number): Observable<void> { return this.api.deleteCrmActivity(id); }
  protected defaultFormValues(): Record<string, unknown> {
    return { activityType: 'CALL', subject: '', leadId: null, activityDate: new Date().toISOString().slice(0, 10), status: 'PLANNED', notes: '' };
  }
  protected patchForm(dto: CrmActivityDto): void {
    this.form.patchValue({ activityType: dto.activityType, subject: dto.subject, leadId: dto.leadId || null, activityDate: dto.activityDate, status: dto.status, notes: dto.notes || '' });
  }
  protected toPayload(): CrmActivityForm {
    const v = this.form.getRawValue();
    return { activityType: v.activityType!, subject: v.subject!, leadId: v.leadId || undefined, activityDate: v.activityDate!, status: v.status!, notes: v.notes || undefined };
  }
  protected mapRow(dto: CrmActivityDto): Record<string, unknown> { return { ...dto }; }
}
