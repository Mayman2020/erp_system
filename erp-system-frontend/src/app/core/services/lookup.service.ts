import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';
import { LookupItem } from '../models/lookup.models';

@Injectable({ providedIn: 'root' })
export class LookupService {
  private readonly cache = new Map<string, Observable<LookupItem[]>>();

  constructor(private http: HttpClient) {}

  getLookup(type: string): Observable<LookupItem[]> {
    if (!this.cache.has(type)) {
      const request$ = this.http
        .get<ApiResponse<LookupItem[]>>(`${environment.apiUrl}/lookups/${type}`)
        .pipe(
          map((response) => response.data || []),
          shareReplay(1)
        );
      this.cache.set(type, request$);
    }
    return this.cache.get(type) as Observable<LookupItem[]>;
  }

  clear(type?: string): void {
    if (type) {
      this.cache.delete(type);
      return;
    }
    this.cache.clear();
  }
}
