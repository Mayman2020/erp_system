import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ThemeService } from '../../../../../core/services/theme.service';

@Component({ standalone: false,
  selector: 'app-nav-right',
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss']
})
export class NavRightComponent implements OnInit, OnDestroy {
  darkMode = false;

  private readonly destroy$ = new Subject<void>();

  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    this.darkMode = this.themeService.mode === 'dark';
    this.themeService.mode$.pipe(takeUntil(this.destroy$)).subscribe((m) => {
      this.darkMode = m === 'dark';
    });
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
