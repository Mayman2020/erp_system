import { Component, Inject, OnDestroy } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

export interface LovSelectDialogData {
  title: string;
  items: unknown[];
  selectedValue?: unknown;
  bindValue?: string;
  getItemLabel: (item: unknown) => string;
  getItemValue: (item: unknown) => unknown;
  onSearch?: (query: string) => void;
  serverSide?: boolean;
}

@Component({
  standalone: false,
  selector: 'app-lov-select-dialog',
  template: `
    <h2 mat-dialog-title class="erp-lov-dialog__title">
      <mat-icon aria-hidden="true">search</mat-icon>
      {{ data.title | translate }}
    </h2>

    <mat-dialog-content class="erp-lov-dialog__body">
      <div class="erp-lov-dialog__search">
        <mat-icon aria-hidden="true">search</mat-icon>
        <input
          type="text"
          [formControl]="searchControl"
          [placeholder]="'LOV.SEARCH_PLACEHOLDER' | translate"
          autocomplete="off">
      </div>

      <div class="erp-lov-dialog__loading" *ngIf="loading">
        <mat-spinner diameter="36"></mat-spinner>
      </div>

      <div class="erp-lov-dialog__table-wrap" *ngIf="!loading">
        <table class="erp-lov-dialog__table" *ngIf="visibleItems.length; else emptyTpl">
          <tbody>
            <tr
              *ngFor="let item of visibleItems"
              [class.selected]="isSelected(item)"
              tabindex="0"
              (click)="pick(item)"
              (keydown.enter)="pick(item)">
              <td>{{ data.getItemLabel(item) }}</td>
              <td class="erp-lov-dialog__pick-col">
                <mat-icon *ngIf="isSelected(item)" aria-hidden="true">check_circle</mat-icon>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <ng-template #emptyTpl>
        <div class="erp-lov-dialog__empty">{{ 'COMMON.NO_DATA' | translate }}</div>
      </ng-template>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-stroked-button type="button" (click)="close()">{{ 'COMMON.CANCEL' | translate }}</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .erp-lov-dialog__body { display: grid; gap: 14px; min-width: min(680px, 92vw); padding-top: 4px !important; }
    .erp-lov-dialog__title { display: flex; align-items: center; gap: 8px; margin: 0; }
    .erp-lov-dialog__search {
      display: flex; align-items: center; gap: 8px; height: 42px; padding: 0 12px;
      border: 1px solid var(--border-color, var(--erp-border)); border-radius: var(--radius-md, 8px);
      background: var(--bg-muted, var(--erp-bg-muted));
    }
    .erp-lov-dialog__search input { flex: 1; border: none; background: transparent; outline: none; color: var(--text-primary, var(--erp-text)); }
    .erp-lov-dialog__table-wrap { max-height: 52vh; overflow: auto; border: 1px solid var(--border-color, var(--erp-border)); border-radius: var(--radius-md, 8px); }
    .erp-lov-dialog__table { width: 100%; border-collapse: collapse; }
    .erp-lov-dialog__table tr { cursor: pointer; border-bottom: 1px solid var(--border-color, var(--erp-border)); }
    .erp-lov-dialog__table tr:hover, .erp-lov-dialog__table tr.selected { background: color-mix(in srgb, var(--color-primary, var(--erp-info)) 8%, transparent); }
    .erp-lov-dialog__table td { padding: 10px 12px; font-size: 0.88rem; }
    .erp-lov-dialog__pick-col { width: 44px; text-align: center; color: var(--color-primary, var(--erp-info)); }
    .erp-lov-dialog__empty, .erp-lov-dialog__loading { padding: 24px; text-align: center; color: var(--text-muted, var(--erp-text-soft)); }
  `]
})
export class LovSelectDialogComponent implements OnDestroy {
  readonly searchControl = new FormControl('', { nonNullable: true });
  visibleItems: unknown[] = [];
  loading = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly dialogRef: MatDialogRef<LovSelectDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: LovSelectDialogData
  ) {
    this.visibleItems = [...(data.items || [])];
    this.searchControl.valueChanges.pipe(debounceTime(250), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe((query) => {
      if (data.serverSide && data.onSearch) {
        data.onSearch(query);
        return;
      }
      const q = query.trim().toLowerCase();
      this.visibleItems = (data.items || []).filter((item) => !q || data.getItemLabel(item).toLowerCase().includes(q));
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  updateItems(items: unknown[]): void {
    this.data.items = items;
    const q = this.searchControl.value.trim().toLowerCase();
    this.visibleItems = (items || []).filter((item) => !q || this.data.getItemLabel(item).toLowerCase().includes(q));
  }

  isSelected(item: unknown): boolean {
    return String(this.data.getItemValue(item)) === String(this.data.selectedValue ?? '');
  }

  pick(item: unknown): void {
    this.dialogRef.close(item);
  }

  close(): void {
    this.dialogRef.close();
  }
}
