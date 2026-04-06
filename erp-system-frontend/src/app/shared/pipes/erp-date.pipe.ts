import { Pipe, PipeTransform, inject } from '@angular/core';
import { DateFormatService } from '../../core/services/date-format.service';

/** Displays dates as dd/MM/yyyy (API values may remain ISO yyyy-MM-dd). */
@Pipe({
  standalone: false,
  name: 'erpDate',
  pure: true
})
export class ErpDatePipe implements PipeTransform {
  private readonly dates = inject(DateFormatService);

  transform(value: unknown): string {
    return this.dates.format(value);
  }
}
