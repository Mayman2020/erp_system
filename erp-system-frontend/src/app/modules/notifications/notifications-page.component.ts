import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslationService } from '../../core/i18n/translation.service';
import { NotificationItem, NotificationService } from '../../core/services/notification.service';

type NotifyTab = 'recent' | 'older';

/** Where each notification's referenceType should deep-link to. This app's list pages are
 * dialog-driven rather than routed by id, so this navigates to the owning list page. */
const REFERENCE_ROUTES: Record<string, string> = {
  JOURNAL_ENTRY: '/journal-entries',
  PRODUCT: '/inventory/products',
  LEAVE_REQUEST: '/hr/leave-requests'
};

@Component({
  standalone: false,
  selector: 'app-notifications-page',
  templateUrl: './notifications-page.component.html',
  styleUrls: ['./notifications-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotificationsPageComponent implements OnInit {
  loading = false;
  page = 0;
  size = 50;
  total = 0;
  rows: NotificationItem[] = [];
  activeTab: NotifyTab = 'recent';

  private readonly recentDays = 14;

  constructor(
    private readonly notificationService: NotificationService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    public readonly translationService: TranslationService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  get unreadCount(): number {
    return this.rows.filter((r) => !r.read).length;
  }

  get recentRows(): NotificationItem[] {
    const cutoff = Date.now() - this.recentDays * 24 * 60 * 60 * 1000;
    return this.rows.filter((r) => this.toTime(r.createdAt) >= cutoff);
  }

  get olderRows(): NotificationItem[] {
    const cutoff = Date.now() - this.recentDays * 24 * 60 * 60 * 1000;
    return this.rows.filter((r) => this.toTime(r.createdAt) < cutoff);
  }

  get visibleRows(): NotificationItem[] {
    return this.activeTab === 'recent' ? this.recentRows : this.olderRows;
  }

  setTab(tab: NotifyTab): void {
    this.activeTab = tab;
    this.cdr.markForCheck();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.notificationService.list(this.page, this.size).subscribe({
      next: (page) => {
        this.rows = page?.content ?? [];
        this.total = page?.totalElements ?? 0;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.rows = [];
        this.total = 0;
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  text(key: string, item: NotificationItem): string {
    const translated = this.translationService.instant(key);
    if (!item.varsJson) {
      return translated;
    }
    try {
      const params = JSON.parse(item.varsJson) as Record<string, string>;
      return Object.entries(params).reduce(
        (value, [paramKey, paramValue]) => value.replace(`{{${paramKey}}}`, String(paramValue)),
        translated
      );
    } catch {
      return translated;
    }
  }

  onClick(item: NotificationItem): void {
    this.markRead(item);
    const route = item.referenceType ? REFERENCE_ROUTES[item.referenceType] : null;
    if (route) {
      this.router.navigateByUrl(route);
    }
  }

  markRead(item: NotificationItem): void {
    if (item.read) return;
    this.notificationService.markRead(item.id).subscribe({
      next: () => {
        item.read = true;
        this.cdr.markForCheck();
      }
    });
  }

  markAllRead(): void {
    this.notificationService.markAllRead().subscribe({
      next: () => this.load()
    });
  }

  onPageChange(index: number): void {
    this.page = index;
    this.load();
  }

  private toTime(value?: string | null): number {
    if (!value) return 0;
    const t = Date.parse(value);
    return Number.isFinite(t) ? t : 0;
  }
}
