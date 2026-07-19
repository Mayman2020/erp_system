import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { AdminApiService } from '../core/services/admin-api.service';
import { AdminUser } from '../core/models/admin.models';
import { TranslationService } from '../core/i18n/translation.service';
import { DataTableColumn } from '../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-user-access',
  templateUrl: './user-access.component.html',
  styleUrls: ['./user-access.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserAccessComponent implements OnInit {
  readonly columns: DataTableColumn[] = [
    { key: 'title', title: 'ACCESS_MANAGEMENT.SCREEN', align: 'start' },
    { key: 'canView', title: 'ACCESS_MANAGEMENT.CAN_VIEW', kind: 'boolean' },
    { key: 'canCreate', title: 'ACCESS_MANAGEMENT.CAN_CREATE', kind: 'boolean' },
    { key: 'canEdit', title: 'ACCESS_MANAGEMENT.CAN_EDIT', kind: 'boolean' },
    { key: 'canDelete', title: 'ACCESS_MANAGEMENT.CAN_DELETE', kind: 'boolean' }
  ];

  users: AdminUser[] = [];
  selectedUserId: number | null = null;
  rows: Array<Record<string, unknown>> = [];
  loadingUsers = false;
  loadingPermissions = false;
  errorKey = '';

  constructor(
    private adminApi: AdminApiService,
    private translationService: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  get selectedUser(): AdminUser | null {
    return this.users.find((u) => u.id === this.selectedUserId) || null;
  }

  ngOnInit(): void {
    this.loadingUsers = true;
    this.adminApi.getUsers().pipe(
      finalize(() => {
        this.loadingUsers = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (users) => { this.users = users || []; },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
    });
  }

  onSelectUser(userId: number | string | null): void {
    this.selectedUserId = userId ? Number(userId) : null;
    this.rows = [];
    if (!this.selectedUserId) {
      this.cdr.markForCheck();
      return;
    }
    this.loadingPermissions = true;
    this.errorKey = '';
    this.adminApi.getEffectivePermissions(this.selectedUserId).pipe(
      finalize(() => {
        this.loadingPermissions = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (permissions) => {
        this.rows = (permissions || []).map((p) => ({
          ...p,
          title: this.translationService.instant(p.titleKey) || p.titleKey
        }));
      },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
    });
  }
}
