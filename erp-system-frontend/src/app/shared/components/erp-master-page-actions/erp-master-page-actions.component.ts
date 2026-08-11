import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ExportColumn } from '../table-export-toolbar/table-export-toolbar.component';

@Component({
  standalone: false,
  selector: 'app-erp-master-page-actions',
  template: `
    <div class="erp-toolbar-icons erp-master-page-actions">
      <app-table-export-toolbar
        *ngIf="canExport"
        [inline]="true"
        [title]="titleKey | translate"
        [fileName]="exportFileName"
        [columns]="exportColumns"
        [rows]="rows"
        [showExport]="canExport"
        [permissionKey]="menuItemId"
        [disabled]="disabled">
      </app-table-export-toolbar>
      <button
        *ngIf="canCreate"
        class="erp-button erp-button--primary"
        type="button"
        (click)="create.emit()">
        <mat-icon aria-hidden="true">add</mat-icon>
        <span>{{ createKey | translate }}</span>
      </button>
      <ng-content></ng-content>
    </div>
  `,
  styles: [`
    :host {
      display: inline-flex;
      align-items: center;
    }
    .erp-master-page-actions {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }
  `]
})
export class ErpMasterPageActionsComponent {
  @Input() canCreate = true;
  @Input() canExport = true;
  @Input() createKey = 'COMMON.CREATE';
  @Input() titleKey = '';
  @Input() exportFileName = 'export';
  @Input() exportColumns: ExportColumn[] = [];
  @Input() rows: unknown[] = [];
  @Input() menuItemId = '';
  @Input() disabled = false;
  @Output() create = new EventEmitter<void>();
}
