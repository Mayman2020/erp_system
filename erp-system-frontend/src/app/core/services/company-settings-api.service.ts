import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';
import { CompanySettings, CompanySettingsForm } from '../models/company-settings.models';

@Injectable({ providedIn: 'root' })
export class CompanySettingsApiService {
  private readonly base = `${environment.apiUrl}/settings/company`;

  constructor(private http: HttpClient) {}

  getSettings(): Observable<CompanySettings> {
    return this.http.get<ApiResponse<CompanySettings>>(this.base).pipe(map((res) => res.data));
  }

  updateSettings(payload: CompanySettingsForm): Observable<CompanySettings> {
    return this.http.put<ApiResponse<CompanySettings>>(this.base, payload).pipe(map((res) => res.data));
  }
}
