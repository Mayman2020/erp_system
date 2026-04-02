import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { NgbDropdownModule, NgbTabsetModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { AdvancedSearchBarComponent } from './components/advanced-search-bar/advanced-search-bar.component';
import { DataTableComponent } from './components/data-table/data-table.component';
import { EmptyStateComponent } from './components/empty-state/empty-state.component';
import { FormFieldComponent } from './components/form-field/form-field.component';
import { FormDialogComponent } from './components/form-dialog/form-dialog.component';
import { LoadingStateComponent } from './components/loading-state/loading-state.component';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { StatusBadgeComponent } from './components/status-badge/status-badge.component';
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
    TreeSelectorComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingStateComponent
  ],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, NgbDropdownModule, NgbTooltipModule, NgbTabsetModule, MatIconModule],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    NgbDropdownModule,
    NgbTooltipModule,
    NgbTabsetModule,
    MatIconModule,
    TranslatePipe,
    LegacyTranslatePipe,
    FormFieldComponent,
    AdvancedSearchBarComponent,
    DataTableComponent,
    StatusBadgeComponent,
    TypeBadgeComponent,
    FormDialogComponent,
    TreeSelectorComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingStateComponent
  ]
})
export class SharedModule {}
