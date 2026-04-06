import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({ standalone: false,
  selector: 'app-erp-dialog, app-form-dialog',
  templateUrl: './form-dialog.component.html'
})
export class FormDialogComponent {
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
}
