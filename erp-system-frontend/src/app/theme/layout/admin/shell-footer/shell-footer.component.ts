import { Component } from '@angular/core';
import { environment } from '../../../../../environments/environment';

@Component({
  standalone: false,
  selector: 'app-shell-footer',
  templateUrl: './shell-footer.component.html',
  styleUrls: ['./shell-footer.component.scss']
})
export class ShellFooterComponent {
  readonly year = new Date().getFullYear();
  readonly version = environment.appVersion ?? '';
  readonly envLabelKey = environment.production ? 'APP.ENV_PRODUCTION' : 'APP.ENV_LOCAL';
}
