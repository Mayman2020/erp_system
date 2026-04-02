import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { JournalEntryRoutingModule } from './journal-entry-routing.module';
import { JournalEntryPageComponent } from './journal-entry-page.component';

@NgModule({
  declarations: [JournalEntryPageComponent],
  imports: [SharedModule, JournalEntryRoutingModule]
})
export class JournalEntryModule {}

