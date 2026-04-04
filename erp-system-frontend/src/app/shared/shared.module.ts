import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { NgbDropdownModule, NgbNavModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { AccountTreePickerComponent } from './components/account-tree-picker/account-tree-picker.component';
import { AdvancedSearchBarComponent } from './components/advanced-search-bar/advanced-search-bar.component';
import { DataTableComponent } from './components/data-table/data-table.component';
import { EmptyStateComponent } from './components/empty-state/empty-state.component';
import { FormFieldComponent } from './components/form-field/form-field.component';
import { FormDialogComponent } from './components/form-dialog/form-dialog.component';
import { LoadingStateComponent } from './components/loading-state/loading-state.component';
import { LanguageSwitcherComponent } from './components/language-switcher/language-switcher.component';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { ProfileCardComponent } from './components/profile-card/profile-card.component';
import { RecentActivityTableComponent } from './components/recent-activity-table/recent-activity-table.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { StatusBadgeComponent } from './components/status-badge/status-badge.component';
import { TabsComponent } from './components/tabs/tabs.component';
import { TreeSelectorComponent } from './components/tree-selector/tree-selector.component';
import { TypeBadgeComponent } from './components/type-badge/type-badge.component';
import { LegacyTranslatePipe, TranslatePipe } from './pipes/translate.pipe';

@NgModule({
  declarations: [
    TranslatePipe,
    LegacyTranslatePipe,
    FormFieldComponent,
    AdvancedSearchBarComponent,
    DataTableComponent,
    StatusBadgeComponent,
    TypeBadgeComponent,
    FormDialogComponent,
    LanguageSwitcherComponent,
    TreeSelectorComponent,
    AccountTreePickerComponent,
    SidebarComponent,
    ProfileCardComponent,
    TabsComponent,
    RecentActivityTableComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingStateComponent
  ],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, NgbDropdownModule, NgbTooltipModule, NgbNavModule, MatIconModule],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    NgbDropdownModule,
    NgbTooltipModule,
    NgbNavModule,
    MatIconModule,
    TranslatePipe,
    LegacyTranslatePipe,
    FormFieldComponent,
    AdvancedSearchBarComponent,
    DataTableComponent,
    StatusBadgeComponent,
    TypeBadgeComponent,
    FormDialogComponent,
    LanguageSwitcherComponent,
    TreeSelectorComponent,
    AccountTreePickerComponent,
    SidebarComponent,
    ProfileCardComponent,
    TabsComponent,
    RecentActivityTableComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingStateComponent
  ]
})
export class SharedModule {}
