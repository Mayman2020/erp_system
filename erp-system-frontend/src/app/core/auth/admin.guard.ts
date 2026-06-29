import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean> {
    const user = this.authService.currentUser;
    if (user) {
      return of(this.checkAdmin(user));
    }
    return this.authService.currentUser$.pipe(
      take(1),
      map((u) => this.checkAdmin(u))
    );
  }

  private checkAdmin(user: { role?: string; roles?: string[] } | null): boolean {
    if (!user) {
      this.router.navigate(['/auth/signin']);
      return false;
    }
    const roles = user.roles || (user.role ? [user.role] : []);
    if (roles.some((r) => r === 'ADMIN')) {
      return true;
    }
    this.router.navigate(['/dashboard']);
    return false;
  }
}
