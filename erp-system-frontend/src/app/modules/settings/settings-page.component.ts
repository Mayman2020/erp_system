import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService, AuthUser, UpdateProfileRequest } from '../../core/auth/auth.service';
import { TranslationService } from '../../core/i18n/translation.service';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-settings-page',
  templateUrl: './settings-page.component.html',
  styleUrls: ['./settings-page.component.scss']
})
export class SettingsPageComponent implements OnInit {
  titleKey = 'PROFILE.TITLE';
  loading = false;
  saving = false;
  errorKey = '';
  successKey = '';
  user: AuthUser | null = null;
  imagePreview = '';
  themeMode: ThemeMode = 'light';

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(150)]],
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(190)]],
    phone: ['', [Validators.required, Validators.maxLength(30)]],
    nationalId: ['', [Validators.maxLength(60)]],
    companyName: ['', [Validators.maxLength(180)]],
    profileImage: ['']
  });

  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private themeService: ThemeService,
    public translationService: TranslationService
  ) {}

  ngOnInit(): void {
    this.themeMode = this.themeService.mode;
    this.load();
  }

  onSelectImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length ? input.files[0] : null;
    if (!file) {
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const data = (reader.result || '').toString();
      this.form.patchValue({ profileImage: data });
      this.imagePreview = data;
    };
    reader.readAsDataURL(file);
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.errorKey = '';
    this.successKey = '';
    const payload: UpdateProfileRequest = {
      username: (this.form.value.username || '').trim(),
      email: (this.form.value.email || '').trim(),
      phone: (this.form.value.phone || '').trim(),
      fullName: (this.form.value.fullName || '').trim(),
      nationalId: (this.form.value.nationalId || '').trim() || null,
      companyName: (this.form.value.companyName || '').trim() || null,
      profileImage: (this.form.value.profileImage || '').trim() || null
    };
    this.authService.updateMyProfile(payload).subscribe(
      (user) => {
        this.user = user;
        this.imagePreview = user.profile?.profileImage || '';
        this.successKey = 'PROFILE.SAVE_SUCCESS';
        this.saving = false;
      },
      () => {
        this.errorKey = 'PROFILE.SAVE_ERROR';
        this.saving = false;
      }
    );
  }

  setTheme(mode: ThemeMode): void {
    this.themeMode = mode;
    this.themeService.setMode(mode);
  }

  setLanguage(lang: string): void {
    if (this.translationService.currentLanguage === lang) {
      return;
    }
    this.translationService.setLanguage(lang).subscribe();
  }

  private load(): void {
    this.loading = true;
    this.errorKey = '';
    this.successKey = '';
    this.authService.getMyProfile().subscribe(
      (user) => {
        this.user = user;
        this.imagePreview = user.profile?.profileImage || '';
        this.form.patchValue({
          fullName: user.profile?.fullName || '',
          username: user.username || '',
          email: user.email || '',
          phone: user.phone || '',
          nationalId: user.profile?.nationalId || '',
          companyName: user.profile?.companyName || '',
          profileImage: user.profile?.profileImage || ''
        });
        this.loading = false;
      },
      () => {
        this.errorKey = 'PROFILE.LOAD_ERROR';
        this.loading = false;
      }
    );
  }
}
