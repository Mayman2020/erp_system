import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, Observable, Subject } from 'rxjs';
import { distinctUntilChanged, filter, finalize, map, takeUntil } from 'rxjs/operators';
import {
  AdminAccessContext,
  AdminLookupType,
  AdminLookupTypeForm,
  AdminLookupValue,
  AdminLookupValueForm,
  AdminRole,
  AdminRoleForm,
  AdminRolePermission,
  AdminUser,
  AdminUserForm,
  UiMenuItemAdmin,
  UiMenuItemAdminForm
} from '../core/models/admin.models';
import { TranslationService } from '../core/i18n/translation.service';
import { AdminApiService } from '../core/services/admin-api.service';
import { DataTableAction, DataTableColumn } from '../shared/components/data-table/data-table.component';

type PrimaryRole = 'ADMIN' | 'ACCOUNTANT';
type DialogMode = 'create' | 'edit';
type DeleteTargetKind = 'lookupType' | 'lookupValue' | 'role' | 'menuScreen';

interface PermissionDraft {
  menuItemId: string;
  titleKey: string;
  url?: string | null;
  parentId?: string | null;
  canView: boolean;
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

@Component({
  standalone: false,
  selector: 'app-accountants-home',
  templateUrl: './accountants-home.component.html',
  styleUrls: ['./accountants-home.component.scss']
})
export class AccountantsHomeComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  readonly titleKey = 'ACCESS_MANAGEMENT.TITLE';
  readonly subtitleKey = 'ACCESS_MANAGEMENT.SUBTITLE';
  activeTabId = 'users';
  loading = false;
  lookupTypesLoading = false;
  lookupValuesLoading = false;
  userSaving = false;
  roleSaving = false;
  lookupTypeSaving = false;
  lookupValueSaving = false;
  screenSaving = false;
  deleteSubmitting = false;
  accessErrorKey = '';
  accessSuccessKey = '';
  lookupErrorKey = '';
  lookupSuccessKey = '';
  userQuery = '';
  roleQuery = '';
  lookupTypeQuery = '';
  lookupValueQuery = '';
  screenQuery = '';
  permissionQuery = '';
  users: AdminUser[] = [];
  roles: AdminRole[] = [];
  menuItems = new Map<string, { titleKey: string; parentId?: string | null; url?: string | null }>();
  lookupTypes: AdminLookupType[] = [];
  lookupValues: AdminLookupValue[] = [];
  menuScreens: UiMenuItemAdmin[] = [];
  selectedRoleId: number | null = null;
  selectedLookupTypeId: number | null = null;
  userDialogVisible = false;
  roleDialogVisible = false;
  lookupTypeDialogVisible = false;
  lookupValueDialogVisible = false;
  screenDialogVisible = false;
  confirmDialogVisible = false;
  userDialogMode: DialogMode = 'create';
  roleDialogMode: DialogMode = 'create';
  lookupTypeDialogMode: DialogMode = 'create';
  lookupValueDialogMode: DialogMode = 'create';
  screenDialogMode: DialogMode = 'create';
  editingUserId: number | null = null;
  editingRoleId: number | null = null;
  editingLookupTypeId: number | null = null;
  editingLookupValueId: number | null = null;
  editingScreenId: string | null = null;
  confirmTarget: { kind: DeleteTargetKind; id: number | string; label: string } | null = null;
  rolePermissionDraft: PermissionDraft[] = [];

