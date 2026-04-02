import {
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import ApexCharts from 'apexcharts/dist/apexcharts.common.js';
import { ApexChartService } from './apex-chart.service';
import { Subscription } from 'rxjs';

@Component({ standalone: false,
  selector: 'app-apex-chart',
  templateUrl: './apex-chart.component.html',
  styleUrls: ['./apex-chart.component.scss']
})
export class ApexChartComponent implements OnInit, OnChanges, OnDestroy {
  /** Kept for backward compatibility with templates; chart mounts on #chartHost instead of global IDs */
  @Input() chartID = '';
  @Input() chartConfig: any;
  @Input() xAxis: any;
  @Input() newData: any;

  @ViewChild('chartHost', { static: true }) chartHost!: ElementRef<HTMLElement>;

  public chart: any;
  private initialized = false;
  private subs: Subscription[] = [];

  constructor(private apexEvent: ApexChartService) {}

  ngOnInit(): void {
    this.subs.push(
      this.apexEvent.changeTimeRange.subscribe(() => {
        if (this.xAxis && this.chart) {
          this.chart.updateOptions({ xaxis: this.xAxis });
        }
      }),
      this.apexEvent.changeSeriesData.subscribe(() => {
        if (this.newData && this.chart) {
          this.chart.updateSeries([{ data: this.newData }]);
        }
      })
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chartConfig']) {
      this.scheduleRender();
    }
  }

  ngOnDestroy(): void {
    this.subs.forEach((s) => s.unsubscribe());
    this.destroyChart();
  }

  private scheduleRender(): void {
    this.destroyChart();
    this.initialized = false;

    const el = this.chartHost?.nativeElement;
    if (!el || !this.isChartReady(this.chartConfig)) {
      return;
    }

    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        const host = this.chartHost?.nativeElement;
        if (!host || !this.isChartReady(this.chartConfig)) {
          return;
        }
        try {
          this.chart = new ApexCharts(host, this.chartConfig);
          const rendered = this.chart.render();
          if (rendered && typeof rendered.then === 'function') {
            rendered.then(() => this.afterChartRender());
          } else {
            this.afterChartRender();
          }
        } catch (e) {
          console.error('ApexCharts render failed', e);
        }
      });
    });
  }

  private afterChartRender(): void {
    this.initialized = true;
    try {
      this.chart?.resize?.();
    } catch (_) {
      /* ignore */
    }
  }

  private isChartReady(cfg: unknown): boolean {
    if (!cfg || typeof cfg !== 'object') {
      return false;
    }
    const c = cfg as Record<string, unknown>;
    return !!(c['chart'] && c['series'] !== undefined && c['series'] !== null);
  }

  private destroyChart(): void {
    if (this.chart) {
      try {
        this.chart.destroy();
      } catch (_) {
        /* ignore */
      }
      this.chart = null;
    }
    this.initialized = false;
  }
}
