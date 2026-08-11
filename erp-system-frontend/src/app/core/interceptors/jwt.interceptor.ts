import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { CompanyContextService } from '../services/company-context.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private companyContext: CompanyContextService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const lang = localStorage.getItem('erp_language') === 'en' ? 'en' : 'ar';
    const token = this.authService.token;
    const headers: Record<string, string> = { 'Accept-Language': lang };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
      if (this.authService.activeRole) {
        headers['X-Active-Role'] = this.authService.activeRole;
      }
      const companyId = this.companyContext.activeCompanyId;
      if (companyId && !req.url.includes('/organizations/companies/accessible')) {
        headers['X-Company-Id'] = String(companyId);
      }
    }
    return next.handle(req.clone({ setHeaders: headers }));
  }
}
