import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-avatar',
  standalone: false,
  template: `
    <div 
      class="erp-avatar" 
      [style.width]="size" 
      [style.height]="size"
      [class.erp-avatar--loading]="loading"
    >
      <img 
        *ngIf="src && !hasError; else fallbackTemplate"
        [src]="src" 
        [alt]="alt"
        class="erp-avatar__img"
        (error)="onImgError()"
      />
      <ng-template #fallbackTemplate>
        <div class="erp-avatar__fallback">
          <mat-icon>person</mat-icon>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    :host {
      display: inline-block;
      vertical-align: middle;
    }
    .erp-avatar {
      position: relative;
      background: var(--erp-bg-soft, #f1f5f9);
      border-radius: 50%;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      border: 1px solid var(--erp-border, rgba(0,0,0,0.05));
    }
    .erp-avatar__img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
    .erp-avatar__fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--erp-bg-muted, #f8fafc);
      color: var(--erp-text-soft, #94a3b8);
    }
    .erp-avatar__fallback mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    .erp-avatar--loading {
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-pulse 1.5s infinite;
    }
    @keyframes skeleton-pulse {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `]
})
export class AvatarComponent {
  @Input() src: string | null = null;
  @Input() size = '40px';
  @Input() alt = 'Avatar';
  @Input() loading = false;

  hasError = false;

  onImgError(): void {
    this.hasError = true;
  }
}
