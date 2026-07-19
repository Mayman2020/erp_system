import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Pipe, PipeTransform, NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbDropdownConfig, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { BehaviorSubject, of } from 'rxjs';

import { NavRightComponent } from './nav-right.component';
import { AuthService } from '../../../../../core/auth/auth.service';
import { ThemeService } from '../../../../../core/services/theme.service';
import { TranslationService } from '../../../../../core/i18n/translation.service';
import { NotificationService } from '../../../../../core/services/notification.service';

@Pipe({ name: 'translate' })
class StubTranslatePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

describe('NavRightComponent', () => {
  let component: NavRightComponent;
  let fixture: ComponentFixture<NavRightComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, NgbDropdownModule],
      declarations: [NavRightComponent, StubTranslatePipe],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        NgbDropdownConfig,
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
        {
          provide: AuthService,
          useValue: {
            token: null,
            refreshCurrentUser: jasmine.createSpy('refreshCurrentUser'),
            isAuthenticated$: of(false),
            currentUser$: of(null),
            logout: jasmine.createSpy('logout')
          }
        },
        {
          provide: ThemeService,
          useValue: {
            mode: 'light',
            mode$: of('light'),
            toggleTheme: jasmine.createSpy('toggleTheme')
          }
        },
        {
          provide: TranslationService,
          useValue: {
            currentLanguage: 'ar',
            currentLanguage$: new BehaviorSubject('ar').asObservable(),
            setLanguage: jasmine.createSpy('setLanguage').and.returnValue(of({}))
          }
        },
        {
          provide: NotificationService,
          useValue: {
            unreadCount: jasmine.createSpy('unreadCount').and.returnValue(of(0))
          }
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavRightComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
