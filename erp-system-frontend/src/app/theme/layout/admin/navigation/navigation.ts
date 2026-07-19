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
  private readonly storageKey = 'erp_ui_menu_cache_v2';
  private menu$?: Observable<Navigation[]>;

  constructor(private http: HttpClient) {}

  public get(): Observable<Navigation[]> {
    if (!this.menu$) {
      const cached = this.readCache();
      this.menu$ = this.http.get<ApiResponse<Navigation[]>>(this.endpoint).pipe(
        map((response) => this.normalize(response.data || [])),
        tap((items) => this.writeCache(items)),
        catchError(() => of(cached.length ? cached : this.fallbackMenu())),
        shareReplay(1)
      );
    }

    return this.menu$;
  }

  /** Minimal safe menu shown when `/ui/menu` fails and no prior successful fetch was cached
   * (first login on a fresh browser/profile, or right after a cache-clearing deploy). */
  private fallbackMenu(): Navigation[] {
    return [
      { id: 'dashboard', title: 'NAV.DASHBOARD', translate: 'NAV.DASHBOARD', type: 'item', icon: 'dashboard', url: '/dashboard', classes: 'nav-item' },
      { id: 'settings', title: 'NAV.SETTINGS', translate: 'NAV.SETTINGS', type: 'item', icon: 'tune', url: '/settings', classes: 'nav-item' },
      { id: 'admin', title: 'NAV.SYSTEM_MANAGEMENT', translate: 'NAV.SYSTEM_MANAGEMENT', type: 'item', icon: 'admin_panel_settings', url: '/admin', classes: 'nav-item' }
    ];
  }

  public refresh(): Observable<Navigation[]> {
    this.menu$ = undefined;
    return this.get();
  }

  private normalize(items: Navigation[] | null | undefined): Navigation[] {
    return (items || []).map((item) => ({
      ...item,
      type: this.normalizeType(item.type),
      classes: item.classes || 'nav-item',
      children: this.normalize(item.children)
    }));
  }

  private normalizeType(type: NavigationItem['type'] | string | undefined): NavigationItem['type'] {
    const value = (type || 'item').toString().toLowerCase();
    if (value === 'group' || value === 'collapse' || value === 'item') {
      return value;
    }
    return 'item';
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
