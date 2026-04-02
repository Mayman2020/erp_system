import {Component, Input, NgZone, OnInit} from '@angular/core';
import {NavigationItem} from '../../navigation';
import {Location} from '@angular/common';
import {Router} from '@angular/router';
import {NextConfig} from '../../../../../../app-config';

@Component({ standalone: false,
  selector: 'app-nav-group',
  templateUrl: './nav-group.component.html',
  styleUrls: ['./nav-group.component.scss']
})
export class NavGroupComponent implements OnInit {
  @Input() item: NavigationItem;
  @Input() layout1: boolean = false;
  @Input() activeId: any;
  public flatConfig: any;
  public themeLayout: string;
  expanded = true;

  constructor(private zone: NgZone, private location: Location, private router: Router) {
    this.flatConfig = NextConfig.config;
    this.themeLayout = this.flatConfig.layout;
  }

  get useCollapsibleGroup(): boolean {
    return this.themeLayout === 'vertical' && this.item.collapsible !== false && !!this.item.children?.length;
  }

  private storageKey(): string {
    return `erp_nav_group_${this.item.id}`;
  }

  ngOnInit() {
    if (this.useCollapsibleGroup) {
      const active = this.hasActiveDescendant();
      if (active) {
        this.expanded = true;
      } else {
        const saved = localStorage.getItem(this.storageKey());
        if (saved !== null) {
          this.expanded = saved === '1';
        } else {
          this.expanded = true;
        }
      }
    }

    let current_url = this.location.path();
    if (this.location['_baseHref']) {
      current_url = this.location['_baseHref'] + this.location.path();
    }
    const link = "a.nav-link[ href='" + current_url + "' ]";
    const ele = document.querySelector(link);
    if (ele !== null && ele !== undefined) {
      const parent = ele.parentElement;
      const up_parent = parent.parentElement.parentElement;
      const last_parent = up_parent.parentElement;
      if (parent.classList.contains('pcoded-hasmenu')) {
        if (this.flatConfig['layout'] === 'vertical') {
          parent.classList.add('pcoded-trigger');
        }
        parent.classList.add('active');
      } else if(up_parent.classList.contains('pcoded-hasmenu')) {
        if (this.flatConfig['layout'] === 'vertical') {
          up_parent.classList.add('pcoded-trigger');
        }
        up_parent.classList.add('active');
      } else if (last_parent.classList.contains('pcoded-hasmenu')) {
        if (this.flatConfig['layout'] === 'vertical') {
          last_parent.classList.add('pcoded-trigger');
        }
        last_parent.classList.add('active');
      }
    }
  }

  toggleExpanded(e: Event): void {
    e.preventDefault();
    e.stopPropagation();
    this.expanded = !this.expanded;
    try {
      localStorage.setItem(this.storageKey(), this.expanded ? '1' : '0');
    } catch {
      /* ignore quota / private mode */
    }
  }

  private hasActiveDescendant(): boolean {
    const path = (this.router.url || '').split('?')[0] || '';
    return this.matchChildren(this.item.children, path);
  }

  private matchChildren(children: NavigationItem[] | undefined, path: string): boolean {
    if (!children) {
      return false;
    }
    for (const c of children) {
      if (c.type === 'item' && c.url) {
        if (path === c.url || path.startsWith(c.url + '/')) {
          return true;
        }
      }
      if ((c.type === 'group' || c.type === 'collapse') && c.children?.length) {
        if (this.matchChildren(c.children, path)) {
          return true;
        }
      }
    }
    return false;
  }

}
