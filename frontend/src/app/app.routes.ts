import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'marketplace',
  },
  {
    path: 'marketplace',
    loadChildren: () =>
      import('./features/marketplace/marketplace.routes').then(
        (module) => module.MARKETPLACE_ROUTES,
      ),
  },
  {
    path: 'dispatch',
    title: 'Dépannage & suivi · Mecanico',
    loadComponent: () =>
      import('./features/dispatch/pages/dispatch-page/dispatch-page.component').then(
        (module) => module.DispatchPageComponent,
      ),
  },
  {
    path: '**',
    redirectTo: 'marketplace',
  },
];
