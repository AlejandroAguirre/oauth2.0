import { Component, OnInit } from '@angular/core';

import { RouterOutlet } from '@angular/router';
import { AuthService } from './Service/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

  title = 'oauth-client-angular';

  constructor(private authService: AuthService) {}

  async ngOnInit(): Promise<void> {
    await this.authService.init();
  }
}