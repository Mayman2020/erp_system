const fs = require('fs');
const base = 'd:/Apps Work/erp Project/erp-system-frontend/src/app/shared/components/account-tree-picker';

const html = <div class="account-tree-picker" [class.account-tree-picker--disabled]="disabled">
  <label class="account-tree-picker__label" *ngIf="labelKey">{{ labelKey | translate }}</label>
  <div class="account-tree-picker__control" (click)="openDialog()">
    <mat-icon class="account-tree-picker__icon" aria-hidden="true">{{ icon }}</mat-icon>
    <input
      class="erp-input account-tree-picker__input"
      [value]="displayLabel"
      readonly
      [disabled]="disabled"
      [attr.placeholder]="''"
    />
    <button
      *ngIf="nullable && selectedId && !disabled"
      type="button"
      class="account-tree-picker__clear"
      (click)="clearSelection( + '$' + event)"
    >
      <mat-icon aria-hidden="true">close</mat-icon>
    </button>
    <button type="button" class="account-tree-picker__browse" [disabled]="disabled">
      <mat-icon aria-hidden="true">search</mat-icon>
    </button>
  </div>
</div>

<app-erp-dialog [visible]="dialogVisible" [title]="labelKey" size="xl" (close)="closeDialog()">
  <div dialog-body>
    <app-tree-selector
      [nodes]="accountTree"
      [minSelectableLevel]="minSelectableLevel"
      [selectedId]="selectedId"
      [initiallyExpandedDepth]="2"
      (selected)="onNodeSelected( + '$' + event)"
    ></app-tree-selector>
  </div>
</app-erp-dialog>
;

const scss = .account-tree-picker {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &__label {
    font-size: 0.85rem;
    font-weight: 500;
    color: var(--text-secondary, #666);
  }

  &__control {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    border: 1px solid var(--border-color, #d0d5dd);
    border-radius: 8px;
    padding: 0 4px 0 8px;
    background: var(--surface-color, #fff);
    transition: border-color 0.2s;

    &:hover {
      border-color: var(--primary-color, #3f51b5);
    }
  }

  &--disabled &__control {
    cursor: not-allowed;
    opacity: 0.6;
    pointer-events: none;
  }

  &__icon {
    font-size: 20px;
    color: var(--text-secondary, #888);
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    border: none !important;
    outline: none !important;
    box-shadow: none !important;
    cursor: pointer;
    background: transparent;
    padding: 6px 4px;
    font-size: 0.9rem;
  }

  &__clear,
  &__browse {
    border: none;
    background: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 4px;
    border-radius: 4px;
    color: var(--text-secondary, #888);
    transition: color 0.15s;

    &:hover {
      color: var(--primary-color, #3f51b5);
    }

    mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
  }

  &__clear:hover {
    color: var(--danger-color, #e53935);
  }
}
;

fs.writeFileSync(base + '/account-tree-picker.component.html', html, 'utf8');
fs.writeFileSync(base + '/account-tree-picker.component.scss', scss, 'utf8');
console.log('done');
