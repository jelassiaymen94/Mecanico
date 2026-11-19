import { DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  ITEM_CONDITION_LABELS,
  LISTING_CATEGORY_LABELS,
  Listing,
} from '../../../../core/models/listing.model';
import { StatusBadgeComponent } from '../../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-listing-card',
  standalone: true,
  imports: [DecimalPipe, RouterLink, StatusBadgeComponent],
  templateUrl: './listing-card.component.html',
  styleUrl: './listing-card.component.css',
})
export class ListingCardComponent {
  readonly listing = input.required<Listing>();

  readonly categoryLabel = computed(() => LISTING_CATEGORY_LABELS[this.listing().category]);
  readonly conditionLabel = computed(() => ITEM_CONDITION_LABELS[this.listing().condition]);
}
