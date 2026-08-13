import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponse } from '../model/UserResponse';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl = `${environment.api.baseUrl}/resources/user`;

  constructor(private http: HttpClient) {}

  getUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(this.apiUrl);
  }
}