import { Component, computed, input } from '@angular/core';
import { LISTING_STATUS_LABELS, ListingStatus } from '../../../core/models/listing.model';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `<span [class]="badgeClass()">{{ label() }}</span>`,
  styleUrl: './status-badge.component.css',
})
export class StatusBadgeComponent {
  readonly status = input.required<ListingStatus>();

  readonly label = computed(() => LISTING_STATUS_LABELS[this.status()]);
  readonly badgeClass = computed(() => `status-badge status-${this.status().toLowerCase()}`);
}
