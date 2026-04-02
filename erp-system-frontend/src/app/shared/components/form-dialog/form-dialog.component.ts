import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'app-erp-dialog, app-form-dialog',
  templateUrl: './form-dialog.component.html'
})
export class FormDialogComponent {
  @Input() visible = false;
  @Input() title = '';
  @Input() size: 'md' | 'lg' | 'xl' | 'xxl' = 'lg';
  @Output() close = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.visible) {
      this.close.emit();
    }
  }
}
