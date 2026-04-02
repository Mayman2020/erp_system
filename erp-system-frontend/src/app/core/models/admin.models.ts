export interface AdminMenuItem {
  id: string;
  parentId?: string | null;
  sortOrder?: number;
  itemType: string;
  titleKey: string;
  icon?: string | null;
  url?: string | null;
}

export interface AdminRolePermission {
  menuItemId: string;
  titleKey: string;
  url?: string | null;
  itemType?: string | null;
  parentId?: string | null;
  sortOrder?: number | null;
  canView: boolean;
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

export interface AdminRole {
  id: number;
  code: string;
  nameEn: string;
  nameAr?: string | null;
  active: boolean;
  systemRole: boolean;
  permissions: AdminRolePermission[];
}

export interface AdminRoleForm {
  code: string;
  nameEn: string;
  nameAr?: string | null;
  active: boolean;
  permissions: Array<{
    menuItemId: string;
    canView: boolean;
    canCreate: boolean;
    canEdit: boolean;
    canDelete: boolean;
  }>;
}

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  phone: string;
  primaryRole: string;
  active: boolean;
  fullName?: string | null;
  createdAt?: string;
  roleIds: number[];
  roleCodes: string[];
}

export interface AdminUserForm {
  username: string;
  email: string;
  phone: string;
  password?: string;
  fullName: string;
  primaryRole: 'ADMIN' | 'ACCOUNTANT';
  active: boolean;
  roleIds: number[];
}

export interface AdminAccessContext {
  users: AdminUser[];
  roles: AdminRole[];
  menuItems: AdminMenuItem[];
}

export interface UiPermission {
  menuItemId: string;
  canView: boolean;
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

export interface AdminLookupType {
  id: number;
  code: string;
  nameEn: string;
  nameAr?: string | null;
  sortOrder: number;
  active: boolean;
}

export interface AdminLookupTypeForm {
  code: string;
  nameEn: string;
  nameAr?: string | null;
  sortOrder: number;
  active: boolean;
}

export interface AdminLookupValue {
  id: number;
  typeCode: string;
  code: string;
  nameEn?: string | null;
  nameAr?: string | null;
  sortOrder: number;
  active: boolean;
}

export interface AdminLookupValueForm {
  typeCode: string;
  code: string;
  nameEn?: string | null;
  nameAr?: string | null;
  sortOrder: number;
  active: boolean;
}
