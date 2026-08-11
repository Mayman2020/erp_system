import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  PartnerDto,
  PartnerForm,
  PartnerTransactionDto,
  PartnerTransactionForm,
  ProfitDistributionDto,
  ProfitDistributionForm
} from '../models/partners.models';
import { ApiResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class PartnersApiService {
  private readonly base = `${environment.apiUrl}/partners`;

  constructor(private http: HttpClient) {}

  getPartners(): Observable<PartnerDto[]> {
    return this.http.get<ApiResponse<PartnerDto[]>>(this.base).pipe(map((res) => res.data));
  }

  getPartner(id: number): Observable<PartnerDto> {
    return this.http.get<ApiResponse<PartnerDto>>(`${this.base}/${id}`).pipe(map((res) => res.data));
  }

  createPartner(payload: PartnerForm): Observable<PartnerDto> {
    return this.http.post<ApiResponse<PartnerDto>>(this.base, payload).pipe(map((res) => res.data));
  }

  updatePartner(id: number, payload: PartnerForm): Observable<PartnerDto> {
    return this.http.put<ApiResponse<PartnerDto>>(`${this.base}/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePartner(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`).pipe(map(() => undefined));
  }

  getTransactions(partnerId?: number): Observable<PartnerTransactionDto[]> {
    let params = new HttpParams();
    if (partnerId != null) {
      params = params.set('partnerId', String(partnerId));
    }
    return this.http
      .get<ApiResponse<PartnerTransactionDto[]>>(`${this.base}/transactions`, { params })
      .pipe(map((res) => res.data));
  }

  getTransaction(id: number): Observable<PartnerTransactionDto> {
    return this.http.get<ApiResponse<PartnerTransactionDto>>(`${this.base}/transactions/${id}`).pipe(map((res) => res.data));
  }

  createTransaction(payload: PartnerTransactionForm): Observable<PartnerTransactionDto> {
    return this.http.post<ApiResponse<PartnerTransactionDto>>(`${this.base}/transactions`, payload).pipe(map((res) => res.data));
  }

  updateTransaction(id: number, payload: PartnerTransactionForm): Observable<PartnerTransactionDto> {
    return this.http.put<ApiResponse<PartnerTransactionDto>>(`${this.base}/transactions/${id}`, payload).pipe(map((res) => res.data));
  }

  approveTransaction(id: number, actor: string): Observable<PartnerTransactionDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http
      .post<ApiResponse<PartnerTransactionDto>>(`${this.base}/transactions/${id}/approve`, {}, { params })
      .pipe(map((res) => res.data));
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/transactions/${id}`).pipe(map(() => undefined));
  }

  getDistributions(): Observable<ProfitDistributionDto[]> {
    return this.http.get<ApiResponse<ProfitDistributionDto[]>>(`${this.base}/distributions`).pipe(map((res) => res.data));
  }

  getDistribution(id: number): Observable<ProfitDistributionDto> {
    return this.http.get<ApiResponse<ProfitDistributionDto>>(`${this.base}/distributions/${id}`).pipe(map((res) => res.data));
  }

  createDistribution(payload: ProfitDistributionForm): Observable<ProfitDistributionDto> {
    return this.http.post<ApiResponse<ProfitDistributionDto>>(`${this.base}/distributions`, payload).pipe(map((res) => res.data));
  }

  updateDistribution(id: number, payload: ProfitDistributionForm): Observable<ProfitDistributionDto> {
    return this.http.put<ApiResponse<ProfitDistributionDto>>(`${this.base}/distributions/${id}`, payload).pipe(map((res) => res.data));
  }

  approveDistribution(id: number, actor: string): Observable<ProfitDistributionDto> {
    const params = new HttpParams().set('actor', actor);
    return this.http
      .post<ApiResponse<ProfitDistributionDto>>(`${this.base}/distributions/${id}/approve`, {}, { params })
      .pipe(map((res) => res.data));
  }

  deleteDistribution(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/distributions/${id}`).pipe(map(() => undefined));
  }
}
