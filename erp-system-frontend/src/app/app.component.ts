import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
import { ThemeService } from './core/services/theme.service';

@Component({ standalone: false,
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(private router: Router, private themeService: ThemeService) { }

  ngOnInit() {
    this.themeService.init();
    this.router.events.subscribe((evt) => {
      if (!(evt instanceof NavigationEnd)) {
        return;
      }
      window.scrollTo(0, 0);
    });
  }
}
