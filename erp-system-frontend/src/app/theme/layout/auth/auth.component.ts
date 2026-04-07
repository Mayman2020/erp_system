import { Component, OnDestroy, OnInit } from '@angular/core';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  standalone: false,
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent implements OnInit, OnDestroy {
  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    this.themeService.setAuthRouteDarkOnly(true);
  }

  ngOnDestroy(): void {
    this.themeService.setAuthRouteDarkOnly(false);
  }
}
