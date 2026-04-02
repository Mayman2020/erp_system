import {Component, Input, OnInit} from '@angular/core';
import {ToastService} from './toast.service';
import { TranslationService } from '../../../../core/i18n/translation.service';

@Component({ standalone: false,
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss']
})
export class ToastComponent implements OnInit {
  @Input() uID: string;
  @Input() toastTitle: string;
  @Input() toastCaption: string;
  @Input() toastClass: string;

  public isShow: boolean;

  constructor(public toastEvent: ToastService, private translationService: TranslationService) {
    this.isShow = false;
  }

  ngOnInit() {
    this.toastEvent.toggleToast.subscribe((toast) => {
      document.querySelector('#' + toast.uid).classList.add('show');
      setTimeout(() => {
        document.querySelector('#' + toast.uid).classList.remove('show');
      }, toast.delay ? toast.delay : 500);
    });
  }

  closeToast(uid) {
    document.querySelector('#' + uid).classList.remove('show');
  }

  get closeLabel(): string {
    return this.translationService.instant('COMMON.CLOSE');
  }

}
