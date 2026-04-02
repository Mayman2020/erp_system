import {Component, Input, OnInit} from '@angular/core';
import { TranslationService } from '../../../../core/i18n/translation.service';

@Component({ standalone: false,
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss']
})
export class AlertComponent implements OnInit {
  @Input() type: string;
  @Input() dismiss: string;

  public dismissAlert(element) {
    element.parentElement.removeChild(element);
  }

  constructor(private translationService: TranslationService) { }

  ngOnInit() {
  }

  get closeLabel(): string {
    return this.translationService.instant('COMMON.CLOSE');
  }

}
