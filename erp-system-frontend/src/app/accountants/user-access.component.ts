import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AdminApiService } from '../core/services/admin-api.service';
import { AdminRole, AdminRolePermission, AdminUser } from '../core/models/admin.models';
import { TranslationService } from '../core/i18n/translation.service';

@Component({
  standalone: false,
  selector: 'app-user-access',
  templateUrl: './user-access.component.html',
  styleUrls: ['./user-access.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserAccessComponent implements OnInit {
  users: AdminUser[] = [];
  roles: AdminRole[] = [];
  selectedUserId: number | null = null;
  extraRoleCodes: string[] = [];
  rows: Array<AdminRolePermission & { title: string }> = [];
  loadingUsers = false;
  loadingPermissions = false;
  saving = false;
  errorKey = '';
  successKey = '';

  readonly form = this.fb.nonNullable.group({
    primaryRoleCode: [''],
    extraRoleToAdd: ['']
  });

  constructor(
    private adminApi: AdminApiService,
    private translationService: TranslationService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  get selectedUser(): AdminUser | null {
    return this.users.find((u) => u.id === this.selectedUserId) || null;
  }

  get userOptions(): Array<{ id: number; label: string }> {
    return this.users.map((user) => ({
      id: user.id,
      label: `${user.fullName || user.username} (${user.username})`
    }));
  }

  get primaryRoleOptions(): Array<AdminRole & { label: string }> {
    return this.roles
      .filter((role) => role.active && ['ADMIN', 'ACCOUNTANT'].includes(role.code))
      .map((role) => ({ ...role, label: this.roleLabel(role) }));
  }

  get extraRoleOptions(): Array<AdminRole & { label: string }> {
    const primary = this.form.controls.primaryRoleCode.value;
    return this.roles
      .filter((role) => role.active && role.code !== primary && !this.extraRoleCodes.includes(role.code))
      .map((role) => ({ ...role, label: this.roleLabel(role) }));
  }

  ngOnInit(): void {
    this.loadingUsers = true;
    this.adminApi.getAccessContext().pipe(
      finalize(() => {
        this.loadingUsers = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (context) => {
        this.users = context.users || [];
        this.roles = context.roles || [];
      },
      error: () => { this.errorKey = 'COMMON.ERROR_LOADING'; }
    });
  }

  onSelectUser(userId: number | string | null): void {
    this.selectedUserId = userId ? Number(userId) : null;
    this.rows = [];
    this.successKey = '';
    if (!this.selectedUserId) {
      this.form.reset();
      this.extraRoleCodes = [];
      this.cdr.markForCheck();
      return;
    }
    const user = this.selectedUser;
    this.form.controls.primaryRoleCode.setValue(user?.primaryRole || '');
    this.form.controls.extraRoleToAdd.setValue('');
    this.extraRoleCodes = (user?.roleCodes || []).filter((code) => code !== user?.primaryRole);
    this.loadEffectivePermissions();
  }

  onExtraRoleSelected(roleCode: unknown): void {
    const code = String(roleCode || '').trim();
    if (code && !this.extraRoleCodes.includes(code) && code !== this.form.controls.primaryRoleCode.value) {
      this.extraRoleCodes = [...this.extraRoleCodes, code];
    }
    this.form.controls.extraRoleToAdd.setValue('');
    this.cdr.markForCheck();
  }

  onPrimaryRoleSelected(roleCode: unknown): void {
    const code = String(roleCode || '').trim();
    this.extraRoleCodes = this.extraRoleCodes.filter((item) => item !== code);
    this.cdr.markForCheck();
  }

  removeExtraRole(roleCode: string): void {
    this.extraRoleCodes = this.extraRoleCodes.filter((code) => code !== roleCode);
  }

  save(): void {
    if (!this.selectedUserId || !this.form.controls.primaryRoleCode.value || this.saving) {
      return;
    }
    this.saving = true;
    this.errorKey = '';
    this.successKey = '';
    const primaryRoleCode = this.form.controls.primaryRoleCode.value;
    this.adminApi.updateUserRoles(this.selectedUserId, {
      primaryRoleCode,
      extraRoleCodes: this.extraRoleCodes.filter((code) => code !== primaryRoleCode)
    }).pipe(
      finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (updated) => {
        this.users = this.users.map((user) => user.id === updated.id ? updated : user);
        this.extraRoleCodes = (updated.roleCodes || []).filter((code) => code !== updated.primaryRole);
        this.successKey = 'ACCESS_MANAGEMENT.USER_ROLES_UPDATE_SUCCESS';
        this.loadEffectivePermissions();
      },
      error: () => { this.errorKey = 'ACCESS_MANAGEMENT.USER_ROLES_UPDATE_ERROR'; }
    });
  }

  roleName(roleCode: string): string {
    const role = this.roles.find((item) => item.code === roleCode);
    return role ? this.roleLabel(role) : roleCode;
  }

  grantedActions(row: AdminRolePermission): string[] {
    return [
      row.canView ? 'ACCESS_MANAGEMENT.CAN_VIEW' : '',
      row.canCreate ? 'ACCESS_MANAGEMENT.CAN_CREATE' : '',
      row.canEdit ? 'ACCESS_MANAGEMENT.CAN_EDIT' : '',
      row.canDelete ? 'ACCESS_MANAGEMENT.CAN_DELETE' : ''
    ].filter(Boolean);
  }

  private loadEffectivePermissions(): void {
    if (!this.selectedUserId) {
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

  private roleLabel(role: AdminRole): string {
    const localizedName = this.translationService.currentLanguage === 'ar'
      ? (role.nameAr || role.nameEn)
      : role.nameEn;
    return `${role.code} — ${localizedName}`;
  }
}
