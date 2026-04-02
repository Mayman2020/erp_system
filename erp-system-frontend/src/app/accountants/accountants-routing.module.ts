import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountantsHomeComponent } from './accountants-home.component';

const routes: Routes = [{ path: '', component: AccountantsHomeComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountantsRoutingModule {}
