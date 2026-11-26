import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Wraps the MS3 (Marketplace & live tracking) pages so that their design system
 * (`src/styles/_marketplace.scss`) only applies inside `.mp-scope` and does not
 * override the Bootstrap styles used by the rest of the portal.
 */
@Component({
  selector: 'app-marketplace-scope',
  imports: [RouterOutlet],
  template: `<div class="mp-scope"><router-outlet /></div>`,
})
export class MarketplaceScopeComponent {}
