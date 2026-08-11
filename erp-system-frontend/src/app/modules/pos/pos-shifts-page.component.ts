import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { PosShiftCloseForm, PosShiftDto } from '../../core/models/erp.models';
import { PosApiService } from '../../core/services/pos-api.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';

@Component({
  standalone: false,
  selector: 'app-pos-shifts-page',
  templateUrl: './pos-shifts-page.component.html',
  styleUrls: ['./pos-shifts-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PosShiftsPageComponent implements OnInit {
  rows: Array<Record<string, unknown>> = [];
  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  closeVisible = false;
  selectedShift: PosShiftDto | null = null;

  readonly closeForm = this.fb.group({
    closingCash: [0, [Validators.required, Validators.min(0)]],
    notes: ['']
  });

  readonly columns: DataTableColumn[] = [
    { key: 'shiftNo', title: 'ERP.CODE' },
    { key: 'terminalCode', title: 'POS.TERMINAL' },
    { key: 'cashierUsername', title: 'ERP.EMAIL' },
    { key: 'status', title: 'COMMON.STATUS', kind: 'status' },
    { key: 'openingCash', title: 'POS.OPENING_CASH', kind: 'text' },
    { key: 'cashSales', title: 'POS.PAY_CASH', kind: 'text' },
    { key: 'discrepancy', title: 'POS.DISCREPANCY', kind: 'text' },
    { key: 'openedAt', title: 'COMMON.DATE', kind: 'date' }
  ];

  readonly actions = [
    {
      id: 'close',
      labelKey: 'POS.CLOSE_SHIFT',
      className: 'erp-action-info',
      disabledWhen: (row: Record<string, unknown>) => row['status'] !== 'OPEN'
    }
  ];

  constructor(
    private fb: FormBuilder,
    private posApi: PosApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadShifts();
  }

  loadShifts(): void {
    this.loading = true;
    this.posApi.getShifts().pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (shifts) => {
        this.rows = shifts.map((shift) => ({ ...shift }));
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR';
      }
    });
  }

  onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    if (event.actionId !== 'close') {
      return;
    }
    this.selectedShift = event.row as unknown as PosShiftDto;
    const expected = Number(this.selectedShift.openingCash || 0) + Number(this.selectedShift.cashSales || 0);
    this.closeForm.reset({ closingCash: expected, notes: '' });
    this.closeVisible = true;
    this.cdr.markForCheck();
  }

  submitClose(): void {
    if (!this.selectedShift || this.closeForm.invalid) {
      this.closeForm.markAllAsTouched();
      return;
    }
    const value = this.closeForm.getRawValue();
    const payload: PosShiftCloseForm = {
      closingCash: Number(value.closingCash || 0),
      notes: value.notes || undefined
    };
    this.saving = true;
    this.posApi.closeShift(this.selectedShift.id, payload).pipe(finalize(() => {
      this.saving = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: () => {
        this.closeVisible = false;
        this.successKey = 'POS.CLOSE_SHIFT';
        this.loadShifts();
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR';
      }
    });
  }
}
