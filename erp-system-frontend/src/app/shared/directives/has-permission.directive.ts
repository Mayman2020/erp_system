import { Directive, Input, OnDestroy, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PermissionService } from '../../core/services/permission.service';
import { UiPermission } from '../../core/models/admin.models';

@Directive({
  standalone: false,
  selector: '[appHasPermission]'
})
export class HasPermissionDirective implements OnInit, OnDestroy {
  @Input('appHasPermission') menuItemId = '';
  @Input() appHasPermissionAction: keyof UiPermission = 'canCreate';

  private readonly destroy$ = new Subject<void>();
  private hasView = false;

  constructor(
    private templateRef: TemplateRef<unknown>,
    private viewContainer: ViewContainerRef,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    if (!this.menuItemId?.trim()) {
      this.render(false);
      return;
    }
    this.permissionService.can(this.menuItemId, this.appHasPermissionAction)
      .pipe(takeUntil(this.destroy$))
      .subscribe((allowed) => this.render(allowed));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private render(show: boolean): void {
    if (show && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!show && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}
