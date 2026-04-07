import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges
} from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';

@Component({
  standalone: false,
  selector: 'app-profile-card',
  templateUrl: './profile-card.component.html',
  styleUrls: ['./profile-card.component.scss'],
  animations: [
    trigger('erpProfileOverlay', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('220ms ease', style({ opacity: 1 }))
      ]),
      transition(':leave', [animate('160ms ease', style({ opacity: 0 }))])
    ]),
    trigger('erpProfilePanel', [
      transition(':enter', [
        style({ opacity: 0, transform: 'scale(0.96) translateY(-6px)' }),
        animate(
          '240ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ opacity: 1, transform: 'none' })
        )
      ]),
      transition(':leave', [
        animate(
          '180ms ease',
          style({ opacity: 0, transform: 'scale(0.98) translateY(-4px)' })
        )
      ])
    ])
  ]
})
export class ProfileCardComponent implements OnChanges, OnDestroy {
  @Input() displayName = '';
  @Input() roleKey = 'PROFILE.TITLE';
  @Input() avatarUrl = 'assets/images/user/avatar-1.jpg';
  @Input() loading = false;
  @Input() menuOpen = false;
  @Output() toggleMenu = new EventEmitter<void>();
  @Output() closeMenu = new EventEmitter<void>();
  @Output() profileClick = new EventEmitter<void>();
  @Output() logoutClick = new EventEmitter<void>();

  @HostListener('document:keydown', ['$event'])
  onDocumentEscape(e: KeyboardEvent): void {
    if (!this.menuOpen || e.key !== 'Escape') {
      return;
    }
    e.stopPropagation();
    this.closeMenu.emit();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['menuOpen']) {
      document.body.classList.toggle('erp-profile-overlay-lock', this.menuOpen);
    }
  }

  ngOnDestroy(): void {
    document.body.classList.remove('erp-profile-overlay-lock');
  }
}
