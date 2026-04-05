import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.removeItem('erp_theme_mode');
    document.documentElement.removeAttribute('data-theme');
    document.body.className = '';
    TestBed.configureTestingModule({});
  });

  it('should set data-theme on documentElement', () => {
    const service = TestBed.inject(ThemeService);
    service.setMode('dark');
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    service.setMode('light');
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
  });

  it('init should apply current mode', () => {
    const service = TestBed.inject(ThemeService);
    service.init();
    expect(['light', 'dark']).toContain(document.documentElement.getAttribute('data-theme'));
  });
});
