import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-erp-alert',
  templateUrl: './erp-alert.component.html',
  styleUrls: ['./erp-alert.component.scss']
})
export class ErpAlertComponent {
  @Input() type: 'success' | 'danger' | 'warning' | 'info' = 'danger';
  @Input() message = '';
  @Input() dismissible = true;
  @Input() extraClass = '';
  @Output() dismissed = new EventEmitter<void>();

  get icon(): string {
    switch (this.type) {
      case 'success':
        return 'check_circle';
      case 'danger':
        return 'error';
      case 'warning':
        return 'warning';
      default:
        return 'info';
    }
  }

  onDismiss(): void {
    this.dismissed.emit();
  }
}
