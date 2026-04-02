import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-tree-selector',
  templateUrl: './tree-selector.component.html',
  styleUrls: ['./tree-selector.component.scss']
})
export class TreeSelectorComponent {
  @Input() nodes: any[] = [];
  @Input() selectedId: number | null = null;
  @Input() selectableLeafOnly = false;
  @Input() minSelectableLevel = 1;
  @Output() selected = new EventEmitter<any>();
  expanded = new Set<number>();

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
}
