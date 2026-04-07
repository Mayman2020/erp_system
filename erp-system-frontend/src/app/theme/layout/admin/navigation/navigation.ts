import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';
import { ApiResponse } from '../../../../core/models/api.models';

export interface NavigationItem {
  id: string;
  title: string;
  type: 'item' | 'collapse' | 'group';
  collapsible?: boolean;
  translate?: string;
  icon?: string;
  hidden?: boolean;
  url?: string;
  classes?: string;
  exactMatch?: boolean;
  external?: boolean;
  target?: boolean;
  breadcrumbs?: boolean;
  function?: any;
  badge?: {
    title?: string;
    type?: string;
  };
  children?: Navigation[];
}

export interface Navigation extends NavigationItem {
  children?: NavigationItem[];
}

@Injectable({ providedIn: 'root' })
export class NavigationService {
  private readonly endpoint = `${environment.apiUrl}/ui/menu`;
  private readonly storageKey = 'erp_ui_menu_cache';
  private menu$?: Observable<Navigation[]>;

  constructor(private http: HttpClient) {}

  public get(): Observable<Navigation[]> {
    if (!this.menu$) {
      const cached = this.readCache();
      this.menu$ = this.http.get<ApiResponse<Navigation[]>>(this.endpoint).pipe(
        map((response) => this.normalize(response.data || [])),
        tap((items) => this.writeCache(items)),
        catchError(() => of(cached)),
        shareReplay(1)
      );
    }

    return this.menu$;
  }

  public refresh(): Observable<Navigation[]> {
    this.menu$ = undefined;
    return this.get();
  }

  private normalize(items: Navigation[] | null | undefined): Navigation[] {
    return (items || []).map((item) => ({
      ...item,
      classes: item.classes || 'nav-item',
      children: this.normalize(item.children)
    }));
  }

  private readCache(): Navigation[] {
    try {
      const raw = localStorage.getItem(this.storageKey);
      return raw ? this.normalize(JSON.parse(raw) as Navigation[]) : [];
    } catch {
      return [];
    }
  }

  private writeCache(items: Navigation[]): void {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(items));
    } catch {
      // Ignore storage quota/privacy mode issues and keep runtime menu.
    }
  }
}
