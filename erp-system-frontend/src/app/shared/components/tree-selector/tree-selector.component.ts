import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AccountDto } from '../../../core/models/accounting.models';
import { AccountingApiService } from '../../../core/services/accounting-api.service';
import { TranslationService } from '../../../core/i18n/translation.service';
import { finalize } from 'rxjs/operators';

@Component({
  standalone: false,
  selector: 'app-tree-selector',
  templateUrl: './tree-selector.component.html',
  styleUrls: ['./tree-selector.component.scss']
})
export class TreeSelectorComponent implements OnChanges {
  @Input() initiallyExpandedDepth = 1;
  @Input() nodes: any[] = [];
  @Input() selectedId: number | null = null;
  @Input() selectableLeafOnly = false;
  @Input() minSelectableLevel = 1;
  /** Show search box to filter by code, names, description */
  @Input() showFilter = true;
  @Input() filterPlaceholderKey = 'COMMON.TREE_FILTER_ACCOUNTS';
  @Output() selected = new EventEmitter<any>();
  expanded = new Set<number>();

  detailVisible = false;
  detailLoading = false;
  detailErrorKey = '';
  detailAccount: AccountDto | null = null;

  filterText = '';
  displayNodes: any[] = [];

  constructor(
    private translationService: TranslationService,
    private accountingApi: AccountingApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['nodes']) {
      this.expanded = new Set<number>();
      this.expandToDepth(this.nodes, 0);
    }
    this.applyFilter();
  }

  onFilterInput(value: string): void {
    this.filterText = value;
    this.applyFilter();
  }

  toggle(nodeId: number): void {
    if (this.expanded.has(nodeId)) {
      this.expanded.delete(nodeId);
      return;
    }
    this.expanded.add(nodeId);
  }

  isExpanded(nodeId: number): boolean {
    return this.expanded.has(nodeId);
  }

  icon(type: string): string {
    const map: { [key: string]: string } = {
      ASSET: 'account_balance_wallet',
      LIABILITY: 'security',
      EQUITY: 'stacked_bar_chart',
      REVENUE: 'trending_up',
      EXPENSE: 'trending_down'
    };
    return map[type] || 'circle';
  }

  label(node: any): string {
    if (this.translationService.currentLanguage === 'ar') {
      return node?.nameAr || node?.name || node?.nameEn || node?.accountName || '';
    }
    return node?.nameEn || node?.name || node?.nameAr || node?.accountName || '';
  }

  isSelectable(node: any): boolean {
    if (this.selectableLeafOnly && node?.children && node.children.length) {
      return false;
    }
    const level = Number(node?.level || 0);
    return level >= this.minSelectableLevel;
  }

  selectNode(node: any): void {
    if (!this.isSelectable(node)) {
      return;
    }
    this.selected.emit(node);
  }

  onAccountNameClick(event: Event, node: any): void {
    event.stopPropagation();
    event.preventDefault();
    const id = Number(node?.id);
    if (!id) {
      return;
    }
    this.detailVisible = true;
    this.detailLoading = true;
    this.detailErrorKey = '';
    this.detailAccount = null;
    this.cdr.markForCheck();
    this.accountingApi
      .getAccount(id)
      .pipe(
        finalize(() => {
          this.detailLoading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (account) => {
          this.detailAccount = account;
          this.cdr.markForCheck();
        },
        error: () => {
          this.detailErrorKey = 'COMMON.ERROR_LOADING';
          this.cdr.markForCheck();
        }
      });
  }

  closeAccountDetail(): void {
    this.detailVisible = false;
    this.detailAccount = null;
    this.detailErrorKey = '';
  }

  normalizeLevel(level: number): number {
    return Math.max(0, Math.min(10, Number(level || 0)));
  }

  private applyFilter(): void {
    const q = (this.filterText || '').trim().toLowerCase();
    if (!q) {
      this.displayNodes = this.nodes || [];
      return;
    }
    this.displayNodes = this.filterTree(this.nodes || [], q);
    this.expandAllFiltered(this.displayNodes);
  }

  private expandAllFiltered(nodes: any[]): void {
    for (const node of nodes || []) {
      if (node?.children?.length) {
        this.expanded.add(node.id);
        this.expandAllFiltered(node.children);
      }
    }
  }

  private filterTree(nodes: any[], q: string): any[] {
    const out: any[] = [];
    for (const node of nodes || []) {
      const selfMatch = this.nodeMatches(node, q);
      const childFiltered = node.children?.length ? this.filterTree(node.children, q) : [];
      if (selfMatch) {
        out.push({ ...node, children: node.children ? [...node.children] : [] });
      } else if (childFiltered.length) {
        out.push({ ...node, children: childFiltered });
      }
    }
    return out;
  }

  private nodeMatches(node: any, q: string): boolean {
    const blob = [
      node?.code,
      this.label(node),
      node?.name,
      node?.nameAr,
      node?.nameEn,
      node?.accountName,
      node?.description,
      node?.descriptionAr,
      node?.descriptionEn
    ]
      .filter((v) => v != null && `${v}`.trim() !== '')
      .join(' ')
      .toLowerCase();
    return blob.includes(q);
  }

  private expandToDepth(nodes: any[], depth: number): void {
    if (depth >= this.initiallyExpandedDepth) {
      return;
    }

    for (const node of nodes || []) {
      if (node?.children?.length) {
        this.expanded.add(node.id);
        this.expandToDepth(node.children, depth + 1);
      }
    }
  }
}
