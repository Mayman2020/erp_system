import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../core/auth/auth.service';
import { PosSaleForm, PosShiftDto } from '../../core/models/erp.models';
import { ErpApiService } from '../../core/services/erp-api.service';
import { PosApiService } from '../../core/services/pos-api.service';
import { PosOfflineService } from '../../core/services/pos-offline.service';

interface CartLine {
  productId: number;
  name: string;
  sku: string;
  unitPrice: number;
  quantity: number;
}

@Component({
  standalone: false,
  selector: 'app-pos-sale-page',
  templateUrl: './pos-sale-page.component.html',
  styleUrls: ['./pos-sale-page.component.scss']
})
export class PosSalePageComponent implements OnInit, OnDestroy {
  shift: PosShiftDto | null = null;
  products: any[] = [];
  cart: CartLine[] = [];
  query = '';
  online = true;
  saving = false;
  errorKey = '';
  discount = 0;
  paymentMethod: 'CASH' | 'CARD' | 'CREDIT' = 'CASH';
  private readonly destroy$ = new Subject<void>();

  constructor(
    private posApi: PosApiService,
    private erpApi: ErpApiService,
    private offline: PosOfflineService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.offline.online$.pipe(takeUntil(this.destroy$)).subscribe((value) => (this.online = value));
    const cashierUserId = this.authService.currentUser?.id;
    this.posApi.getOpenShift(undefined, cashierUserId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (shift) => {
        if (!shift?.id) {
          this.router.navigate(['/pos/start']);
          return;
        }
        this.shift = shift;
        this.loadProducts();
      },
      error: () => this.router.navigate(['/pos/start'])
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get filteredProducts(): any[] {
    const q = (this.query || '').trim().toLowerCase();
    if (!q) {
      return this.products.slice(0, 48);
    }
    return this.products.filter((p) =>
      `${p.nameEn || ''} ${p.nameAr || ''} ${p.code || ''}`.toLowerCase().includes(q)
    );
  }

  get subtotal(): number {
    return this.cart.reduce((sum, line) => sum + line.unitPrice * line.quantity, 0);
  }

  get total(): number {
    return Math.max(0, this.subtotal - (this.discount || 0));
  }

  addProduct(product: any): void {
    const existing = this.cart.find((line) => line.productId === product.id);
    if (existing) {
      existing.quantity += 1;
      return;
    }
    this.cart.push({
      productId: product.id,
      name: product.nameAr || product.nameEn || product.code,
      sku: product.code,
      unitPrice: Number(product.salePrice || product.costPrice || 0),
      quantity: 1
    });
  }

  removeLine(index: number): void {
    this.cart.splice(index, 1);
  }

  async confirmSale(): Promise<void> {
    if (!this.shift || !this.cart.length) {
      this.errorKey = 'POS.CART_EMPTY';
      return;
    }
    this.saving = true;
    this.errorKey = '';
    const payload: PosSaleForm = {
      shiftId: this.shift.id,
      warehouseId: this.shift.warehouseId,
      discountAmount: this.discount || 0,
      paidCash: this.paymentMethod === 'CASH' ? this.total : 0,
      paidCard: this.paymentMethod === 'CARD' ? this.total : 0,
      paidCredit: this.paymentMethod === 'CREDIT' ? this.total : 0,
      idempotencyKey: crypto.randomUUID(),
      lines: this.cart.map((line) => ({
        productId: line.productId,
        quantity: line.quantity,
        unitPrice: line.unitPrice,
        discountAmount: 0,
        taxRate: 0
      }))
    };

    if (!this.online) {
      await this.offline.enqueueSale(payload, this.shift.terminalId);
      this.cart = [];
      this.saving = false;
      return;
    }

    this.posApi.createSale(payload).subscribe({
      next: () => {
        this.cart = [];
        this.saving = false;
      },
      error: async () => {
        if (!navigator.onLine) {
          await this.offline.enqueueSale(payload, this.shift!.terminalId);
          this.cart = [];
          this.saving = false;
          return;
        }
        this.saving = false;
        this.errorKey = 'COMMON.ERROR';
      }
    });
  }

  closeShift(): void {
    if (!this.shift) {
      return;
    }
    const expected = Number(this.shift.openingCash || 0) + Number(this.shift.cashSales || 0);
    const closingCash = Number(prompt('Closing cash', String(expected)) || 0);
    this.posApi.closeShift(this.shift.id, { closingCash }).subscribe({
      next: () => this.router.navigate(['/pos/shifts']),
      error: () => (this.errorKey = 'COMMON.ERROR')
    });
  }

  private loadProducts(): void {
    this.erpApi.getProducts({ active: true } as any).pipe(takeUntil(this.destroy$)).subscribe({
      next: (rows) => {
        this.products = rows || [];
      },
      error: () => {
        this.errorKey = 'COMMON.ERROR';
      }
    });
  }
}
