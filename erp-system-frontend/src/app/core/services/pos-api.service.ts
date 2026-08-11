import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';
import {
  PosOfflineSyncForm,
  PosOfflineSyncResultDto,
  PosSaleDto,
  PosSaleForm,
  PosShiftCloseForm,
  PosShiftDto,
  PosShiftOpenForm,
  PosTerminalDto
} from '../models/erp.models';

@Injectable({ providedIn: 'root' })
export class PosApiService {
  private readonly base = `${environment.apiUrl}/pos`;

  constructor(private http: HttpClient) {}

  getTerminals(): Observable<PosTerminalDto[]> {
    return this.http
      .get<ApiResponse<PosTerminalDto[]>>(`${this.base}/terminals`)
      .pipe(map((res) => res.data || []));
  }

  /** @deprecated use getTerminals */
  listTerminals(): Observable<PosTerminalDto[]> {
    return this.getTerminals();
  }

  getTerminal(id: number): Observable<PosTerminalDto> {
    return this.http
      .get<ApiResponse<PosTerminalDto>>(`${this.base}/terminals/${id}`)
      .pipe(map((res) => res.data));
  }

  getShifts(filters: Record<string, string | number> = {}): Observable<PosShiftDto[]> {
    return this.http
      .get<ApiResponse<PosShiftDto[]>>(`${this.base}/shifts`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  /** @deprecated use getShifts */
  listShifts(): Observable<PosShiftDto[]> {
    return this.getShifts();
  }

  getOpenShift(terminalId?: number, cashierUserId?: number): Observable<PosShiftDto | null> {
    let params = new HttpParams();
    if (terminalId != null) {
      params = params.set('terminalId', String(terminalId));
    }
    if (cashierUserId != null) {
      params = params.set('cashierUserId', String(cashierUserId));
    }
    return this.http
      .get<ApiResponse<PosShiftDto | null>>(`${this.base}/shifts/open`, { params })
      .pipe(map((res) => res.data ?? null));
  }

  /** @deprecated use getOpenShift */
  getCurrentShift(cashierUserId?: number): Observable<PosShiftDto | null> {
    return this.getOpenShift(undefined, cashierUserId);
  }

  getShift(id: number): Observable<PosShiftDto> {
    return this.http
      .get<ApiResponse<PosShiftDto>>(`${this.base}/shifts/${id}`)
      .pipe(map((res) => res.data));
  }

  openShift(payload: PosShiftOpenForm): Observable<PosShiftDto> {
    return this.http
      .post<ApiResponse<PosShiftDto>>(`${this.base}/shifts/open`, payload)
      .pipe(map((res) => res.data));
  }

  closeShift(id: number, payload: PosShiftCloseForm): Observable<PosShiftDto> {
    return this.http
      .post<ApiResponse<PosShiftDto>>(`${this.base}/shifts/${id}/close`, payload)
      .pipe(map((res) => res.data));
  }

  getSales(shiftId: number): Observable<PosSaleDto[]> {
    return this.http
      .get<ApiResponse<PosSaleDto[]>>(`${this.base}/sales`, { params: { shiftId: String(shiftId) } })
      .pipe(map((res) => res.data || []));
  }

  getSale(id: number): Observable<PosSaleDto> {
    return this.http
      .get<ApiResponse<PosSaleDto>>(`${this.base}/sales/${id}`)
      .pipe(map((res) => res.data));
  }

  createSale(payload: PosSaleForm): Observable<PosSaleDto> {
    return this.http
      .post<ApiResponse<PosSaleDto>>(`${this.base}/sales`, payload)
      .pipe(map((res) => res.data));
  }

  syncOfflineBatch(payload: PosOfflineSyncForm): Observable<PosOfflineSyncResultDto> {
    return this.http
      .post<ApiResponse<PosOfflineSyncResultDto>>(`${this.base}/offline/sync`, payload)
      .pipe(map((res) => res.data));
  }

  /** @deprecated use syncOfflineBatch */
  syncOffline(payload: PosOfflineSyncForm): Observable<PosOfflineSyncResultDto> {
    return this.syncOfflineBatch(payload);
  }

  private toParams(filters: Record<string, string | number>): HttpParams {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return params;
  }
}
