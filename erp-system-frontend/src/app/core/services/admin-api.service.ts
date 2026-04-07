import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';
import {
  AdminAccessContext,
  AdminLookupType,
  AdminLookupTypeForm,
  AdminLookupValue,
  AdminLookupValueForm,
  AdminRole,
  AdminRoleForm,
  AdminUser,
  AdminUserForm,
  UiMenuItemAdmin,
  UiMenuItemAdminForm,
  UiPermission
} from '../models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly accessBase = `${environment.apiUrl}/admin/access`;
  private readonly lookupsBase = `${environment.apiUrl}/admin/lookups`;
  private readonly uiBase = `${environment.apiUrl}/ui/menu`;

  constructor(private http: HttpClient) {}

  getAccessContext(): Observable<AdminAccessContext> {
    return this.http.get<ApiResponse<AdminAccessContext>>(`${this.accessBase}/context`).pipe(map((res) => res.data));
  }

  getUsers(): Observable<AdminUser[]> {
    return this.http.get<ApiResponse<AdminUser[]>>(`${this.accessBase}/users`).pipe(map((res) => res.data || []));
  }

  createUser(payload: AdminUserForm): Observable<AdminUser> {
    return this.http.post<ApiResponse<AdminUser>>(`${this.accessBase}/users`, payload).pipe(map((res) => res.data));
  }

  updateUser(userId: number, payload: AdminUserForm): Observable<AdminUser> {
    return this.http.put<ApiResponse<AdminUser>>(`${this.accessBase}/users/${userId}`, payload).pipe(map((res) => res.data));
  }

  setUserActive(userId: number, active: boolean): Observable<AdminUser> {
    return this.http
      .patch<ApiResponse<AdminUser>>(`${this.accessBase}/users/${userId}/active`, { active })
      .pipe(map((res) => res.data));
  }

  getRoles(): Observable<AdminRole[]> {
    return this.http.get<ApiResponse<AdminRole[]>>(`${this.accessBase}/roles`).pipe(map((res) => res.data || []));
  }

  createRole(payload: AdminRoleForm): Observable<AdminRole> {
    return this.http.post<ApiResponse<AdminRole>>(`${this.accessBase}/roles`, payload).pipe(map((res) => res.data));
  }

  updateRole(roleId: number, payload: AdminRoleForm): Observable<AdminRole> {
    return this.http.put<ApiResponse<AdminRole>>(`${this.accessBase}/roles/${roleId}`, payload).pipe(map((res) => res.data));
  }

  deleteRole(roleId: number): Observable<void> {
    return this.http.delete<ApiResponse<unknown>>(`${this.accessBase}/roles/${roleId}`).pipe(map(() => undefined));
  }

  listMenuItems(): Observable<UiMenuItemAdmin[]> {
    return this.http.get<ApiResponse<UiMenuItemAdmin[]>>(`${environment.apiUrl}/admin/ui/menu-items`).pipe(map((res) => res.data || []));
  }

  createMenuItem(payload: UiMenuItemAdminForm): Observable<UiMenuItemAdmin> {
    return this.http.post<ApiResponse<UiMenuItemAdmin>>(`${environment.apiUrl}/admin/ui/menu-items`, payload).pipe(map((res) => res.data));
  }

  updateMenuItem(menuItemId: string, payload: UiMenuItemAdminForm): Observable<UiMenuItemAdmin> {
    return this.http
      .put<ApiResponse<UiMenuItemAdmin>>(`${environment.apiUrl}/admin/ui/menu-items/${encodeURIComponent(menuItemId)}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteMenuItem(menuItemId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<unknown>>(`${environment.apiUrl}/admin/ui/menu-items/${encodeURIComponent(menuItemId)}`)
      .pipe(map(() => undefined));
  }

  getUiPermissions(): Observable<UiPermission[]> {
    return this.http.get<ApiResponse<UiPermission[]>>(`${this.uiBase}/permissions`).pipe(map((res) => res.data || []));
  }

  getLookupTypes(): Observable<AdminLookupType[]> {
    return this.http.get<ApiResponse<AdminLookupType[]>>(`${this.lookupsBase}/types`).pipe(map((res) => res.data || []));
  }

  createLookupType(payload: AdminLookupTypeForm): Observable<AdminLookupType> {
    return this.http.post<ApiResponse<AdminLookupType>>(`${this.lookupsBase}/types`, payload).pipe(map((res) => res.data));
  }

  updateLookupType(typeId: number, payload: AdminLookupTypeForm): Observable<AdminLookupType> {
    return this.http.put<ApiResponse<AdminLookupType>>(`${this.lookupsBase}/types/${typeId}`, payload).pipe(map((res) => res.data));
  }

  deleteLookupType(typeId: number): Observable<boolean> {
    return this.http.delete<ApiResponse<boolean>>(`${this.lookupsBase}/types/${typeId}`).pipe(map((res) => !!res.data));
  }

  getLookupValues(typeCode: string): Observable<AdminLookupValue[]> {
    const params = new HttpParams().set('typeCode', typeCode);
    return this.http.get<ApiResponse<AdminLookupValue[]>>(`${this.lookupsBase}/values`, { params }).pipe(map((res) => res.data || []));
  }

  createLookupValue(payload: AdminLookupValueForm): Observable<AdminLookupValue> {
    return this.http.post<ApiResponse<AdminLookupValue>>(`${this.lookupsBase}/values`, payload).pipe(map((res) => res.data));
  }

  updateLookupValue(valueId: number, payload: AdminLookupValueForm): Observable<AdminLookupValue> {
    return this.http.put<ApiResponse<AdminLookupValue>>(`${this.lookupsBase}/values/${valueId}`, payload).pipe(map((res) => res.data));
  }

  deleteLookupValue(valueId: number): Observable<boolean> {
    return this.http.delete<ApiResponse<boolean>>(`${this.lookupsBase}/values/${valueId}`).pipe(map((res) => !!res.data));
  }
}
