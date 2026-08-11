import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AlertEventDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-alerts-page',
  templateUrl: './alerts-page.component.html',
  styleUrls: ['./alerts-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlertsPageComponent implements OnInit {
  loading = false;
  rows: AlertEventDto[] = [];
  readonly columns: DataTableColumn[] = [
    { key: 'title', title: 'COMMON.TITLE' },
    { key: 'severity', title: 'ALERTS.SEVERITY', kind: 'status' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'createdAt', title: 'COMMON.CREATED_AT', kind: 'date' }
  ];
  readonly actions: DataTableAction[] = [
    { id: 'ack', labelKey: 'ALERTS.ACKNOWLEDGE', className: 'erp-action-success', disabledWhen: (r) => String(r['status']) === 'ACKNOWLEDGED' }
  ];

  get tableRows(): Record<string, unknown>[] {
    return this.rows.map((row) => ({ ...row }));
  }

  constructor(private api: ErpApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.load(); }

  onAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'ack' && id) {
      this.api.acknowledgeAlert(id).subscribe({ next: () => this.load() });
    }
  }

  private load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.api.getAlerts().subscribe({
      next: (rows) => { this.rows = rows; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }
}
