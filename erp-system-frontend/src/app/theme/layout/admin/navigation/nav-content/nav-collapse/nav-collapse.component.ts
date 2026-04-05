import {Component, Input, OnInit, OnDestroy} from '@angular/core';
import {NavigationItem} from '../../navigation';
import {NavigationEnd, Router} from '@angular/router';
import {animate, style, transition, trigger} from '@angular/animations';
import {NextConfig} from '../../../../../../app-config';
import {Subscription} from 'rxjs';
import {filter} from 'rxjs/operators';

@Component({ standalone: false,
  selector: 'app-nav-collapse',
  templateUrl: './nav-collapse.component.html',
  styleUrls: ['./nav-collapse.component.scss'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({transform: 'translateY(-100%)', display: 'block'}),
        animate('250ms ease-in', style({transform: 'translateY(0%)'}))
      ]),
      transition(':leave', [
        animate('250ms ease-in', style({transform: 'translateY(-100%)'}))
      ])
    ])
  ],
})
export class NavCollapseComponent implements OnInit, OnDestroy {
  public visible;
  public isActive = false;
  @Input() item: NavigationItem;
  public flatConfig: any;
  public themeLayout: string;
  private routerSub: Subscription;

  constructor(private router: Router) {
    this.visible = false;
    this.flatConfig = NextConfig.config;
    this.themeLayout = this.flatConfig.layout;
  }

  ngOnInit() {
    this.checkActive(this.router.url);
    this.routerSub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => this.checkActive(e.urlAfterRedirects || e.url));
  }

  ngOnDestroy() {
    this.routerSub?.unsubscribe();
  }

  private checkActive(url: string): void {
    this.isActive = this.hasActiveChild(this.item, url);
    if (this.isActive) {
      this.visible = true;
    }
  }

  private hasActiveChild(item: NavigationItem, url: string): boolean {
    if (!item.children) { return false; }
    return item.children.some((child) =>
      (child.url && url.startsWith(child.url)) || this.hasActiveChild(child, url)
    );
  }

  navCollapse(e) {
    this.visible = !this.visible;

    let parent = e.target;
    if (this.themeLayout === 'vertical') {
      parent = parent.parentElement;
    }

    const sections = document.querySelectorAll('.pcoded-hasmenu');
    for (let i = 0; i < sections.length; i++) {
      if (sections[i] !== parent) {
        sections[i].classList.remove('pcoded-trigger');
      }
    }

    let firstParent = parent.parentElement;
    let preParent = parent.parentElement.parentElement;
    if (firstParent.classList.contains('pcoded-hasmenu')) {
      do {
        firstParent.classList.add('pcoded-trigger');
        // firstParent.parentElement.classList.toggle('pcoded-trigger');
        firstParent = firstParent.parentElement.parentElement.parentElement;
      } while (firstParent.classList.contains('pcoded-hasmenu'));
    } else if (preParent.classList.contains('pcoded-submenu')) {
      do {
        preParent.parentElement.classList.add('pcoded-trigger');
        preParent = preParent.parentElement.parentElement.parentElement;
      } while (preParent.classList.contains('pcoded-submenu'));
    }
    parent.classList.toggle('pcoded-trigger');
  }

}
