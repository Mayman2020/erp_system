import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Pipe, PipeTransform } from '@angular/core';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbDropdownConfig, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { BehaviorSubject, of } from 'rxjs';

import { NavRightComponent } from './nav-right.component';
import { AuthService } from '../../../../../core/auth/auth.service';
import { ThemeService } from '../../../../../core/services/theme.service';
import { TranslationService } from '../../../../../core/i18n/translation.service';

@Pipe({ name: 't' })
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
      providers: [
        NgbDropdownConfig,
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
        {
          provide: AuthService,
          useValue: {
            refreshCurrentUser: jasmine.createSpy('refreshCurrentUser'),
            isAuthenticated$: of(false),
            currentUser$: of(null),
            loadingUser$: of(false),
            logout: jasmine.createSpy('logout'),
          },
        },
        {
          provide: ThemeService,
          useValue: {
            mode: 'light',
            mode$: of('light'),
            toggleTheme: jasmine.createSpy('toggleTheme'),
          },
        },
        {
          provide: TranslationService,
          useValue: {
            currentLanguage$: new BehaviorSubject('ar').asObservable(),
          },
        },
      ],
    })
    .compileComponents();
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
