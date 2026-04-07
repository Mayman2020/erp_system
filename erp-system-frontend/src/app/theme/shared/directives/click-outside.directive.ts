import {
  AfterViewInit,
  Directive,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  Output
} from '@angular/core';

/**
 * Drop-in replacement for deprecated `ng-click-outside`.
 */
@Directive({
  selector: '[clickOutside]',
  standalone: false
})
export class ClickOutsideDirective implements AfterViewInit, OnDestroy {
  @Output() readonly clickOutside = new EventEmitter<MouseEvent | TouchEvent>();

  /** Comma-separated query selectors whose clicks should not count as "outside". */
  @Input() exclude = '';

  /** When true, defer outside-detection until after the opening click cycle completes. */
  @Input() excludeBeforeClick = false;

  private ready = false;

  constructor(private readonly host: ElementRef<HTMLElement>) {}

  ngAfterViewInit(): void {
    if (this.excludeBeforeClick) {
      setTimeout(() => {
        this.ready = true;
      }, 0);
    } else {
      this.ready = true;
    }
  }

  ngOnDestroy(): void {
    this.clickOutside.complete();
  }

  @HostListener('document:mousedown', ['$event'])
  @HostListener('document:touchstart', ['$event'])
  onDocumentDown(event: MouseEvent | TouchEvent): void {
    if (!this.ready) {
      return;
    }

    const target = (event.target as Node) || null;
    if (!target || !this.host.nativeElement) {
      return;
    }

    if (this.host.nativeElement.contains(target)) {
      return;
    }

    if (this.exclude) {
      const parts = this.exclude
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
      for (const sel of parts) {
        try {
          const el = document.querySelector(sel);
          if (el?.contains(target)) {
            return;
          }
        } catch {
          /* invalid selector */
        }
      }
    }

    this.clickOutside.emit(event);
  }
}
