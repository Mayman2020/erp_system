import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DataTableAction, DataTableColumn } from '../data-table/data-table.component';
import { ListLoadController } from '../../utils/list-load.util';
import { DEFAULT_TABLE_PAGE_SIZE } from '../../../core/utils/pagination.util';

@Component({
  standalone: false,
  selector: 'app-erp-master-list-shell',
  template: `
    <div class="erp-card erp-list-page__card" *ngIf="listLoad.showInitialSpinner">
      <app-loading-state></app-loading-state>
    </div>

    <div
      class="erp-card erp-list-page__card"
      *ngIf="listLoad.showSurface"
      [class.is-refreshing]="listLoad.refreshing">
      <div class="erp-card__body">
        <ng-content select="[list-stats]"></ng-content>
        <ng-content select="[list-filters]"></ng-content>

        <app-advanced-search-bar
          *ngIf="showSearch && !hideSearchBar"
          [showDateRange]="showDateRange"
          [showStatus]="showStatus"
          [statusOptions]="statusOptions"
          [statusLabelPrefix]="statusLabelPrefix"
          (search)="search.emit($event)">
        </app-advanced-search-bar>

        <app-erp-alert
          *ngIf="errorKey && !formVisible"
          type="danger"
          [message]="errorKey"
          extraClass="mt-3"
          (dismissed)="dismissError.emit()">
        </app-erp-alert>
        <app-erp-alert
          *ngIf="successKey && !formVisible"
          type="success"
          [message]="successKey"
          extraClass="mt-3"
          (dismissed)="dismissSuccess.emit()">
        </app-erp-alert>

        <app-empty-state
          *ngIf="totalElements === 0 && !listLoad.refreshing"
          icon="table_rows"
          [titleKey]="emptyTitleKey"
          [descriptionKey]="emptyDescriptionKey">
        </app-empty-state>

        <ng-container *ngIf="totalElements > 0 || listLoad.refreshing">
          <app-data-table
            [columns]="columns"
            [data]="pagedRows"
            [actions]="actions"
            [loading]="listLoad.refreshing"
            [exportable]="false"
            [showPager]="false"
            (actionClick)="actionClick.emit($event)">
          </app-data-table>
          <app-table-pager
            [length]="totalElements"
            [pageSize]="pageSize"
            [pageIndex]="pageIndex"
            (pageIndexChange)="pageIndexChange.emit($event)">
          </app-table-pager>
        </ng-container>
      </div>
    </div>
  `
})
export class ErpMasterListShellComponent {
  @Input() listLoad!: ListLoadController;
  @Input() showSearch = true;
  @Input() showDateRange = false;
  @Input() showStatus = false;
  @Input() statusOptions: string[] = [];
  @Input() statusLabelPrefix = '';
  @Input() hideSearchBar = false;
  @Input() errorKey = '';
  @Input() successKey = '';
  @Input() formVisible = false;
  @Input() columns: DataTableColumn[] = [];
  @Input() pagedRows: Array<Record<string, unknown>> = [];
  @Input() actions: DataTableAction[] = [];
  @Input() totalElements = 0;
  @Input() pageIndex = 0;
  @Input() pageSize = DEFAULT_TABLE_PAGE_SIZE;
  @Input() emptyTitleKey = 'COMMON.NO_DATA';
  @Input() emptyDescriptionKey = 'COMMON.NO_RESULTS_HINT';

  @Output() search = new EventEmitter<Record<string, string>>();
  @Output() pageIndexChange = new EventEmitter<number>();
  @Output() actionClick = new EventEmitter<{ actionId: string; row: Record<string, unknown> }>();
  @Output() dismissError = new EventEmitter<void>();
  @Output() dismissSuccess = new EventEmitter<void>();
}
