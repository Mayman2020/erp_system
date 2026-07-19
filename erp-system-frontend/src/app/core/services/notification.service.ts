import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';
import { HttpClient } from '@angular/common/http';

export interface NotificationItem {
  id: number;
  type: string;
  titleKey: string;
  bodyKey: string;
  varsJson?: string | null;
  referenceType?: string | null;
  referenceId?: number | null;
  read: boolean;
  readAt?: string | null;
  createdAt?: string | null;
}

export interface NotificationPage {
  content: NotificationItem[];
  totalElements: number;
  page: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  constructor(private readonly http: HttpClient) {}

  list(page = 0, size = 20): Observable<NotificationPage> {
    return this.http
      .get<ApiResponse<NotificationPage>>(`${environment.apiUrl}/notifications/my`, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  unreadCount(): Observable<number> {
    return this.http
      .get<ApiResponse<{ unreadCount: number }>>(`${environment.apiUrl}/notifications/my/unread-count`)
      .pipe(map((res) => res.data?.unreadCount ?? 0));
  }

  markRead(id: number): Observable<NotificationItem> {
    return this.http
      .patch<ApiResponse<NotificationItem>>(`${environment.apiUrl}/notifications/${id}/read`, {})
      .pipe(map((res) => res.data));
  }

  markAllRead(): Observable<void> {
    return this.http
      .patch<ApiResponse<void>>(`${environment.apiUrl}/notifications/my/read-all`, {})
      .pipe(map(() => undefined));
  }
}
