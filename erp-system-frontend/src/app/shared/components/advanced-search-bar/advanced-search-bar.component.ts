import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';

@Component({ standalone: false,
  selector: 'app-advanced-search-bar',
  templateUrl: './advanced-search-bar.component.html'
})
export class AdvancedSearchBarComponent implements OnDestroy {
  @Input() showStatus = true;
  @Input() showDateRange = true;
  @Input() showAmountRange = false;
  @Input() statusOptions: string[] = [];
  @Input() statusLabelPrefix = 'STATUS.';
  @Output() search = new EventEmitter<any>();

  readonly form = this.fb.group({
    query: [''],
    status: [''],
    fromDate: [''],
    toDate: [''],
    minAmount: [''],
    maxAmount: ['']
  });

  private readonly searchTrigger = new Subject<void>();
  private readonly destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder) {
    this.form.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => this.searchTrigger.next());
    this.searchTrigger.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(() => this.emitSearch());
  }

  emitSearch(): void {
    this.search.emit(this.form.getRawValue());
  }

  clear(): void {
    this.form.reset({
      query: '',
      status: '',
      fromDate: '',
      toDate: '',
      minAmount: '',
      maxAmount: ''
    });
    this.emitSearch();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
