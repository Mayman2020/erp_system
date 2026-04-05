import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { TranslationService } from '../i18n/translation.service';

export interface ConfirmByKeyOptions {
  /** i18n key; default COMMON.CONFIRM_TITLE */
  titleKey?: string;
  /** i18n key for body */
  messageKey: string;
  /** i18n key for confirm button; default COMMON.CONFIRM_OK */
  confirmKey?: string;
  /** i18n key for cancel button; default COMMON.CANCEL */
  cancelKey?: string;
  /** Use danger styling on confirm (e.g. cancel voucher) */
  danger?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private readonly dialog = inject(MatDialog);
  private readonly i18n = inject(TranslationService);

  /**
   * Opens a styled confirmation dialog. Emits `true` if the user confirms, `false` if dismissed or cancelled.
   */
  confirmByKey(options: ConfirmByKeyOptions): Observable<boolean> {
    const data: ConfirmDialogData = {
      title: this.i18n.instant(options.titleKey ?? 'COMMON.CONFIRM_TITLE'),
      message: this.i18n.instant(options.messageKey),
      confirmText: this.i18n.instant(options.confirmKey ?? 'COMMON.CONFIRM_OK'),
      cancelText: this.i18n.instant(options.cancelKey ?? 'COMMON.CANCEL'),
      danger: options.danger ?? false
    };

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: 'min(420px, 92vw)',
      maxWidth: '95vw',
      disableClose: true,
      autoFocus: 'dialog',
      data,
      panelClass: 'erp-confirm-dialog-panel',
      ariaDescribedBy: 'erp-confirm-message',
      ariaLabelledBy: 'erp-confirm-title'
    });

    return ref.afterClosed().pipe(map((result) => result === true));
  }
}
