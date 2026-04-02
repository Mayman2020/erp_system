import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

@Component({ standalone: false,
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
  @Output() selected = new EventEmitter<any>();
  expanded = new Set<number>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['nodes']) {
      this.expanded = new Set<number>();
      this.expandToDepth(this.nodes, 0);
    }
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
      INCOME: 'trending_up',
      EXPENSE: 'trending_down'
    };
    return map[type] || 'circle';
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

  normalizeLevel(level: number): number {
    return Math.max(0, Math.min(10, Number(level || 0)));
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
