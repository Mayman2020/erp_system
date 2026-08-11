import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';

export interface ErpCompany {
  id: number;
  code: string;
  nameEn: string;
  nameAr: string;
  entityType: 'COMPANY' | 'BRANCH';
  parentId?: number | null;
  currencyCode: string;
  countryCode: string;
  defaultCompany: boolean;
}

@Injectable({ providedIn: 'root' })
export class CompanyContextService {
  private readonly storageKey = 'erp_active_company_id';
  private readonly companiesSubject = new BehaviorSubject<ErpCompany[]>([]);
  readonly companies$ = this.companiesSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  get activeCompanyId(): number | null {
    const value = localStorage.getItem(this.storageKey);
    return value && /^\d+$/.test(value) ? Number(value) : null;
  }

  get companies(): ErpCompany[] {
    return this.companiesSubject.value;
  }

  get activeCompany(): ErpCompany | null {
    return this.companies.find((company) => company.id === this.activeCompanyId) || null;
  }

  loadAccessible(): Observable<ErpCompany[]> {
    return this.http.get<ApiResponse<ErpCompany[]>>(
      `${environment.apiUrl}/organizations/companies/accessible`
    ).pipe(
      map((response) => response.data || []),
      tap((companies) => {
        this.companiesSubject.next(companies);
        const selected = this.activeCompanyId;
        if (!selected || !companies.some((company) => company.id === selected)) {
          const fallback = companies.find((company) => company.defaultCompany) || companies[0];
          if (fallback) localStorage.setItem(this.storageKey, String(fallback.id));
        }
      })
    );
  }

  switchCompany(companyId: number): boolean {
    if (!this.companies.some((company) => company.id === companyId)) return false;
    localStorage.setItem(this.storageKey, String(companyId));
    return true;
  }

  clear(): void {
    localStorage.removeItem(this.storageKey);
    this.companiesSubject.next([]);
  }
}
