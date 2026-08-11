import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../core/auth/auth.service';
import { PosShiftOpenForm, PosTerminalDto } from '../../core/models/erp.models';
import { PosApiService } from '../../core/services/pos-api.service';

@Component({
  standalone: false,
  selector: 'app-pos-start-page',
  templateUrl: './pos-start-page.component.html',
  styleUrls: ['./pos-start-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PosStartPageComponent implements OnInit {
  terminals: PosTerminalDto[] = [];
  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';

  readonly form = this.fb.group({
    terminalId: [null as number | null, Validators.required],
    warehouseId: [null as number | null, Validators.required],
    openingCash: [0, [Validators.required, Validators.min(0)]],
    notes: ['']
  });

  constructor(
    private fb: FormBuilder,
    private posApi: PosApiService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  get terminalLovItems(): Array<{ id: number; label: string }> {
    return (this.terminals || []).map((t) => ({ id: t.id, label: `${t.code} — ${t.name}` }));
  }

  ngOnInit(): void {
    this.loading = true;
    this.posApi.getTerminals().pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: (terminals) => {
        this.terminals = terminals;
        if (terminals.length === 1) {
          this.form.patchValue({
            terminalId: terminals[0].id,
            warehouseId: terminals[0].warehouseId
          });
        }
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR';
      }
    });
  }

  onTerminalChange(): void {
    const terminalId = this.form.controls.terminalId.value;
    const terminal = this.terminals.find((item) => item.id === terminalId);
    if (terminal) {
      this.form.patchValue({ warehouseId: terminal.warehouseId });
    }
  }

  openShift(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const user = this.authService.currentUser;
    if (!user?.id) {
      this.errorKey = 'COMMON.ERROR';
      return;
    }
    const value = this.form.getRawValue();
    const payload: PosShiftOpenForm = {
      terminalId: Number(value.terminalId),
      warehouseId: Number(value.warehouseId),
      openingCash: Number(value.openingCash || 0),
      cashierUserId: user.id,
      notes: value.notes || undefined
    };
    this.saving = true;
    this.errorKey = '';
    this.posApi.openShift(payload).pipe(finalize(() => {
      this.saving = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: () => {
        this.successKey = 'POS.OPEN_SHIFT';
        this.router.navigate(['/pos/sale']);
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR';
      }
    });
  }
}
