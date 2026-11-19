import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import {
  LISTING_CATEGORY_OPTIONS,
  LISTING_STATUS_LABELS,
  Listing,
  ListingCategory,
  ListingStatus,
} from '../../../../core/models/listing.model';
import { ListingService } from '../../../../core/services/listing.service';
import { apiErrorMessage } from '../../../../core/utils/http-error';
import { ListingCardComponent } from '../../components/listing-card/listing-card.component';

@Component({
  selector: 'app-marketplace-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ListingCardComponent],
  templateUrl: './marketplace-page.component.html',
  styleUrl: './marketplace-page.component.css',
})
export class MarketplacePageComponent implements OnInit {
  private readonly listingService = inject(ListingService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly categoryOptions = LISTING_CATEGORY_OPTIONS;
  readonly statusOptions = Object.entries(LISTING_STATUS_LABELS) as [ListingStatus, string][];

  readonly listings = signal<Listing[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);

  readonly filters = this.formBuilder.nonNullable.group({
    q: [''],
    category: ['' as ListingCategory | ''],
    city: [''],
    status: ['' as ListingStatus | ''],
  });

  ngOnInit(): void {
    this.loadListings(0);

    this.filters.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged((previous, current) => JSON.stringify(previous) === JSON.stringify(current)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.loadListings(0));
  }

  loadListings(page: number): void {
    this.loading.set(true);
    this.error.set(null);

    const filters = this.filters.getRawValue();

    this.listingService
      .getListings({
        q: filters.q || undefined,
        category: filters.category || undefined,
        city: filters.city || undefined,
        status: filters.status || undefined,
        page,
        size: 12,
      })
      .subscribe({
        next: (response) => {
          this.listings.set(response.content);
          this.page.set(response.page);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
          this.loading.set(false);
        },
        error: (error) => {
          this.error.set(apiErrorMessage(error, 'Impossible de charger les annonces.'));
          this.loading.set(false);
        },
      });
  }

  resetFilters(): void {
    this.filters.reset({
      q: '',
      category: '',
      city: '',
      status: '',
    });
  }

  previousPage(): void {
    if (this.page() > 0) {
      this.loadListings(this.page() - 1);
    }
  }

  nextPage(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.loadListings(this.page() + 1);
    }
  }
}
