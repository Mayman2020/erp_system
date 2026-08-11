import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { CardComponent } from './card.component';
import { NgbDropdownModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
/*import { AnimationService, AnimatorModule } from 'css-animator';*/

@NgModule({
  imports: [
    CommonModule,
    MatIconModule,
    NgbDropdownModule,
    NgbTooltipModule,
    /*AnimatorModule*/
  ],
  declarations: [CardComponent],
  exports: [CardComponent],
  providers: [/*AnimationService*/]
})
export class CardModule { }
