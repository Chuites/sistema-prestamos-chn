import { Routes } from '@angular/router';

import { Clientes } from './pages/clientes/clientes';
import { Dashboard } from './pages/dashboard/dashboard';
import { Pagos } from './pages/pagos/pagos';
import { Prestamos } from './pages/prestamos/prestamos';
import { Solicitudes } from './pages/solicitudes/solicitudes';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard'
  },
  {
    path: 'dashboard',
    component: Dashboard
  },
  {
    path: 'clientes',
    component: Clientes
  },
  {
    path: 'solicitudes',
    component: Solicitudes
  },
  {
    path: 'prestamos',
    component: Prestamos
  },
  {
    path: 'pagos',
    component: Pagos
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];