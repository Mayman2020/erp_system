import { TestBed } from '@angular/core/testing';

import { ApexChartService } from './apex-chart.service';

describe('ApexChartService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [ApexChartService]
  }));

  it('should be created', () => {
    const service: ApexChartService = TestBed.inject(ApexChartService);
    expect(service).toBeTruthy();
  });
});
