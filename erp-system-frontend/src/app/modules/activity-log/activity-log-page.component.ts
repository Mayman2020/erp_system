import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivityLogDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-activity-log-page',
  templateUrl: './activity-log-page.component.html',
  styleUrls: ['./activity-log-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActivityLogPageComponent implements OnInit {
  readonly titleKey = 'MENU.ACTIVITY_LOG';
  readonly columns: DataTableColumn[] = [
    { key: 'createdAt', title: 'COMMON.DATE', kind: 'date' },
    { key: 'moduleName', title: 'ERP.MODULE' },
    { key: 'actionType', title: 'ERP.ACTION' },
    { key: 'entityType', title: 'ERP.ENTITY_TYPE' },
    { key: 'entityReference', title: 'ERP.REFERENCE' },
    { key: 'description', title: 'COMMON.DESCRIPTION' },
    { key: 'actor', title: 'ERP.ACTOR' }
  ];

  rows: Array<Record<string, unknown>> = [];
  loading = false;
  page = 0;
  readonly pageSize = 25;

  constructor(private api: ErpApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getActivityLogs(this.page, this.pageSize).subscribe({
      next: (data) => {
        this.rows = (data.items || []).map((i) => ({ ...i }));
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  nextPage(): void {
    this.page += 1;
    this.load();
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page -= 1;
      this.load();
    }
  }
}
