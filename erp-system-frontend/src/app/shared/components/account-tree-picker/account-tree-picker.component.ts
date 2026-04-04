import { Component, forwardRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  standalone: false,
  selector: 'app-account-tree-picker',
  templateUrl: './account-tree-picker.component.html',
  styleUrls: ['./account-tree-picker.component.scss'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => AccountTreePickerComponent),
    multi: true
  }]
})
export class AccountTreePickerComponent implements ControlValueAccessor, OnChanges {
  @Input() accountTree: any[] = [];
  @Input() labelKey = 'JOURNAL.ACCOUNT';
  @Input() icon = 'account_balance';
  @Input() minSelectableLevel = 1;
  @Input() nullable = false;

  dialogVisible = false;
  selectedId: number | null = null;
  displayLabel = '';
  disabled = false;

  private onChange: (value: number | null) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private translationService: TranslationService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['accountTree'] && this.selectedId) {
      this.updateLabel();
    }
  }

  writeValue(value: number | null): void {
    this.selectedId = value;
    this.updateLabel();
  }

  registerOnChange(fn: (value: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  openDialog(): void {
    if (this.disabled) {
      return;
    }
    this.dialogVisible = true;
    this.onTouched();
  }

  closeDialog(): void {
    this.dialogVisible = false;
  }

  onNodeSelected(node: any): void {
    this.selectedId = node.id;
    this.displayLabel = this.buildDisplayLabel(node);
    this.onChange(node.id);
    this.dialogVisible = false;
  }

  clearSelection(event: Event): void {
    event.stopPropagation();
    this.selectedId = null;
    this.displayLabel = '';
    this.onChange(null);
  }

  private updateLabel(): void {
    if (!this.selectedId || !this.accountTree?.length) {
      this.displayLabel = '';
      return;
    }
    const node = this.findNode(this.accountTree, this.selectedId);
    if (node) {
      this.displayLabel = this.buildDisplayLabel(node);
    }
  }

  private findNode(nodes: any[], id: number): any {
    for (const node of nodes) {
      if (node.id === id) {
        return node;
      }
      if (node.children?.length) {
        const found = this.findNode(node.children, id);
        if (found) {
          return found;
        }
      }
    }
    return null;
  }

  private nodeLabel(node: any): string {
    if (this.translationService.currentLanguage === 'ar') {
      return node?.nameAr || node?.name || node?.nameEn || '';
    }
    return node?.nameEn || node?.name || node?.nameAr || '';
  }

  private buildDisplayLabel(node: any): string {
    const name = this.nodeLabel(node);
    const code = node?.code || '';
    const fs = node?.financialStatement;
    if (!fs) {
      return `${code} - ${name}`.trim();
    }
    const fsShort = this.translationService.instant('FINANCIAL_STATEMENT.' + fs);
    return `${code} - ${name} · ${fsShort}`.trim();
  }
}
