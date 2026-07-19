import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return newPassword && confirmPassword && newPassword !== confirmPassword ? { passwordMismatch: true } : null;
}

@Component({
  standalone: false,
  selector: 'app-force-password-change',
  templateUrl: './force-password-change.component.html',
  styleUrls: ['./force-password-change.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForcePasswordChangeComponent {
  saving = false;
  errorKey = '';

  readonly form = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordsMatchValidator });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  submit(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.errorKey = '';
    const { currentPassword, newPassword } = this.form.getRawValue();

    this.authService.changePassword(currentPassword!, newPassword!).pipe(
      finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => this.errorKey = err?.error?.message || 'PROFILE.CHANGE_PASSWORD_ERROR'
    });
  }
}
