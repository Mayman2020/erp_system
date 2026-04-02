import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { JournalEntryPageComponent } from './journal-entry-page.component';

const routes: Routes = [{ path: '', component: JournalEntryPageComponent }];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class JournalEntryRoutingModule {}

