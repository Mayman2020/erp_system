import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';

import { NavGroupComponent } from './nav-group.component';

@Pipe({ name: 't' })
class MockTranslatePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

describe('NavGroupComponent', () => {
  let component: NavGroupComponent;
  let fixture: ComponentFixture<NavGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [NavGroupComponent, MockTranslatePipe],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavGroupComponent);
    component = fixture.componentInstance;
    component.item = {
      id: 'test-group',
      title: 'NAV.HESABATY',
      type: 'group',
      icon: 'work',
      children: [
        {
          id: 'dashboard',
          title: 'NAV.DASHBOARD',
          type: 'item',
          url: '/dashboard',
          classes: 'nav-item',
          icon: 'home'
        }
      ]
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
