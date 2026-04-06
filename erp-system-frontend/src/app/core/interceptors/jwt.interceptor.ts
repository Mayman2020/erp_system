import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const lang = localStorage.getItem('erp_language') === 'en' ? 'en' : 'ar';
    const token = this.authService.token;
    const headers: Record<string, string> = { 'Accept-Language': lang };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return next.handle(req.clone({ setHeaders: headers }));
  }
}
