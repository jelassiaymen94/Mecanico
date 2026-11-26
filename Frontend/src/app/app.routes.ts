import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { guestGuard } from './core/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.DashboardComponent),
      },
      {
        path: 'vehicles',
        loadComponent: () =>
          import('./pages/vehicles/vehicle-list/vehicle-list').then((m) => m.VehicleListComponent),
      },
      {
        path: 'vehicles/new',
        loadComponent: () =>
          import('./pages/vehicles/vehicle-form/vehicle-form').then((m) => m.VehicleFormComponent),
      },
      {
        path: 'vehicles/:id',
        loadComponent: () =>
          import('./pages/vehicles/vehicle-detail/vehicle-detail').then((m) => m.VehicleDetailComponent),
      },
      {
        path: 'vehicles/:id/edit',
        loadComponent: () =>
          import('./pages/vehicles/vehicle-form/vehicle-form').then((m) => m.VehicleFormComponent),
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./pages/notifications/notifications').then((m) => m.NotificationsComponent),
      },
      // MS3 - Marketplace & live tracking (wrapped so its global styles stay scoped)
      {
        path: '',
        loadComponent: () =>
          import('./layout/marketplace-scope').then((m) => m.MarketplaceScopeComponent),
        children: [
          {
            path: 'marketplace',
            loadChildren: () =>
              import('./features/marketplace/marketplace.routes').then((m) => m.MARKETPLACE_ROUTES),
          },
          {
            path: 'dispatch',
            title: 'Dépannage & suivi · Mecanico',
            loadComponent: () =>
              import('./features/dispatch/pages/dispatch-page/dispatch-page.component').then(
                (m) => m.DispatchPageComponent,
              ),
          },
        ],
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
