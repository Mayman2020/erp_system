import {
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { Router } from '@angular/router';
import { animate, style, transition, trigger } from '@angular/animations';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  COMMAND_PALETTE_ITEMS,
  CommandPaletteItem,
  CommandPaletteService
} from '../../../core/services/command-palette.service';

@Component({
  standalone: false,
  selector: 'app-command-palette',
  templateUrl: './command-palette.component.html',
  styleUrls: ['./command-palette.component.scss'],
  animations: [
    trigger('erpCommandPalettePanel', [
      transition(':enter', [
        style({ opacity: 0, transform: 'scale(0.96) translateY(-8px)' }),
        animate(
          '220ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ opacity: 1, transform: 'none' })
        )
      ]),
      transition(':leave', [
        animate(
          '160ms ease',
          style({ opacity: 0, transform: 'scale(0.98) translateY(-4px)' })
        )
      ])
    ])
  ]
})
export class CommandPaletteComponent implements OnInit, OnDestroy {
  @ViewChild('q') searchInput?: ElementRef<HTMLInputElement>;

  open = false;
  query = '';
  filtered: CommandPaletteItem[] = [...COMMAND_PALETTE_ITEMS];
  activeItem: CommandPaletteItem | null = this.filtered[0] || null;
  groups: Record<string, CommandPaletteItem[]> = {};
  groupedKeys: string[] = [];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly palette: CommandPaletteService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.palette.open$.pipe(takeUntil(this.destroy$)).subscribe((v) => {
      this.open = v;
      if (v) {
        this.query = '';
        this.applyFilter('');
        setTimeout(() => this.searchInput?.nativeElement?.focus(), 0);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:keydown', ['$event'])
  onDocumentKeydown(e: KeyboardEvent): void {
    if (!this.open) {
      return;
    }
    if (e.key === 'Escape') {
      e.preventDefault();
      this.close();
    }
  }

  close(): void {
    this.palette.close();
  }

  onQuery(v: string): void {
    this.query = v;
    this.applyFilter(v);
  }

  private applyFilter(q: string): void {
    const n = (q || '').trim().toLowerCase();
    if (!n) {
      this.filtered = [...COMMAND_PALETTE_ITEMS];
    } else {
      this.filtered = COMMAND_PALETTE_ITEMS.filter((item) => {
        const path = item.path.toLowerCase();
        const id = item.id.toLowerCase();
        return path.includes(n) || id.includes(n);
      });
    }
    this.rebuildGroups();
    this.activeItem = this.filtered[0] || null;
  }

  private rebuildGroups(): void {
    const g: Record<string, CommandPaletteItem[]> = {};
    for (const item of this.filtered) {
      if (!g[item.groupKey]) {
        g[item.groupKey] = [];
      }
      g[item.groupKey].push(item);
    }
    this.groups = g;
    this.groupedKeys = Object.keys(g);
  }

  onPanelKeydown(e: KeyboardEvent): void {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      this.moveActive(1);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      this.moveActive(-1);
    } else if (e.key === 'Enter' && this.activeItem) {
      e.preventDefault();
      this.go(this.activeItem);
    }
  }

  private moveActive(delta: number): void {
    if (!this.filtered.length) {
      return;
    }
    const i = this.activeItem ? this.filtered.indexOf(this.activeItem) : -1;
    const next = Math.min(this.filtered.length - 1, Math.max(0, i + delta));
    this.activeItem = this.filtered[next];
  }

  go(item: CommandPaletteItem): void {
    this.close();
    void this.router.navigateByUrl(item.path);
  }

  trackById(_: number, item: CommandPaletteItem): string {
    return item.id;
  }
}
