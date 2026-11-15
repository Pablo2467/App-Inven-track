import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginPage } from './pages/login';
import { Dashboard } from './pages/dashboard';
import { CrudPage } from './pages/crud-page';
import { BATCHES, ENTRIES, EXITS, PRODUCTS, SUPPLIERS } from './pages/crud-configs';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'products', component: CrudPage, data: { config: PRODUCTS } },
      { path: 'suppliers', component: CrudPage, data: { config: SUPPLIERS } },
      { path: 'entries', component: CrudPage, data: { config: ENTRIES } },
      { path: 'exits', component: CrudPage, data: { config: EXITS } },
      { path: 'batches', component: CrudPage, data: { config: BATCHES } },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];