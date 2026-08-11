import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { DigitalLiteracyPageComponent } from './digital-literacy-page.component';

const routes: Routes = [{ path: '', component: DigitalLiteracyPageComponent }];

@NgModule({
  declarations: [DigitalLiteracyPageComponent],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class DigitalLiteracyModule {}
