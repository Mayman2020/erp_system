import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApexChartComponent } from './apex-chart.component';
import { ApexChartService } from './apex-chart.service';

describe('ApexChartComponent', () => {
  let component: ApexChartComponent;
  let fixture: ComponentFixture<ApexChartComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ApexChartComponent],
      providers: [ApexChartService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApexChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
