import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/models/api.models';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-capability-list-page',
  template: `
    <section class="app-page" dir="rtl">
      <header class="page-header"><h1>{{ titleKey | translate }}</h1></header>
      <div class="app-card table-card p-3">
        <div *ngIf="error">{{ error }}</div>
        <pre *ngIf="!error">{{ rows | json }}</pre>
        <div *ngIf="!error && !rows?.length">{{ 'COMMON.NO_DATA' | translate }}</div>
      </div>
    </section>
  `
})
export class CapabilityListPageComponent implements OnInit {
  titleKey = 'COMMON.LOADING';
  apiPath = '';
  rows: any[] = [];
  error = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    // filled by route data via dynamic component factories in module routes
  }

  load(titleKey: string, apiPath: string): void {
    this.titleKey = titleKey;
    this.apiPath = apiPath;
    this.http.get<ApiResponse<any>>(`${environment.apiUrl}${apiPath}`)
      .pipe(map((r) => r.data))
      .subscribe({
        next: (data) => { this.rows = Array.isArray(data) ? data : (data ? [data] : []); },
        error: (err) => { this.error = err?.error?.message || 'COMMON.ERROR_LOADING'; this.rows = []; }
      });
  }
}
