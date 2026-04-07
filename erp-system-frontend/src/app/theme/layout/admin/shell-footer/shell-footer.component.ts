import { booleanAttribute, Component, HostBinding, Input } from '@angular/core';
import { environment } from '../../../../../environments/environment';

@Component({
  standalone: false,
  selector: 'app-shell-footer',
  templateUrl: './shell-footer.component.html',
  styleUrls: ['./shell-footer.component.scss']
})
export class ShellFooterComponent {
  /** Tight layout for auth/login so the page fits one viewport without body scroll */
  @Input({ transform: booleanAttribute }) compact = false;

  @HostBinding('class.erp-shell-footer--compact')
  get compactHostClass(): boolean {
    return this.compact;
  }
  readonly year = new Date().getFullYear();
  readonly version = environment.appVersion ?? '';
  readonly envLabelKey = environment.production ? 'APP.ENV_PRODUCTION' : 'APP.ENV_LOCAL';
}
