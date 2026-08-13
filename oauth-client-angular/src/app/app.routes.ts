import { Routes } from '@angular/router';

import { HomeComponent } from './home-component/home-component.component';
import { UserComponentComponent } from './user-component/user-component.component';
import { CallbackComponent } from './callback-component/callback-component.component';
import { authGuard } from '../guard/guard';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
  },
  {
    path: 'login-callback',
    component: CallbackComponent,
  },
  {
    path: 'user',
    component: UserComponentComponent,
    canActivate: [authGuard],
  },
  { path: 'logout', redirectTo: '', pathMatch: 'full' },
];
