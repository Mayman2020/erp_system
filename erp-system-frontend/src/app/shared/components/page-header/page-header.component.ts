import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { Subscription } from 'rxjs';
import { NavigationHistoryService } from '../../../core/services/navigation-history.service';

@Component({ standalone: false,
  selector: 'app-page-header',
  template: `
    <header class="app-page-header erp-page-header" role="banner">
      <div class="page-heading erp-page-header__copy">
        <div class="erp-page-header__title-row">
          <button
            *ngIf="showBackButton"
            type="button"
            class="erp-icon-button erp-page-header__back"
            [ngbTooltip]="'COMMON.BACK' | translate"
            placement="top"
            container="body"
            [attr.aria-label]="'COMMON.BACK' | translate"
            (click)="onBack()">
            <mat-icon aria-hidden="true">arrow_back</mat-icon>
          </button>
          <div class="erp-page-header__titles">
            <span class="app-page-eyebrow erp-page-header__eyebrow" *ngIf="eyebrowKey">{{ eyebrowKey | translate }}</span>
            <h1 class="app-page-title">{{ titleKey | translate }}</h1>
          </div>
          <ng-content select="[header-meta]"></ng-content>
        </div>
        <p class="app-page-subtitle" *ngIf="subtitleKey">{{ subtitleKey | translate }}</p>
      </div>
      <div class="page-actions erp-page-header__actions">
        <ng-content select="[header-actions]"></ng-content>
      </div>
    </header>
  `,
  styles: [`
    .erp-page-header__title-row {
      display: flex;
      align-items: flex-start;
      gap: 10px;
    }
    .erp-page-header__actions {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
      flex-shrink: 0;
    }
    :host ::ng-deep [header-actions] {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    .erp-page-header__back {
      margin-top: 2px;
      flex: 0 0 auto;
    }
    .erp-page-header__titles {
      min-width: 0;
    }
  `]
})
export class PageHeaderComponent implements OnInit, OnDestroy {
  @Input() titleKey = '';
  @Input() subtitleKey = '';
  @Input() eyebrowKey = '';
  @Input() showBack = false;

  showBackButton = false;
  private sub?: Subscription;

  constructor(
    private readonly navHistory: NavigationHistoryService,
    private readonly location: Location
  ) {}

  ngOnInit(): void {
    this.showBackButton = this.showBack || this.navHistory.canGoBack();
    this.sub = this.navHistory.canGoBack$.subscribe((canGo) => {
      this.showBackButton = this.showBack || canGo;
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  onBack(): void {
    this.navHistory.goBack(this.location);
  }
}
