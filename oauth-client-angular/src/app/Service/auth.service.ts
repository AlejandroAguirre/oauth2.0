import { Injectable } from '@angular/core';
import { AuthConfig, OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../environments/environment';

const authConfig: AuthConfig = {
  issuer: environment.auth.issuer,
  redirectUri: window.location.origin + '/login-callback',
  postLogoutRedirectUri: window.location.origin + '/logout',
  clientId: environment.auth.clientId,
  responseType: 'code',
  scope: environment.auth.scope,
  requireHttps: false,
  showDebugInformation: true
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private oauthService: OAuthService) {
    this.oauthService.configure(authConfig);
  }

  async init(): Promise<void> {
    await this.oauthService.loadDiscoveryDocumentAndTryLogin();
  }

  login(): void {
    this.oauthService.initCodeFlow();
  }

  logout(): void {
    this.oauthService.logOut();
  }

  getAccessToken(): string {
    return this.oauthService.getAccessToken();
  }

  isLoggedIn(): boolean {
    return this.oauthService.hasValidAccessToken();
  }
}