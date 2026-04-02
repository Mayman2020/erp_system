import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({ standalone: false,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  errorKey = '';
  loading = false;
  otpLoading = false;
  hidePassword = true;
  hideNewPassword = true;
  darkMode = false;
  mode: 'login' | 'forgot' = 'login';
  otpSent = false;

  readonly form = this.fb.group({
    email: [localStorage.getItem('erp_login_email') || '', [Validators.required, Validators.email]],
    rememberMe: [!!localStorage.getItem('erp_login_email')],
    password: ['', Validators.required]
  });

  readonly forgotForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly resetForm = this.fb.group({
    otpCode: ['', [Validators.required, Validators.minLength(4)]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private themeService: ThemeService
  ) {
    this.darkMode = this.themeService.mode === 'dark';
  }

  toggleTheme(): void {
    this.themeService.toggle();
    this.darkMode = this.themeService.mode === 'dark';
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorKey = '';
    const email = (this.form.value.email || '').trim();
    const rememberMe = !!this.form.value.rememberMe;
    if (rememberMe) {
      localStorage.setItem('erp_login_email', email);
    } else {
      localStorage.removeItem('erp_login_email');
    }
    this.auth.login({
      email,
      password: String(this.form.value.password || '')
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.errorKey = 'AUTH.INVALID_CREDENTIALS';
      }
    });
  }

  sendOtp(): void {
    if (this.forgotForm.invalid || this.otpLoading) {
      this.forgotForm.markAllAsTouched();
      return;
    }
    this.otpLoading = true;
    this.errorKey = '';
    const email = (this.forgotForm.value.email || '').trim();
    this.auth.sendPasswordResetOtp(email).subscribe({
      next: () => {
        this.otpLoading = false;
        this.otpSent = true;
      },
      error: () => {
        this.otpLoading = false;
        this.errorKey = 'COMMON.UNEXPECTED_ERROR';
      }
    });
  }

  resetPassword(): void {
    if (this.resetForm.invalid || this.otpLoading) {
      this.resetForm.markAllAsTouched();
      return;
    }
    const email = (this.forgotForm.value.email || '').trim();
    this.otpLoading = true;
    this.auth.resetPasswordWithOtp(email, this.resetForm.value.otpCode || '', this.resetForm.value.newPassword || '').subscribe({
      next: () => {
        this.otpLoading = false;
        this.mode = 'login';
        this.otpSent = false;
        this.errorKey = '';
      },
      error: () => {
        this.otpLoading = false;
        this.errorKey = 'AUTH.INVALID_CREDENTIALS';
      }
    });
  }
}

