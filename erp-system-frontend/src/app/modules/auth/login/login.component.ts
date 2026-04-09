import { Component, AfterViewInit, OnDestroy, PLATFORM_ID, Inject, ViewChild, ElementRef } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import gsap from 'gsap';
import { AuthService } from '../../../core/auth/auth.service';

declare var particlesJS: any;

@Component({
  standalone: false,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements AfterViewInit, OnDestroy {
  @ViewChild('loginCard', { read: ElementRef }) loginCardRef?: ElementRef<HTMLElement>;
  @ViewChild('brandPanel', { read: ElementRef }) brandPanelRef?: ElementRef<HTMLElement>;

  errorKey = '';
  loading = false;
  otpLoading = false;
  hidePassword = true;
  hideNewPassword = true;
  mode: 'login' | 'forgot' = 'login';
  otpSent = false;

  private loginIntroPlayed = false;
  private particlesInitialized = false;
  private particlesAttempts = 0;

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

  private readonly destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => this.initParticles(), 200);
      requestAnimationFrame(() => this.runLoginIntro());
    }
  }

  private initParticles(): void {
    if (this.particlesInitialized) {
      return;
    }
    if (typeof particlesJS === 'undefined') {
      if (this.particlesAttempts++ < 40) {
        setTimeout(() => this.initParticles(), 250);
      }
      return;
    }
    this.particlesInitialized = true;
    particlesJS('particles-js', {
      particles: {
        number: { value: 80, density: { enable: true, value_area: 800 } },
        color: { value: '#ffffff' },
        shape: { type: 'circle' },
        opacity: { value: 0.3, random: true, anim: { enable: true, speed: 1, opacity_min: 0.1, sync: false } },
        size: { value: 3, random: true, anim: { enable: true, speed: 2, size_min: 1, sync: false } },
        line_linked: { enable: true, distance: 150, color: '#ffffff', opacity: 0.2, width: 1 },
        move: { enable: true, speed: 1, direction: 'none', random: true, straight: false, out_mode: 'out', bounce: false }
      },
      interactivity: {
        detect_on: 'canvas',
        events: { onhover: { enable: true, mode: 'grab' }, onclick: { enable: true, mode: 'push' }, resize: true },
        modes: {
          grab: { distance: 140, line_linked: { opacity: 1 } },
          push: { particles_nb: 4 }
        }
      },
      retina_detect: true
    });
  }

  private runLoginIntro(): void {
    if (!isPlatformBrowser(this.platformId) || this.loginIntroPlayed) {
      return;
    }
    const card = this.loginCardRef?.nativeElement;
    if (!card) {
      return;
    }
    this.loginIntroPlayed = true;

    const tl = gsap.timeline({});
    tl.fromTo(
      card,
      { y: -120, scaleX: 0.22, scaleY: 0.52, opacity: 0 },
      { y: 0, scaleX: 0.22, scaleY: 0.52, opacity: 1, duration: 1.2, ease: 'power3.out' }
    );
    tl.to(card, { scaleY: 1, duration: 0.55, ease: 'power3.out' }, '-=0.35');
    tl.to(card, { scaleX: 1, duration: 0.65, ease: 'power3.out' }, '-=0.25');

    const staggerEls = card.querySelectorAll<HTMLElement>('.login-gsap-stagger');
    if (staggerEls.length) {
      gsap.set(staggerEls, { opacity: 0, y: -48 });
      gsap.to(staggerEls, {
        opacity: 1,
        y: 0,
        duration: 0.9,
        ease: 'power2.out',
        stagger: 0.12,
        delay: 0.4,
        clearProps: 'transform'
      });
    }

    const brand = this.brandPanelRef?.nativeElement;
    if (brand) {
      const rtl = getComputedStyle(document.documentElement).direction === 'rtl';
      gsap.from(brand, {
        x: rtl ? -72 : 72,
        opacity: 0,
        duration: 1.05,
        ease: 'elastic.out(1, 0.62)',
        delay: 0.75
      });
    }
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

