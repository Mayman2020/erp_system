import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import { Navigation, NavigationItem, NavigationService } from '../navigation';
import { NextConfig } from '../../../../../app-config';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { NavigationHistoryService } from '../../../../../core/services/navigation-history.service';

@Component({
  standalone: false,
  selector: 'app-nav-content',
  templateUrl: './nav-content.component.html',
  styleUrls: ['./nav-content.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavContentComponent implements OnInit, OnChanges, OnDestroy {
  public flatConfig: any;
  public navigation: Navigation[] = [];
  public windowWidth: number;
  public menuLoading = false;

  private readonly destroy$ = new Subject<void>();
  private readonly sectionExpanded: Record<string, boolean> = {};
  private readonly collapseExpanded: Record<string, boolean> = {};
  private currentUrl = '';

  @Output() onNavMobCollapse = new EventEmitter<void>();

  @Input() menuIconsOnly = false;

  constructor(
    private navigationService: NavigationService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private navHistory: NavigationHistoryService
  ) {
    this.flatConfig = NextConfig.config;
    this.windowWidth = window.innerWidth;
    this.currentUrl = this.normalizePath(this.router.url);
  }

  ngOnInit(): void {
    this.loadNavigation();
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((e) => {
        this.currentUrl = this.normalizePath(e.urlAfterRedirects || e.url);
        this.cdr.markForCheck();
      });
  }

  ngOnChanges(_changes: SimpleChanges): void {
    this.cdr.markForCheck();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  trackById(_index: number, item: NavigationItem): string {
    return item.id;
  }

  isCollapsibleGroup(item: NavigationItem): boolean {
    return item.collapsible !== false && !!item.children?.length;
  }

  isSectionOpen(item: NavigationItem): boolean {
    if (this.menuIconsOnly) {
      return true;
    }
    if (this.sectionExpanded[item.id] !== undefined) {
      return this.sectionExpanded[item.id];
    }
    return this.hasActiveDescendant(item);
  }

  toggleSection(item: NavigationItem, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    const next = !this.isSectionOpen(item);
    this.sectionExpanded[item.id] = next;
    this.persistToggle(`erp_nav_group_v2_${item.id}`, next);
    this.cdr.markForCheck();
  }

  isCollapseOpen(item: NavigationItem): boolean {
    if (this.menuIconsOnly) {
      return true;
    }
    if (this.collapseExpanded[item.id] !== undefined) {
      return this.collapseExpanded[item.id];
    }
    return this.hasActiveDescendant(item);
  }

  toggleCollapse(item: NavigationItem, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    const next = !this.isCollapseOpen(item);
    this.collapseExpanded[item.id] = next;
    this.persistToggle(`erp_nav_collapse_v2_${item.id}`, next);
    this.cdr.markForCheck();
  }

  isCollapseActive(item: NavigationItem): boolean {
    return this.hasActiveDescendant(item);
  }

  linkActiveOptions(item: NavigationItem): { exact: boolean } {
    return { exact: item.exactMatch === true };
  }

  isItemActive(item: NavigationItem): boolean {
    if (!item.url) {
      return false;
    }
    const path = this.currentUrl;
    if (item.exactMatch) {
      return path === item.url;
    }
    return path === item.url || path.startsWith(item.url + '/');
  }

  onNavItemClick(): void {
    this.navHistory.markFromMenu();
    if (this.windowWidth < 992) {
      this.onNavMobCollapse.emit();
    }
  }

  private loadNavigation(): void {
    this.menuLoading = true;
    this.navigationService
      .get()
      .pipe(takeUntil(this.destroy$))
      .subscribe((items) => {
        this.navigation = items || [];
        this.menuLoading = false;
        this.hydrateExpandedState(this.navigation);
        this.cdr.markForCheck();
      });
  }

  private hydrateExpandedState(items: NavigationItem[]): void {
    for (const item of items) {
      if (item.type === 'group' && this.isCollapsibleGroup(item) && !this.hasActiveDescendant(item)) {
        const saved = localStorage.getItem(`erp_nav_group_v2_${item.id}`);
        if (saved !== null) {
          this.sectionExpanded[item.id] = saved === '1';
        } else {
          this.sectionExpanded[item.id] = false;
        }
      }
      if (item.type === 'collapse' && item.children?.length && !this.hasActiveDescendant(item)) {
        const saved = localStorage.getItem(`erp_nav_collapse_v2_${item.id}`);
        if (saved !== null) {
          this.collapseExpanded[item.id] = saved === '1';
        } else {
          this.collapseExpanded[item.id] = false;
        }
      }
      if (item.children?.length) {
        this.hydrateExpandedState(item.children);
      }
    }
  }

  private hasActiveDescendant(item: NavigationItem): boolean {
    return this.matchChildren(item.children, this.currentUrl);
  }

  private matchChildren(children: NavigationItem[] | undefined, path: string): boolean {
    if (!children) {
      return false;
    }
    for (const child of children) {
      if (child.type === 'item' && child.url) {
        if (this.isPathMatch(child.url, path, child.exactMatch === true)) {
          return true;
        }
      }
      if ((child.type === 'group' || child.type === 'collapse') && child.children?.length) {
        if (this.matchChildren(child.children, path)) {
          return true;
        }
      }
    }
    return false;
  }

  private isPathMatch(url: string, path: string, exact: boolean): boolean {
    if (exact) {
      return path === url;
    }
    return path === url || path.startsWith(url + '/');
  }

  private normalizePath(url: string): string {
    return (url || '').split('?')[0] || '';
  }

  private persistToggle(key: string, expanded: boolean): void {
    try {
      localStorage.setItem(key, expanded ? '1' : '0');
    } catch {
      // Ignore storage quota / private mode.
    }
  }
}
