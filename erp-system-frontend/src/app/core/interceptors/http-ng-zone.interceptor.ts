import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

/**
 * Ensures Angular runs change detection after HTTP completes. Some environments or
 * library combinations can deliver HttpClient callbacks outside the NgZone, which leaves
 * templates (e.g. loading flags) stale until the next user event such as a click.
 */
@Injectable()
export class HttpNgZoneInterceptor implements HttpInterceptor {
  constructor(private readonly ngZone: NgZone) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      finalize(() => {
        this.ngZone.run(() => undefined);
      })
    );
  }
}
