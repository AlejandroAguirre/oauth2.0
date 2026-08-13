import { Component } from '@angular/core';
import { AuthService } from '../Service/auth.service';

@Component({
  selector: 'app-home-component',
  standalone: true,
  imports: [],
  templateUrl: './home-component.component.html',
  styleUrl: './home-component.component.css',
})
export class HomeComponent {
  mensaje = '';
  conectando = false;
  constructor(private authService: AuthService) {}

  login(): void {
    this.conectando = true;
    this.mensaje = 'Preparando conexión...';
    setTimeout(() => {
      this.mensaje = 'Conectando con API Server Oauth...';
    }, 2000);
    setTimeout(() => {
      this.mensaje = 'Redirigiendo al Authorization Server...';
      setTimeout(() => {
        this.authService.login();
      }, 1500);
    }, 4000);
  }
}
