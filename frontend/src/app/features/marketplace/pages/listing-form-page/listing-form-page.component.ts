import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  ITEM_CONDITION_LABELS,
  LISTING_CATEGORY_OPTIONS,
  ListingCategory,
  ListingRequest,
  ItemCondition,
} from '../../../../core/models/listing.model';
import { ListingService } from '../../../../core/services/listing.service';
import { apiErrorMessage } from '../../../../core/utils/http-error';

@Component({
  selector: 'app-listing-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './listing-form-page.component.html',
  styleUrl: './listing-form-page.component.css',
})
export class ListingFormPageComponent implements OnInit {
  private readonly listingService = inject(ListingService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly categoryOptions = LISTING_CATEGORY_OPTIONS;
  readonly conditionOptions = (
    Object.keys(ITEM_CONDITION_LABELS) as ItemCondition[]
  ).map((value) => ({
    value,
    label: ITEM_CONDITION_LABELS[value],
  }));

  readonly listingId = this.readListingId();
  readonly isEdit = this.listingId !== null;

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    sellerId: ['seller-demo', [Validators.required, Validators.maxLength(64)]],
    title: ['', [Validators.required, Validators.maxLength(180)]],
    description: ['', [Validators.required, Validators.maxLength(4000)]],
    category: ['ENGINE' as ListingCategory, [Validators.required]],
    brand: ['', [Validators.maxLength(120)]],
    compatibleModels: ['', [Validators.maxLength(255)]],
    condition: ['USED' as ItemCondition, [Validators.required]],
    price: [0, [Validators.required, Validators.min(0)]],
    quantity: [1, [Validators.required, Validators.min(0)]],
    city: ['Tunis', [Validators.required, Validators.maxLength(120)]],
  });

  ngOnInit(): void {
    if (this.listingId === null) {
      return;
    }

    this.loading.set(true);
    this.listingService
      .getListing(this.listingId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (listing) => {
          this.form.patchValue({
            sellerId: listing.sellerId,
            title: listing.title,
            description: listing.description,
            category: listing.category,
            brand: listing.brand ?? '',
            compatibleModels: listing.compatibleModels ?? '',
            condition: listing.condition,
            price: listing.price,
            quantity: listing.quantity,
            city: listing.city,
          });
          this.loading.set(false);
        },
        error: (error) => {
          this.error.set(apiErrorMessage(error, 'Impossible de charger cette annonce.'));
          this.loading.set(false);
        },
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: ListingRequest = {
      sellerId: value.sellerId.trim(),
      title: value.title.trim(),
      description: value.description.trim(),
      category: value.category,
      brand: value.brand.trim() || null,
      compatibleModels: value.compatibleModels.trim() || null,
      condition: value.condition,
      price: Number(value.price),
      quantity: Number(value.quantity),
      city: value.city.trim(),
    };

    this.saving.set(true);
    this.error.set(null);

    const operation =
      this.listingId !== null
        ? this.listingService.updateListing(this.listingId, request)
        : this.listingService.createListing(request);

    operation.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (listing) => {
        this.saving.set(false);
        void this.router.navigate(['/marketplace', listing.id]);
      },
      error: (error) => {
        this.error.set(apiErrorMessage(error, 'Impossible d’enregistrer l’annonce.'));
        this.saving.set(false);
      },
    });
  }

  isInvalid(
    controlName:
      | 'sellerId'
      | 'title'
      | 'description'
      | 'category'
      | 'brand'
      | 'compatibleModels'
      | 'condition'
      | 'price'
      | 'quantity'
      | 'city',
  ): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.touched || control.dirty);
  }

  private readListingId(): number | null {
    const rawId = this.route.snapshot.paramMap.get('id');
    if (!rawId) {
      return null;
    }

    const parsedId = Number(rawId);
    return Number.isFinite(parsedId) ? parsedId : null;
  }
}
