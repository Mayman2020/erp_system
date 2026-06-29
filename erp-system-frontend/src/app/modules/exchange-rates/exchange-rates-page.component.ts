import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { ExchangeRateDto, ExchangeRateForm } from '../../core/models/accounting.models';
import { AuthService } from '../../core/auth/auth.service';
import { AccountingApiService } from '../../core/services/accounting-api.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table.component';
import { ErpMasterPageBase, MasterPageConfig } from '../../shared/utils/erp-master-page.base';

@Component({
  standalone: false,
  selector: 'app-exchange-rates-page',
  templateUrl: './exchange-rates-page.component.html',
  styleUrls: ['./exchange-rates-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExchangeRatesPageComponent extends ErpMasterPageBase<ExchangeRateDto, ExchangeRateForm> implements OnInit, OnDestroy {
  readonly config: MasterPageConfig = {
    titleKey: 'EXCHANGE_RATES.TITLE',
    createKey: 'EXCHANGE_RATES.CREATE',
    editKey: 'COMMON.EDIT',
    viewKey: 'COMMON.VIEW',
    showStatus: false,
    showSearch: false
  };

  readonly columns: DataTableColumn[] = [
    { key: 'sourceCurrency', title: 'EXCHANGE_RATES.SOURCE' },
    { key: 'targetCurrency', title: 'EXCHANGE_RATES.TARGET' },
    { key: 'rate', title: 'EXCHANGE_RATES.RATE', align: 'end' },
    { key: 'effectiveDate', title: 'EXCHANGE_RATES.EFFECTIVE', kind: 'date' },
    { key: 'expiryDate', title: 'EXCHANGE_RATES.EXPIRY', kind: 'date' }
  ];

  readonly form = this.fb.group({
    sourceCurrency: ['USD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    targetCurrency: ['SAR', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    rate: [1, [Validators.required, Validators.min(0.000001)]],
    effectiveDate: [new Date().toISOString().slice(0, 10), Validators.required],
    expiryDate: ['']
  });

  constructor(
    private api: AccountingApiService,
    private fb: FormBuilder,
    authService: AuthService,
    confirmDialog: ConfirmDialogService,
    cdr: ChangeDetectorRef
  ) {
    super(authService, confirmDialog, cdr);
  }

  get tableActions() {
    return [{ id: 'view', labelKey: 'COMMON.VIEW', className: 'erp-action-secondary' }];
  }

  ngOnInit(): void {
    this.initMasterPage();
  }

  ngOnDestroy(): void { this.destroyMasterPage(); }

  override onTableAction(event: { actionId: string; row: Record<string, unknown> }): void {
    const id = Number(event.row['id']);
    if (event.actionId === 'view' && id) {
      this.openDocument(id, 'view');
    }
  }

  protected fetchList(_filters: Record<string, string>): Observable<ExchangeRateDto[]> {
    return this.api.getExchangeRates();
  }

  protected fetchOne(id: number): Observable<ExchangeRateDto> {
    return this.api.getExchangeRates().pipe(
      map((rows) => {
        const found = (rows || []).find((r) => r.id === id);
        if (!found) {
          throw new Error('Exchange rate not found');
        }
        return found;
      })
    );
  }

  protected createItem(payload: ExchangeRateForm): Observable<ExchangeRateDto> {
    return this.api.createExchangeRate(payload);
  }

  protected updateItem(_id: number, _payload: ExchangeRateForm): Observable<ExchangeRateDto> {
    return of({} as ExchangeRateDto);
  }

  protected removeItem(_id: number): Observable<void> {
    return of(undefined);
  }

  protected defaultFormValues(): Record<string, unknown> {
    return {
      sourceCurrency: 'USD',
      targetCurrency: 'SAR',
      rate: 1,
      effectiveDate: new Date().toISOString().slice(0, 10),
      expiryDate: ''
    };
  }

  protected patchForm(dto: ExchangeRateDto): void {
    this.form.reset({
      sourceCurrency: dto.sourceCurrency || '',
      targetCurrency: dto.targetCurrency || '',
      rate: Number(dto.rate || 0),
      effectiveDate: dto.effectiveDate || '',
      expiryDate: dto.expiryDate || ''
    });
  }

  protected toPayload(): ExchangeRateForm {
    const v = this.form.getRawValue();
    return {
      sourceCurrency: (v.sourceCurrency || '').toUpperCase(),
      targetCurrency: (v.targetCurrency || '').toUpperCase(),
      rate: Number(v.rate),
      effectiveDate: v.effectiveDate!,
      expiryDate: v.expiryDate || undefined
    };
  }

  protected mapRow(dto: ExchangeRateDto): Record<string, unknown> {
    return {
      ...dto,
      rate: Number(dto.rate || 0).toLocaleString(undefined, { minimumFractionDigits: 4, maximumFractionDigits: 6 }),
      expiryDate: dto.expiryDate || '—'
    };
  }
}
