import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-profile-card',
  template: `
    <div class="erp-sidebar-profile" [class.erp-sidebar-profile--open]="menuOpen" (clickOutside)="closeMenu.emit()">
      <button
        type="button"
        class="erp-sidebar-profile__toggle"
        (click)="toggleMenu.emit()"
        [attr.aria-label]="displayName || ('PROFILE.TITLE' | translate)"
        [attr.aria-expanded]="menuOpen"
        *ngIf="!loading; else profileSkeleton"
      >
        <img [src]="avatarUrl" class="erp-sidebar-profile__avatar" [attr.alt]="'PROFILE.AVATAR_ALT' | translate" />
        <span class="erp-sidebar-profile__info">
          <strong>{{ displayName || ('NAV.HESABATY' | translate) | slice:0:18 }}</strong>
          <small>{{ roleKey | translate }}</small>
        </span>
        <mat-icon aria-hidden="true" class="erp-sidebar-profile__caret">{{ menuOpen ? 'expand_less' : 'expand_more' }}</mat-icon>
      </button>

      <ng-template #profileSkeleton>
        <div class="erp-sidebar-profile__toggle erp-sidebar-profile__skeleton">
          <div class="erp-sidebar-profile__avatar skeleton-pulse"></div>
          <div class="erp-sidebar-profile__info">
            <div class="skeleton-line skeleton-line--primary skeleton-pulse"></div>
            <div class="skeleton-line skeleton-line--secondary skeleton-pulse"></div>
          </div>
        </div>
      </ng-template>

      <div class="erp-sidebar-profile__menu" *ngIf="menuOpen" role="menu">
        <a class="erp-sidebar-profile__menu-link" [routerLink]="['/settings']" title="{{ 'PROFILE.TITLE' | translate }}" (click)="profileClick.emit()" role="menuitem">
          <mat-icon aria-hidden="true">person</mat-icon>
          <span>{{ 'PROFILE.TITLE' | translate }}</span>
        </a>
        <button type="button" class="erp-sidebar-profile__menu-link erp-sidebar-profile__menu-link--danger" (click)="logoutClick.emit()" role="menuitem">
          <mat-icon aria-hidden="true">logout</mat-icon>
          <span>{{ 'AUTH.LOGOUT' | translate }}</span>
        </button>
      </div>
    </div>
  `
})
export class ProfileCardComponent {
  @Input() displayName = '';
  @Input() roleKey = 'PROFILE.TITLE';
  @Input() avatarUrl = 'assets/images/user/avatar-1.jpg';
  @Input() loading = false;
  @Input() menuOpen = false;
  @Output() toggleMenu = new EventEmitter<void>();
  @Output() closeMenu = new EventEmitter<void>();
  @Output() profileClick = new EventEmitter<void>();
  @Output() logoutClick = new EventEmitter<void>();
}
