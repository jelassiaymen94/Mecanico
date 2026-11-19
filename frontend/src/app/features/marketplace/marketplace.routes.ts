import { Routes } from '@angular/router';

export const MARKETPLACE_ROUTES: Routes = [
  {
    path: '',
    title: 'Marketplace · Mecanico',
    loadComponent: () =>
      import('./pages/marketplace-page/marketplace-page.component').then(
        (module) => module.MarketplacePageComponent,
      ),
  },
  {
    path: 'new',
    title: 'Publier une annonce · Mecanico',
    loadComponent: () =>
      import('./pages/listing-form-page/listing-form-page.component').then(
        (module) => module.ListingFormPageComponent,
      ),
  },
  {
    path: ':id/edit',
    title: 'Modifier une annonce · Mecanico',
    loadComponent: () =>
      import('./pages/listing-form-page/listing-form-page.component').then(
        (module) => module.ListingFormPageComponent,
      ),
  },
  {
    path: ':id',
    title: 'Annonce · Mecanico',
    loadComponent: () =>
      import('./pages/listing-details-page/listing-details-page.component').then(
        (module) => module.ListingDetailsPageComponent,
      ),
  },
];