  readonly userColumns: DataTableColumn[] = [
    { key: 'fullName', title: 'ACCESS_MANAGEMENT.FULL_NAME', clickable: true, sortable: true },
    { key: 'username', title: 'PROFILE.USERNAME', sortable: true },
    { key: 'email', title: 'PROFILE.EMAIL', sortable: true },
    { key: 'passwordMasked', title: 'AUTH.PASSWORD' },
    { key: 'primaryRole', title: 'ACCESS_MANAGEMENT.PRIMARY_ROLE', kind: 'type', prefix: 'ROLE.', sortable: true },
    { key: 'roleSummary', title: 'ACCESS_MANAGEMENT.ASSIGNED_ROLES', align: 'start' },
    { key: 'active', title: 'ACCESS_MANAGEMENT.ACTIVE_USER', kind: 'booleanToggle', sortable: true }
  ];
  readonly userActions: DataTableAction[] = [];
  readonly roleColumns: DataTableColumn[] = [
    { key: 'displayName', title: 'COMMON.NAME', clickable: true, align: 'start', sortable: true },
    { key: 'code', title: 'ACCESS_MANAGEMENT.ROLE_CODE', sortable: true },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean', sortable: true },
    { key: 'systemRole', title: 'ACCESS_MANAGEMENT.SYSTEM_ROLE', kind: 'boolean', sortable: true },
    { key: 'grantedScreens', title: 'ACCESS_MANAGEMENT.VISIBLE_SCREENS', sortable: true }
  ];
  readonly roleActions: DataTableAction[] = [
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-btn--info' },
    { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-btn--danger' }
  ];
  readonly lookupTypeColumns: DataTableColumn[] = [
    { key: 'code', title: 'ACCESS_MANAGEMENT.TYPE_CODE', clickable: true, sortable: true },
    { key: 'displayName', title: 'COMMON.NAME', align: 'start', sortable: true },
    { key: 'sortOrder', title: 'ACCESS_MANAGEMENT.SORT_ORDER', sortable: true },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean', sortable: true }
  ];
  readonly lookupTypeActions: DataTableAction[] = [
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-btn--info' },
    { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-btn--danger' }
  ];
  readonly lookupValueColumns: DataTableColumn[] = [
    { key: 'code', title: 'ACCESS_MANAGEMENT.VALUE_CODE', sortable: true },
    { key: 'displayName', title: 'COMMON.NAME', align: 'start', sortable: true },
    { key: 'sortOrder', title: 'ACCESS_MANAGEMENT.SORT_ORDER', sortable: true },
    { key: 'active', title: 'COMMON.STATUS', kind: 'boolean', sortable: true }
  ];
  readonly screenItemTypeOptions = [
    { value: 'item', labelKey: 'ACCESS_MANAGEMENT.MENU_ITEM_TYPE_ITEM' },
    { value: 'group', labelKey: 'ACCESS_MANAGEMENT.MENU_ITEM_TYPE_GROUP' },
    { value: 'collapse', labelKey: 'ACCESS_MANAGEMENT.MENU_ITEM_TYPE_COLLAPSE' }
  ];
  readonly screenColumns: DataTableColumn[] = [
    { key: 'id', title: 'ACCESS_MANAGEMENT.SCREEN_ID', align: 'start', sortable: true },
    { key: 'titleKey', title: 'ACCESS_MANAGEMENT.TITLE_KEY', align: 'start', sortable: true },
    { key: 'titlePreview', title: 'ACCESS_MANAGEMENT.SCREEN_TITLE_PREVIEW', align: 'start', sortable: true },
    { key: 'itemTypeDisplay', title: 'ACCESS_MANAGEMENT.ITEM_TYPE', sortable: true },
    { key: 'parentId', title: 'ACCESS_MANAGEMENT.PARENT_ID', align: 'start', sortable: true },
    { key: 'url', title: 'ACCESS_MANAGEMENT.MENU_ROUTE', align: 'start', sortable: true },
    { key: 'sortOrder', title: 'ACCESS_MANAGEMENT.SORT_ORDER', sortable: true },
    { key: 'rolesCsv', title: 'ACCESS_MANAGEMENT.ROLES_CSV', align: 'start', sortable: true }
  ];
  readonly screenActions: DataTableAction[] = [
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-btn--info' },
    { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-btn--danger' }
  ];
  readonly lookupValueActions: DataTableAction[] = [
    { id: 'edit', labelKey: 'COMMON.EDIT', className: 'erp-action-btn--info' },
    { id: 'delete', labelKey: 'COMMON.DELETE', className: 'erp-action-btn--danger' }
  ];

  readonly userForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(190)]],
    phone: ['', [Validators.required, Validators.maxLength(30)]],
    password: ['', [Validators.minLength(8), Validators.maxLength(255), Validators.pattern(/^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^\w\s]).{8,}$/)]],
    fullNameEn: ['', [Validators.required, Validators.maxLength(150)]],
    fullNameAr: ['', [Validators.required, Validators.maxLength(150)]],
    primaryRole: ['ACCOUNTANT' as PrimaryRole, Validators.required],
    active: [true, Validators.required],
    roleIds: [[] as number[]]
  });
  readonly roleForm = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(60)]],
    nameEn: ['', [Validators.required, Validators.maxLength(150)]],
    nameAr: ['', [Validators.required, Validators.maxLength(150)]],
    active: [true, Validators.required]
  });
  readonly lookupTypeForm = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(60)]],
    nameEn: ['', [Validators.required, Validators.maxLength(150)]],
    nameAr: ['', [Validators.required, Validators.maxLength(150)]],
    sortOrder: [0, [Validators.required, Validators.min(0)]],
    active: [true, Validators.required]
  });
  readonly lookupValueForm = this.fb.group({
    typeCode: ['', [Validators.required, Validators.maxLength(60)]],
    code: ['', [Validators.required, Validators.maxLength(80)]],
    nameEn: ['', [Validators.required, Validators.maxLength(150)]],
    nameAr: ['', [Validators.required, Validators.maxLength(150)]],
    sortOrder: [0, [Validators.required, Validators.min(0)]],
    active: [true, Validators.required]
  });
  readonly screenForm = this.fb.group({
    id: ['', [Validators.required, Validators.maxLength(64)]],
    parentId: [''],
    sortOrder: [0, [Validators.required, Validators.min(0)]],
    itemType: ['item', [Validators.required, Validators.maxLength(16)]],
    titleKey: ['', [Validators.required, Validators.maxLength(128)]],
    icon: ['', Validators.maxLength(64)],
    url: ['', Validators.maxLength(512)],
    rolesCsv: ['', Validators.maxLength(256)],
    external: [false],
    targetBlank: [false],
    itemClasses: ['', Validators.maxLength(128)],
    breadcrumbsFlag: [false]
  });

  constructor(
    private readonly adminApi: AdminApiService,
    private readonly fb: FormBuilder,
    public readonly translationService: TranslationService,
    private readonly cdr: ChangeDetectorRef,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const tab = this.route.snapshot.data['tab'];
    if (tab) {
      this.activeTabId = tab;
    }
    this.route.data
      .pipe(
        map((data) => data['tab'] as string | undefined),
        filter((t): t is string => !!t),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe((t) => {
        this.activeTabId = t;
      });
    this.translationService.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe(() => this.cdr.detectChanges());
    this.loadPage();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get selectedRole(): AdminRole | null {
    return this.roles.find((role) => role.id === this.selectedRoleId) || null;
  }

  get selectedLookupType(): AdminLookupType | null {
    return this.lookupTypes.find((type) => type.id === this.selectedLookupTypeId) || null;
  }

  get usersCount(): number { return this.users.length; }
  get activeUsersCount(): number { return this.users.filter((item) => item.active).length; }
  get customRolesCount(): number { return this.roles.length; }
  get lookupTypesCount(): number { return this.lookupTypes.length; }

  get filteredUsers(): Array<Record<string, unknown>> {
    const query = this.userQuery.trim().toLowerCase();
    const masked = this.translationService.instant('ACCESS_MANAGEMENT.PASSWORD_MASKED');
    return this.users
      .filter(
        (user) =>
          !query ||
          [user.fullNameEn, user.fullNameAr, user.fullName, user.username, user.email, user.phone, user.primaryRole, ...(user.roleCodes || [])]
            .filter(Boolean)
            .join(' ')
            .toLowerCase()
            .includes(query)
      )
      .map((user) => ({
        ...user,
        fullName: this.displayName(user.fullNameEn, user.fullNameAr, user.username),
        passwordMasked: masked,
        roleSummary: (user.roleCodes || []).join(', ') || ''
      }));
  }

  get filteredRoles(): Array<Record<string, unknown>> {
    const query = this.roleQuery.trim().toLowerCase();
    return this.roles
      .filter((role) => !query || [role.code, role.nameEn, role.nameAr].filter(Boolean).join(' ').toLowerCase().includes(query))
      .map((role) => ({
        ...role,
        displayName: this.displayName(role.nameEn, role.nameAr, role.code),
        grantedScreens: role.permissions.filter((permission) => permission.canView).length
      }));
  }

  get filteredLookupTypes(): Array<Record<string, unknown>> {
    const query = this.lookupTypeQuery.trim().toLowerCase();
    return this.lookupTypes
      .filter((type) => !query || [type.code, type.nameEn, type.nameAr].filter(Boolean).join(' ').toLowerCase().includes(query))
      .map((type) => ({
        ...type,
        displayName: this.displayName(type.nameEn, type.nameAr, type.code)
      }));
  }

  get filteredLookupValues(): Array<Record<string, unknown>> {
    const query = this.lookupValueQuery.trim().toLowerCase();
    return this.lookupValues
      .filter((value) => !query || [value.code, value.nameEn, value.nameAr].filter(Boolean).join(' ').toLowerCase().includes(query))
      .map((value) => ({
        ...value,
        displayName: this.displayName(value.nameEn, value.nameAr, value.code)
      }));
  }

  get filteredScreens(): Array<Record<string, unknown>> {
    const query = this.screenQuery.trim().toLowerCase();
    return (this.menuScreens || [])
      .filter((row) =>
        !query ||
        [row.id, row.titleKey, row.url, row.itemType, row.parentId, row.rolesCsv].filter(Boolean).join(' ').toLowerCase().includes(query)
      )
      .map((row) => ({
        ...row,
        titlePreview: this.translationService.instant(row.titleKey),
        itemTypeDisplay: this.screenItemTypeLabel(row.itemType)
      }));
  }

  get selectedRolePermissions(): AdminRolePermission[] {
    const role = this.selectedRole;
    const query = this.permissionQuery.trim().toLowerCase();
    return (role?.permissions || []).filter((permission) => !query || [permission.menuItemId, permission.url, this.translationService.instant(permission.titleKey)].join(' ').toLowerCase().includes(query));
  }

  get editableRolePermissions(): PermissionDraft[] {
    const query = this.permissionQuery.trim().toLowerCase();
    return this.rolePermissionDraft.filter((permission) => !query || [permission.menuItemId, permission.url, this.translationService.instant(permission.titleKey)].join(' ').toLowerCase().includes(query));
  }

  loadPage(): void {
    this.loading = true;
    this.lookupTypesLoading = true;
    this.accessErrorKey = '';
    this.lookupErrorKey = '';
    forkJoin({
      access: this.adminApi.getAccessContext(),
      lookupTypes: this.adminApi.getLookupTypes(),
      menuScreens: this.adminApi.listMenuItems()
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.lookupTypesLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: ({ access, lookupTypes, menuScreens }) => {
          this.applyAccessContext(access);
          this.applyLookupTypes(lookupTypes);
          this.menuScreens = menuScreens || [];
        },
        error: () => {
          this.accessErrorKey = 'ACCESS_MANAGEMENT.LOAD_ERROR';
          this.lookupErrorKey = 'ACCESS_MANAGEMENT.LOOKUP_LOAD_ERROR';
        }
      });
  }

  openCreateUserDialog(): void {
    this.userDialogMode = 'create';
    this.editingUserId = null;
    this.userForm.reset({ username: '', email: '', phone: '', password: '', fullNameEn: '', fullNameAr: '', primaryRole: 'ACCOUNTANT', active: true, roleIds: [] });
    this.userDialogVisible = true;
  }

  openEditUserDialog(userId: number): void {
    const user = this.users.find((item) => item.id === userId);
    if (!user) { return; }
    this.userDialogMode = 'edit';
    this.editingUserId = user.id;
    this.userForm.reset({
      username: user.username,
      email: user.email,
      phone: user.phone,
      password: '',
      fullNameEn: user.fullNameEn || '',
      fullNameAr: user.fullNameAr || '',
      primaryRole: (user.primaryRole || 'ACCOUNTANT') as PrimaryRole,
      active: !!user.active,
      roleIds: [...(user.roleIds || [])]
    });
    this.userDialogVisible = true;
  }

  closeUserDialog(): void { this.userDialogVisible = false; }

  saveUser(): void {
    const password = String(this.userForm.value.password || '');
    if (this.userDialogMode === 'create' && !password.trim()) {
      this.userForm.controls.password.setErrors({ required: true });
    }
    if (this.userForm.invalid || this.userSaving) {
      this.userForm.markAllAsTouched();
      return;
    }
    this.userSaving = true;
    this.accessErrorKey = '';
    this.accessSuccessKey = '';
    const payload: AdminUserForm = {
      username: String(this.userForm.value.username || '').trim(),
      email: String(this.userForm.value.email || '').trim(),
      phone: String(this.userForm.value.phone || '').trim(),
      password: password.trim() || undefined,
      fullNameEn: String(this.userForm.value.fullNameEn || '').trim(),
      fullNameAr: String(this.userForm.value.fullNameAr || '').trim(),
      primaryRole: (this.userForm.value.primaryRole || 'ACCOUNTANT') as PrimaryRole,
      active: !!this.userForm.value.active,
      roleIds: [...(this.userForm.value.roleIds || [])]
    };
    const request$ = this.userDialogMode === 'create' ? this.adminApi.createUser(payload) : this.adminApi.updateUser(this.editingUserId as number, payload);
    request$.pipe(finalize(() => (this.userSaving = false))).subscribe({
      next: () => {
        this.accessSuccessKey = this.userDialogMode === 'create' ? 'ACCESS_MANAGEMENT.USER_CREATE_SUCCESS' : 'ACCESS_MANAGEMENT.USER_UPDATE_SUCCESS';
        this.userDialogVisible = false;
        this.refreshAccessContext();
      },
      error: () => {
        this.accessErrorKey = this.userDialogMode === 'create' ? 'ACCESS_MANAGEMENT.USER_CREATE_ERROR' : 'ACCESS_MANAGEMENT.USER_UPDATE_ERROR';
      }
    });
  }

  toggleUserRole(roleId: number, checked: boolean): void {
    const selected = new Set(this.userForm.value.roleIds || []);
    if (checked) { selected.add(roleId); } else { selected.delete(roleId); }
    this.userForm.patchValue({ roleIds: Array.from(selected) });
  }

  hasUserRole(roleId: number): boolean {
    return (this.userForm.value.roleIds || []).includes(roleId);
  }

  onUserAction(_event: { actionId: string; row: Record<string, unknown> }): void {}

  onUserActiveToggle(event: { key: string; row: Record<string, unknown>; checked: boolean }): void {
    if (event.key !== 'active') {
      return;
    }
    const id = Number(event.row['id']);
    if (!id) {
      return;
    }
    const user = this.users.find((u) => u.id === id);
    const previousActive = !!user?.active;
    this.accessErrorKey = '';
    this.accessSuccessKey = '';
    this.patchUserActive(id, event.checked);
    this.adminApi.setUserActive(id, event.checked).subscribe({
      next: () => {
        this.accessSuccessKey = 'ACCESS_MANAGEMENT.USER_ACTIVE_UPDATED';
        this.cdr.detectChanges();
      },
      error: () => {
        this.patchUserActive(id, previousActive);
        this.accessErrorKey = 'ACCESS_MANAGEMENT.USER_ACTIVE_ERROR';
        this.cdr.detectChanges();
      }
    });
  }

  onUserCellClick(event: { key: string; row: Record<string, unknown> }): void {
    if (event.key === 'fullName') { this.openEditUserDialog(Number(event.row['id'])); }
  }

  openCreateRoleDialog(): void {
    this.roleDialogMode = 'create';
    this.editingRoleId = null;
    this.permissionQuery = '';
    this.roleForm.enable({ emitEvent: false });
    this.roleForm.reset({ code: '', nameEn: '', nameAr: '', active: true });
    this.rolePermissionDraft = this.buildPermissionDraft();
    this.roleDialogVisible = true;
  }

  openEditRoleDialog(roleId: number): void {
    const role = this.roles.find((item) => item.id === roleId);
    if (!role) { return; }
    this.roleDialogMode = 'edit';
    this.editingRoleId = role.id;
    this.permissionQuery = '';
    this.roleForm.reset({ code: role.code, nameEn: role.nameEn, nameAr: role.nameAr || '', active: !!role.active });
    if (role.systemRole) { this.roleForm.controls.code.disable({ emitEvent: false }); } else { this.roleForm.controls.code.enable({ emitEvent: false }); }
    this.rolePermissionDraft = this.buildPermissionDraft(role.permissions);
    this.roleDialogVisible = true;
  }

  closeRoleDialog(): void { this.roleDialogVisible = false; }

  saveRole(): void {
    if (this.roleForm.invalid || this.roleSaving) {
      this.roleForm.markAllAsTouched();
      return;
    }
    this.roleSaving = true;
    this.accessErrorKey = '';
    this.accessSuccessKey = '';
    const payload: AdminRoleForm = {
      code: String(this.roleForm.getRawValue().code || '').trim(),
      nameEn: String(this.roleForm.value.nameEn || '').trim(),
      nameAr: String(this.roleForm.value.nameAr || '').trim() || undefined,
      active: !!this.roleForm.value.active,
      permissions: this.rolePermissionDraft.map((permission) => ({
        menuItemId: permission.menuItemId,
        canView: permission.canView,
        canCreate: permission.canCreate,
        canEdit: permission.canEdit,
        canDelete: permission.canDelete
      }))
    };
    const request$ = this.roleDialogMode === 'create' ? this.adminApi.createRole(payload) : this.adminApi.updateRole(this.editingRoleId as number, payload);
    request$.pipe(finalize(() => (this.roleSaving = false))).subscribe({
      next: (role) => {
        this.accessSuccessKey = this.roleDialogMode === 'create' ? 'ACCESS_MANAGEMENT.ROLE_CREATE_SUCCESS' : 'ACCESS_MANAGEMENT.ROLE_UPDATE_SUCCESS';
        this.roleDialogVisible = false;
        this.refreshAccessContext(role.id);
      },
      error: () => {
        this.accessErrorKey = this.roleDialogMode === 'create' ? 'ACCESS_MANAGEMENT.ROLE_CREATE_ERROR' : 'ACCESS_MANAGEMENT.ROLE_UPDATE_ERROR';
      }
    });
  }

  togglePermission(permission: PermissionDraft, field: 'canView' | 'canCreate' | 'canEdit' | 'canDelete', checked: boolean): void {
    permission[field] = checked;
    if (field === 'canView' && !checked) {
      permission.canCreate = false;
      permission.canEdit = false;
      permission.canDelete = false;
    }
    if (checked && field !== 'canView') {
      permission.canView = true;
    }
  }

  setSelectedRole(roleId: number): void { this.selectedRoleId = roleId; }

  onRoleAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'edit') {
      this.openEditRoleDialog(id);
      return;
    }
    if (event.actionId === 'delete') {
      if (event.row['systemRole']) {
        this.accessErrorKey = 'ACCESS_MANAGEMENT.ROLE_DELETE_SYSTEM';
        return;
      }
      const role = this.roles.find((r) => r.id === id);
      this.confirmTarget = {
        kind: 'role',
        id,
        label: role ? this.displayName(role.nameEn, role.nameAr, role.code) : String(id)
      };
      this.confirmDialogVisible = true;
    }
  }

  onRoleCellClick(event: { key: string; row: Record<string, unknown> }): void {
    if (event.key === 'displayName') {
      this.setSelectedRole(Number(event.row['id']));
    }
  }

  permissionEnabledCount(role: AdminRole | null): number {
    return (role?.permissions || []).filter((item) => item.canView || item.canCreate || item.canEdit || item.canDelete).length;
  }

  permissionEnabledCountDraft(): number {
    return this.rolePermissionDraft.filter((item) => item.canView || item.canCreate || item.canEdit || item.canDelete).length;
  }

  openCreateLookupTypeDialog(): void {
    this.lookupTypeDialogMode = 'create';
    this.editingLookupTypeId = null;
    this.lookupTypeForm.reset({ code: '', nameEn: '', nameAr: '', sortOrder: this.lookupTypes.length, active: true });
    this.lookupTypeDialogVisible = true;
  }

  openEditLookupTypeDialog(typeId: number): void {
    const type = this.lookupTypes.find((item) => item.id === typeId);
    if (!type) { return; }
    this.lookupTypeDialogMode = 'edit';
    this.editingLookupTypeId = type.id;
    this.lookupTypeForm.reset({ code: type.code, nameEn: type.nameEn, nameAr: type.nameAr || '', sortOrder: type.sortOrder, active: !!type.active });
    this.lookupTypeDialogVisible = true;
  }

  closeLookupTypeDialog(): void { this.lookupTypeDialogVisible = false; }

  saveLookupType(): void {
    if (this.lookupTypeForm.invalid || this.lookupTypeSaving) {
      this.lookupTypeForm.markAllAsTouched();
      return;
    }
    this.lookupTypeSaving = true;
    this.lookupErrorKey = '';
    this.lookupSuccessKey = '';
    const payload: AdminLookupTypeForm = {
      code: String(this.lookupTypeForm.value.code || '').trim(),
      nameEn: String(this.lookupTypeForm.value.nameEn || '').trim(),
      nameAr: String(this.lookupTypeForm.value.nameAr || '').trim() || undefined,
      sortOrder: Number(this.lookupTypeForm.value.sortOrder || 0),
      active: !!this.lookupTypeForm.value.active
    };
    const request$ = this.lookupTypeDialogMode === 'create' ? this.adminApi.createLookupType(payload) : this.adminApi.updateLookupType(this.editingLookupTypeId as number, payload);
    request$.pipe(finalize(() => (this.lookupTypeSaving = false))).subscribe({
      next: (type) => {
        this.lookupSuccessKey = this.lookupTypeDialogMode === 'create' ? 'ACCESS_MANAGEMENT.LOOKUP_TYPE_CREATE_SUCCESS' : 'ACCESS_MANAGEMENT.LOOKUP_TYPE_UPDATE_SUCCESS';
        this.lookupTypeDialogVisible = false;
        this.refreshLookupTypes(type.id);
      },
      error: () => {
        this.lookupErrorKey = this.lookupTypeDialogMode === 'create' ? 'ACCESS_MANAGEMENT.LOOKUP_TYPE_CREATE_ERROR' : 'ACCESS_MANAGEMENT.LOOKUP_TYPE_UPDATE_ERROR';
      }
    });
  }

  openCreateLookupValueDialog(): void {
    const type = this.selectedLookupType;
    if (!type) { return; }
    this.lookupValueDialogMode = 'create';
    this.editingLookupValueId = null;
    this.lookupValueForm.reset({ typeCode: type.code, code: '', nameEn: '', nameAr: '', sortOrder: this.lookupValues.length, active: true });
    this.lookupValueDialogVisible = true;
  }

  openEditLookupValueDialog(valueId: number): void {
    const value = this.lookupValues.find((item) => item.id === valueId);
    if (!value) { return; }
    this.lookupValueDialogMode = 'edit';
    this.editingLookupValueId = value.id;
    this.lookupValueForm.reset({ typeCode: value.typeCode, code: value.code, nameEn: value.nameEn || '', nameAr: value.nameAr || '', sortOrder: value.sortOrder, active: !!value.active });
    this.lookupValueDialogVisible = true;
  }

  closeLookupValueDialog(): void { this.lookupValueDialogVisible = false; }

  saveLookupValue(): void {
    if (this.lookupValueForm.invalid || this.lookupValueSaving) {
      this.lookupValueForm.markAllAsTouched();
      return;
    }
    this.lookupValueSaving = true;
    this.lookupErrorKey = '';
    this.lookupSuccessKey = '';
    const payload: AdminLookupValueForm = {
      typeCode: String(this.lookupValueForm.value.typeCode || '').trim(),
      code: String(this.lookupValueForm.value.code || '').trim(),
      nameEn: String(this.lookupValueForm.value.nameEn || '').trim() || undefined,
      nameAr: String(this.lookupValueForm.value.nameAr || '').trim() || undefined,
      sortOrder: Number(this.lookupValueForm.value.sortOrder || 0),
      active: !!this.lookupValueForm.value.active
    };
    const request$ = this.lookupValueDialogMode === 'create' ? this.adminApi.createLookupValue(payload) : this.adminApi.updateLookupValue(this.editingLookupValueId as number, payload);
    request$.pipe(finalize(() => (this.lookupValueSaving = false))).subscribe({
      next: () => {
        this.lookupSuccessKey = this.lookupValueDialogMode === 'create' ? 'ACCESS_MANAGEMENT.LOOKUP_VALUE_CREATE_SUCCESS' : 'ACCESS_MANAGEMENT.LOOKUP_VALUE_UPDATE_SUCCESS';
        this.lookupValueDialogVisible = false;
        if (this.selectedLookupType) { this.loadLookupValues(this.selectedLookupType.code); }
      },
      error: () => {
        this.lookupErrorKey = this.lookupValueDialogMode === 'create' ? 'ACCESS_MANAGEMENT.LOOKUP_VALUE_CREATE_ERROR' : 'ACCESS_MANAGEMENT.LOOKUP_VALUE_UPDATE_ERROR';
      }
    });
  }

  confirmDeleteLookupType(typeId: number): void {
    const type = this.lookupTypes.find((item) => item.id === typeId);
    if (!type) { return; }
    this.confirmTarget = { kind: 'lookupType', id: type.id, label: this.displayName(type.nameEn, type.nameAr, type.code) };
    this.confirmDialogVisible = true;
  }

  confirmDeleteLookupValue(valueId: number): void {
    const value = this.lookupValues.find((item) => item.id === valueId);
    if (!value) { return; }
    this.confirmTarget = { kind: 'lookupValue', id: value.id, label: this.displayName(value.nameEn, value.nameAr, value.code) };
    this.confirmDialogVisible = true;
  }

  closeConfirmDialog(): void {
    this.confirmDialogVisible = false;
    this.confirmTarget = null;
  }

  submitDelete(): void {
    if (!this.confirmTarget || this.deleteSubmitting) { return; }
    this.deleteSubmitting = true;
    this.lookupErrorKey = '';
    this.lookupSuccessKey = '';
    this.accessErrorKey = '';
    this.accessSuccessKey = '';
    const target = this.confirmTarget;
    const request$: Observable<unknown> =
      target.kind === 'lookupType'
        ? this.adminApi.deleteLookupType(target.id as number)
        : target.kind === 'lookupValue'
          ? this.adminApi.deleteLookupValue(target.id as number)
          : target.kind === 'role'
            ? this.adminApi.deleteRole(target.id as number)
            : this.adminApi.deleteMenuItem(String(target.id));
    request$.pipe(finalize(() => (this.deleteSubmitting = false))).subscribe({
      next: () => {
        if (target.kind === 'lookupType') {
          this.lookupSuccessKey = 'ACCESS_MANAGEMENT.LOOKUP_TYPE_DELETE_SUCCESS';
          this.closeConfirmDialog();
          this.refreshLookupTypes(null);
        } else if (target.kind === 'lookupValue') {
          this.lookupSuccessKey = 'ACCESS_MANAGEMENT.LOOKUP_VALUE_DELETE_SUCCESS';
          this.closeConfirmDialog();
          if (this.selectedLookupType) { this.loadLookupValues(this.selectedLookupType.code); }
        } else if (target.kind === 'role') {
          this.accessSuccessKey = 'ACCESS_MANAGEMENT.ROLE_DELETE_SUCCESS';
          this.closeConfirmDialog();
          this.refreshAccessContext();
        } else {
          this.accessSuccessKey = 'ACCESS_MANAGEMENT.SCREEN_DELETE_SUCCESS';
          this.closeConfirmDialog();
          this.refreshMenuScreens();
          this.refreshMenuItemsOnly();
        }
      },
      error: () => {
        if (target.kind === 'lookupType') {
          this.lookupErrorKey = 'ACCESS_MANAGEMENT.LOOKUP_TYPE_DELETE_ERROR';
        } else if (target.kind === 'lookupValue') {
          this.lookupErrorKey = 'ACCESS_MANAGEMENT.LOOKUP_VALUE_DELETE_ERROR';
        } else if (target.kind === 'role') {
          this.accessErrorKey = 'ACCESS_MANAGEMENT.ROLE_DELETE_ERROR';
        } else {
          this.accessErrorKey = 'ACCESS_MANAGEMENT.SCREEN_DELETE_ERROR';
        }
      }
    });
  }

  onLookupTypeAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'edit') { this.openEditLookupTypeDialog(id); return; }
    if (event.actionId === 'delete') { this.confirmDeleteLookupType(id); }
  }

  onLookupTypeCellClick(event: { key: string; row: Record<string, unknown> }): void {
    if (event.key !== 'code') { return; }
    const type = this.lookupTypes.find((item) => item.id === Number(event.row['id']));
    if (!type) { return; }
    this.selectedLookupTypeId = type.id;
    this.loadLookupValues(type.code);
  }

  onLookupValueAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'edit') { this.openEditLookupValueDialog(id); return; }
    if (event.actionId === 'delete') { this.confirmDeleteLookupValue(id); }
  }

  openCreateScreenDialog(): void {
    this.screenDialogMode = 'create';
    this.editingScreenId = null;
    this.screenForm.reset({
      id: '',
      parentId: '',
      sortOrder: this.menuScreens.length,
      itemType: 'item',
      titleKey: '',
      icon: 'link',
      url: '',
      rolesCsv: 'ADMIN',
      external: false,
      targetBlank: false,
      itemClasses: 'nav-item',
      breadcrumbsFlag: false
    });
    this.screenForm.controls.id.enable({ emitEvent: false });
    this.screenDialogVisible = true;
  }

  openEditScreenDialog(screenId: string): void {
    const row = this.menuScreens.find((s) => s.id === screenId);
    if (!row) {
      return;
    }
    this.screenDialogMode = 'edit';
    this.editingScreenId = row.id;
    this.screenForm.reset({
      id: row.id,
      parentId: row.parentId || '',
      sortOrder: row.sortOrder ?? 0,
      itemType: row.itemType || 'item',
      titleKey: row.titleKey,
      icon: row.icon || '',
      url: row.url || '',
      rolesCsv: row.rolesCsv || '',
      external: !!row.external,
      targetBlank: !!row.targetBlank,
      itemClasses: row.itemClasses || '',
      breadcrumbsFlag: !!row.breadcrumbsFlag
    });
    this.screenForm.controls.id.disable({ emitEvent: false });
    this.screenDialogVisible = true;
  }

  closeScreenDialog(): void {
    this.screenDialogVisible = false;
  }

  saveScreen(): void {
    if (this.screenForm.invalid || this.screenSaving) {
      this.screenForm.markAllAsTouched();
      return;
    }
    this.screenSaving = true;
    this.accessErrorKey = '';
    const raw = this.screenForm.getRawValue();
    const parentRaw = String(raw.parentId || '').trim();
    const payload: UiMenuItemAdminForm = {
      id: String(raw.id || '').trim(),
      parentId: parentRaw ? parentRaw : null,
      sortOrder: Number(raw.sortOrder || 0),
      itemType: String(raw.itemType || 'item').trim(),
      titleKey: String(raw.titleKey || '').trim(),
      icon: raw.icon ? String(raw.icon).trim() : null,
      url: raw.url ? String(raw.url).trim() : null,
      rolesCsv: raw.rolesCsv ? String(raw.rolesCsv).trim() : null,
      external: !!raw.external,
      targetBlank: !!raw.targetBlank,
      itemClasses: raw.itemClasses ? String(raw.itemClasses).trim() : null,
      breadcrumbsFlag: !!raw.breadcrumbsFlag
    };
    const request$ =
      this.screenDialogMode === 'create'
        ? this.adminApi.createMenuItem(payload)
        : this.adminApi.updateMenuItem(this.editingScreenId as string, payload);
    request$.pipe(finalize(() => (this.screenSaving = false))).subscribe({
      next: () => {
        this.accessSuccessKey =
          this.screenDialogMode === 'create' ? 'ACCESS_MANAGEMENT.SCREEN_CREATE_SUCCESS' : 'ACCESS_MANAGEMENT.SCREEN_UPDATE_SUCCESS';
        this.screenDialogVisible = false;
        this.refreshMenuScreens();
        this.refreshMenuItemsOnly();
      },
      error: () => {
        this.accessErrorKey = 'ACCESS_MANAGEMENT.SCREEN_SAVE_ERROR';
      }
    });
  }

  onScreenAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = String(event.row['id'] || '');
    if (event.actionId === 'edit') {
      this.openEditScreenDialog(id);
      return;
    }
    if (event.actionId === 'delete') {
      this.confirmTarget = { kind: 'menuScreen', id, label: id };
      this.confirmDialogVisible = true;
    }
  }

  refreshMenuScreens(): void {
    this.adminApi.listMenuItems().subscribe({
      next: (rows) => {
        this.menuScreens = rows || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.accessErrorKey = 'ACCESS_MANAGEMENT.SCREEN_LOAD_ERROR';
      }
    });
  }

  /** Keeps role permission matrix in sync after menu CRUD without toggling the full-page loading flag. */
  private refreshMenuItemsOnly(): void {
    this.adminApi.getAccessContext().subscribe({
      next: (context) => {
        this.menuItems = new Map(
          (context.menuItems || []).map((item) => [item.id, { titleKey: item.titleKey, parentId: item.parentId, url: item.url }])
        );
        this.cdr.detectChanges();
      },
      error: () => {
        this.accessErrorKey = 'ACCESS_MANAGEMENT.LOAD_ERROR';
      }
    });
  }

  displayName(nameEn?: string | null, nameAr?: string | null, fallback?: string | null): string {
    return this.translationService.currentLanguage === 'ar' ? nameAr || nameEn || fallback || '' : nameEn || nameAr || fallback || '';
  }

  permissionRouteLabel(url?: string | null): string { return url || ''; }

  permissionPathLabel(parentId?: string | null): string {
    if (!parentId) { return ''; }
    const parent = this.menuItems.get(parentId);
    return parent ? this.translationService.instant(parent.titleKey) : parentId;
  }

  private refreshAccessContext(preferredRoleId: number | null = null): void {
    this.loading = true;
    this.adminApi
      .getAccessContext()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
      next: (context) => this.applyAccessContext(context, preferredRoleId),
      error: () => { this.accessErrorKey = 'ACCESS_MANAGEMENT.LOAD_ERROR'; }
    });
  }

  private refreshLookupTypes(preferredTypeId: number | null): void {
    this.lookupTypesLoading = true;
    this.adminApi
      .getLookupTypes()
      .pipe(
        finalize(() => {
          this.lookupTypesLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
      next: (types) => this.applyLookupTypes(types, preferredTypeId),
      error: () => { this.lookupErrorKey = 'ACCESS_MANAGEMENT.LOOKUP_LOAD_ERROR'; }
    });
  }

  private loadLookupValues(typeCode: string): void {
    this.lookupValuesLoading = true;
    this.adminApi
      .getLookupValues(typeCode)
      .pipe(
        finalize(() => {
          this.lookupValuesLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
      next: (values) => { this.lookupValues = values; },
      error: () => { this.lookupErrorKey = 'ACCESS_MANAGEMENT.LOOKUP_VALUES_LOAD_ERROR'; }
    });
  }

  private patchUserActive(userId: number, active: boolean): void {
    const target = this.users.find((u) => u.id === userId);
    if (target) {
      target.active = active;
    }
  }

  private applyAccessContext(context: AdminAccessContext, preferredRoleId: number | null = null): void {
    this.users = context.users || [];
    this.roles = context.roles || [];
    this.menuItems = new Map((context.menuItems || []).map((item) => [item.id, { titleKey: item.titleKey, parentId: item.parentId, url: item.url }]));
    if (preferredRoleId && this.roles.some((role) => role.id === preferredRoleId)) {
      this.selectedRoleId = preferredRoleId;
    } else if (!this.selectedRoleId || !this.roles.some((role) => role.id === this.selectedRoleId)) {
      this.selectedRoleId = this.roles[0]?.id || null;
    }
  }

  private applyLookupTypes(types: AdminLookupType[], preferredTypeId: number | null = null): void {
    this.lookupTypes = types || [];
    let nextTypeId = preferredTypeId !== null && preferredTypeId !== undefined ? preferredTypeId : this.selectedLookupTypeId;
    if (!nextTypeId || !this.lookupTypes.some((type) => type.id === nextTypeId)) {
      nextTypeId = this.lookupTypes[0]?.id || null;
    }
    this.selectedLookupTypeId = nextTypeId;
    const selectedType = this.lookupTypes.find((type) => type.id === this.selectedLookupTypeId) || null;
    if (selectedType) { this.loadLookupValues(selectedType.code); } else { this.lookupValues = []; }
  }

  private screenItemTypeLabel(itemType?: string | null): string {
    const keys: Record<string, string> = {
      item: 'ACCESS_MANAGEMENT.MENU_ITEM_TYPE_ITEM',
      group: 'ACCESS_MANAGEMENT.MENU_ITEM_TYPE_GROUP',
      collapse: 'ACCESS_MANAGEMENT.MENU_ITEM_TYPE_COLLAPSE'
    };
    const k = keys[itemType || 'item'];
    return k ? this.translationService.instant(k) : itemType || '';
  }

  private buildPermissionDraft(permissions: AdminRolePermission[] = []): PermissionDraft[] {
    const permissionMap = new Map(permissions.map((item) => [item.menuItemId, item]));
    return Array.from(this.menuItems.entries()).map(([menuItemId, item]) => {
      const permission = permissionMap.get(menuItemId);
      return { menuItemId, titleKey: item.titleKey, url: item.url, parentId: item.parentId, canView: !!permission?.canView, canCreate: !!permission?.canCreate, canEdit: !!permission?.canEdit, canDelete: !!permission?.canDelete };
    });
  }
}
