import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslationService } from '../../../../../core/i18n/translation.service';

@Component({
  standalone: false,
  selector: 'app-sidebar-calendar',
  templateUrl: './sidebar-calendar.component.html',
  styleUrls: ['./sidebar-calendar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarCalendarComponent implements OnInit, OnDestroy {
  monthTitle = '';
  weekHeaders: string[] = [];
  cells: Array<{ day: number; muted?: boolean; today?: boolean }> = [];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private i18n: TranslationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.build();
    this.i18n.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.build();
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private build(): void {
    const now = new Date();
    const locale = this.i18n.currentLanguage === 'en' ? 'en-US' : 'ar-EG';
    this.monthTitle = now.toLocaleDateString(locale, { month: 'long', year: 'numeric' });
    const formatter = new Intl.DateTimeFormat(locale, { weekday: 'narrow' });
    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - now.getDay());
    this.weekHeaders = Array.from({ length: 7 }, (_, index) => {
      const day = new Date(weekStart);
      day.setDate(weekStart.getDate() + index);
      return formatter.format(day);
    });

    const year = now.getFullYear();
    const month = now.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const next: Array<{ day: number; muted?: boolean; today?: boolean }> = [];
    for (let i = 0; i < firstDay; i++) {
      next.push({ day: 0, muted: true });
    }
    for (let day = 1; day <= daysInMonth; day++) {
      next.push({ day, today: day === now.getDate() });
    }
    this.cells = next;
  }
}
