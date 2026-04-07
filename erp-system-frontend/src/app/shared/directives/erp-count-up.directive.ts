import { Directive, ElementRef, Input, NgZone, OnChanges, OnDestroy, Renderer2, SimpleChanges } from '@angular/core';

/** Animates displayed number from 0 to target (presentation only; ~0.8s ease-out). */
@Directive({
  standalone: false,
  selector: '[erpCountUp]'
})
export class ErpCountUpDirective implements OnChanges, OnDestroy {
  @Input({ required: true }) erpCountUp!: number;
  @Input() erpCountUpDuration = 800;
  @Input() erpCountUpDecimals = 2;

  private rafId = 0;

  constructor(
    private readonly el: ElementRef<HTMLElement>,
    private readonly r2: Renderer2,
    private readonly zone: NgZone
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['erpCountUp']) {
      return;
    }
    const v = Number(this.erpCountUp);
    if (Number.isNaN(v)) {
      return;
    }
    if (typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches) {
      this.setText(v);
      return;
    }
    this.animateTo(v);
  }

  ngOnDestroy(): void {
    if (this.rafId) {
      cancelAnimationFrame(this.rafId);
    }
  }

  private setText(value: number): void {
    const text = value.toLocaleString(undefined, {
      minimumFractionDigits: this.erpCountUpDecimals,
      maximumFractionDigits: this.erpCountUpDecimals
    });
    this.r2.setProperty(this.el.nativeElement, 'textContent', text);
  }

  private animateTo(end: number): void {
    if (this.rafId) {
      cancelAnimationFrame(this.rafId);
    }

    const duration = Math.max(0, this.erpCountUpDuration);
    const decimals = this.erpCountUpDecimals;
    const host = this.el.nativeElement;

    this.zone.runOutsideAngular(() => {
      const start = performance.now();
      const from = 0;

      const frame = (now: number) => {
        const t = duration <= 0 ? 1 : Math.min(1, (now - start) / duration);
        const eased = 1 - Math.pow(1 - t, 3);
        const val = from + (end - from) * eased;
        const text = val.toLocaleString(undefined, {
          minimumFractionDigits: decimals,
          maximumFractionDigits: decimals
        });
        this.r2.setProperty(host, 'textContent', text);
        if (t < 1) {
          this.rafId = requestAnimationFrame(frame);
        } else {
          this.setText(end);
          this.rafId = 0;
        }
      };

      this.rafId = requestAnimationFrame(frame);
    });
  }
}
