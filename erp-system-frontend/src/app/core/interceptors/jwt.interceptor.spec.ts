import { HttpHandler, HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { JwtInterceptor } from './jwt.interceptor';
import { CompanyContextService } from '../services/company-context.service';

describe('JwtInterceptor', () => {
  it('sends the validated active role with the bearer token', () => {
    const auth = {
      token: 'signed-token',
      activeRole: 'REPORT_VIEWER'
    } as AuthService;
    const companyContext = { activeCompanyId: 3 } as CompanyContextService;
    const interceptor = new JwtInterceptor(auth, companyContext);
    let forwarded: HttpRequest<unknown> | undefined;
    const next = {
      handle: (request: HttpRequest<unknown>) => {
        forwarded = request;
        return of(new HttpResponse({ status: 200 }));
      }
    } as HttpHandler;

    interceptor.intercept(new HttpRequest('GET', '/api/v1/ui/menu/permissions'), next).subscribe();

    expect(forwarded?.headers.get('Authorization')).toBe('Bearer signed-token');
    expect(forwarded?.headers.get('X-Active-Role')).toBe('REPORT_VIEWER');
    expect(forwarded?.headers.get('X-Company-Id')).toBe('3');
  });
});
