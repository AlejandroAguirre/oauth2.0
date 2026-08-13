import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { AuthService } from '../Service/auth.service';

@Component({
  selector: 'app-callback-component',
  standalone: true,
  imports: [],
  templateUrl: './callback-component.component.html',
  styleUrl: './callback-component.component.css',
})
export class CallbackComponent implements OnInit {

  mensaje = 'Procesando autenticación...';

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
  ) {}

async ngOnInit(): Promise<void> {
  try {
    await this.authService.init();
    await new Promise(resolve => setTimeout(resolve, 1000));
    const token = this.authService.getAccessToken();

    if (!token) {
      console.error('No hay access token');
      this.mensaje = 'No se pudo obtener el token';
      return;
    }
    console.log('Usuario autenticado');
    this.router.navigate(['/user']);

  } catch (error) {
    console.error('OAUTH ERROR:', error);
    this.mensaje = 'No se pudo completar la autenticación';
  }
}
}

