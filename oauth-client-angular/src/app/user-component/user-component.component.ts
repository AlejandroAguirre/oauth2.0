import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { AuthService } from '../Service/auth.service';
import { CommonModule } from '@angular/common';
import { UserResponse } from '../model/UserResponse';
import { UserService } from '../Service/UserService';

@Component({
  selector: 'app-user-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-component.component.html',
  styleUrl: './user-component.component.css',
})
export class UserComponentComponent implements OnInit {
  user?: UserResponse;

  constructor(
    private router: Router,
    private authService: AuthService,
    private userService: UserService,
  ) {}

  ngOnInit(): void {
    this.userService.getUser().subscribe({
      next: (response) => {
        this.user = response;
      },
      error: (error) => {
        console.error('Error Resource Server:', error);
      },
    });
  }

  logout(): void {
    const confirmar = window.confirm(
      '¿Estás seguro de que deseas cerrar sesión?',
    );
    if (confirmar) {
      this.authService.logout();
    }
  }
}
