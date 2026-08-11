import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { BackupJobDto } from '../core/models/erp.models';
import { ErpApiService } from '../core/services/erp-api.service';
import { DataTableAction, DataTableColumn } from '../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-backups-page',
  templateUrl: './backups-page.component.html',
  styleUrls: ['./backups-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BackupsPageComponent implements OnInit {
  loading = false;
  creating = false;
  rows: BackupJobDto[] = [];
  readonly columns: DataTableColumn[] = [
    { key: 'jobNo', title: 'ERP.NUMBER' },
    { key: 'status', title: 'BACKUPS.STATUS', kind: 'status' },
    { key: 'fileSizeBytes', title: 'BACKUPS.SIZE', align: 'end' },
    { key: 'createdAt', title: 'COMMON.CREATED_AT', kind: 'date' }
  ];
  readonly actions: DataTableAction[] = [
    { id: 'download', labelKey: 'BACKUPS.DOWNLOAD', className: 'erp-action-info', disabledWhen: (r) => !r['downloadable'] }
  ];

  constructor(private api: ErpApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.load(); }

  get tableRows(): Record<string, unknown>[] {
    return this.rows.map((row) => ({ ...row }));
  }

  createBackup(): void {
    this.creating = true;
    this.cdr.markForCheck();
    this.api.createBackup().subscribe({
      next: () => { this.creating = false; this.load(); },
      error: () => { this.creating = false; this.cdr.markForCheck(); }
    });
  }

  onAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'download' && id) {
      this.api.downloadBackup(id).subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const anchor = document.createElement('a');
          anchor.href = url;
          anchor.download = `backup-${id}.sql`;
          anchor.click();
          URL.revokeObjectURL(url);
        }
      });
    }
  }

  private load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.api.getBackups().subscribe({
      next: (rows) => { this.rows = rows; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }
}
