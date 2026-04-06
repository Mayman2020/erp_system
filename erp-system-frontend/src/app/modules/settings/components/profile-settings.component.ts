import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthUser, resolveProfileFullName, UpdateProfileRequest } from '../../../core/auth/auth.service';
import { ThemeMode } from '../../../core/services/theme.service';

@Component({
  standalone: false,
  selector: 'app-profile-settings',
  templateUrl: './profile-settings.component.html',
  styleUrls: ['./profile-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileSettingsComponent {
  @Input() user: AuthUser | null = null;
  @Input() themeMode: ThemeMode = 'light';
  @Input() currentLanguage = 'ar';
  @Input() saving = false;
  @Input() errorKey = '';
  @Input() successKey = '';

  @Output() saveProfile = new EventEmitter<UpdateProfileRequest>();
  @Output() changeTheme = new EventEmitter<ThemeMode>();
  @Output() changeLanguage = new EventEmitter<string>();

  form: FormGroup;
  imagePreview = '';

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      fullNameEn: ['', [Validators.required, Validators.maxLength(150)]],
      fullNameAr: ['', [Validators.required, Validators.maxLength(150)]],
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(190)]],
      phone: ['', [Validators.required, Validators.maxLength(30)]],
      nationalId: ['', [Validators.maxLength(60)]],
      companyNameEn: ['', [Validators.maxLength(180)]],
      companyNameAr: ['', [Validators.maxLength(180)]],
      profileImage: ['']
    });
  }

  headerDisplayName(): string {
    if (!this.user) {
      return '';
    }
    return resolveProfileFullName(this.user.profile, this.currentLanguage);
  }

  ngOnChanges(): void {
    if (this.user) {
      this.imagePreview = this.user.profile?.profileImage || '';
      this.form.patchValue({
        fullNameEn: this.user.profile?.fullNameEn || '',
        fullNameAr: this.user.profile?.fullNameAr || '',
        username: this.user.username || '',
        email: this.user.email || '',
        phone: this.user.phone || '',
        nationalId: this.user.profile?.nationalId || '',
        companyNameEn: this.user.profile?.companyNameEn || '',
        companyNameAr: this.user.profile?.companyNameAr || '',
        profileImage: this.user.profile?.profileImage || ''
      });
    }
  }

  onSelectImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const data = (reader.result || '').toString();
      this.form.patchValue({ profileImage: data });
      this.imagePreview = data;
    };
    reader.readAsDataURL(file);
  }

  onSave(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saveProfile.emit(this.form.value);
  }
}
