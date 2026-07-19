import { Component, EventEmitter, HostListener, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';

@Component({ standalone: false,
  selector: 'app-erp-dialog, app-form-dialog',
  templateUrl: './form-dialog.component.html'
})
export class FormDialogComponent implements OnChanges, OnDestroy {
  private static openDialogCount = 0;
  private bodyLockRegistered = false;
  @Input() visible = false;
  /** i18n key for the dialog heading (avoid naming this `title` — conflicts with native HTML title / tooltips on the host). */
  @Input() titleKey = '';
  @Input() size: 'md' | 'lg' | 'xl' | 'xxl' | 'account' = 'lg';
  @Input() surfaceClass = '';
  @Output() close = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.visible) {
      this.close.emit();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']) {
      this.syncBodyScrollLock(this.visible);
    }
  }

  ngOnDestroy(): void {
    this.syncBodyScrollLock(false);
  }

  private syncBodyScrollLock(shouldLock: boolean): void {
    if (shouldLock && !this.bodyLockRegistered) {
      this.bodyLockRegistered = true;
      FormDialogComponent.openDialogCount += 1;
    } else if (!shouldLock && this.bodyLockRegistered) {
      this.bodyLockRegistered = false;
      FormDialogComponent.openDialogCount = Math.max(0, FormDialogComponent.openDialogCount - 1);
    }
    document.body.classList.toggle('erp-dialog-open', FormDialogComponent.openDialogCount > 0);
  }
}
