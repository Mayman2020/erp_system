import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Runs HttpClient next/error/complete inside the Angular zone so views update immediately
 * (dialog close, table refresh, workflow buttons) without waiting for the next user event.
 */
@Injectable()
export class HttpNgZoneInterceptor implements HttpInterceptor {
  constructor(private readonly ngZone: NgZone) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return new Observable<HttpEvent<unknown>>((observer) => {
      const sub = next.handle(req).subscribe({
        next: (event) => this.ngZone.run(() => observer.next(event)),
        error: (err) => this.ngZone.run(() => observer.error(err)),
        complete: () => this.ngZone.run(() => observer.complete())
      });
      return () => sub.unsubscribe();
    });
  }
}
