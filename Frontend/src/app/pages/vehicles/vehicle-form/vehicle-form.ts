import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { apiErrorMessage } from '../../../core/api-error';
import { AuthService } from '../../../core/auth.service';
import {
  FUEL_LABELS,
  FUEL_TYPES,
  FuelType,
  TRANSMISSION_LABELS,
  TRANSMISSION_TYPES,
  TransmissionType,
  VEHICLE_STATUS_LABELS,
  VEHICLE_STATUSES,
  VEHICLE_TYPE_LABELS,
  VEHICLE_TYPES,
  Vehicle,
  VehicleBrand,
  VehicleModel,
  VehiclePayload,
  VehicleStatus,
  VehicleType,
} from '../../../core/models';
import { PassportApi } from '../../../core/passport-api.service';
import { ToastService } from '../../../core/toast.service';

@Component({
  selector: 'app-vehicle-form',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './vehicle-form.html',
  styleUrl: './vehicle-form.scss',
})
export class VehicleFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(PassportApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);

  readonly vehicleId = (() => {
    const raw = this.route.snapshot.paramMap.get('id');
    return raw ? Number(raw) : null;
  })();
  readonly isEdit = this.vehicleId !== null;

  readonly brands = signal<VehicleBrand[]>([]);
  readonly models = signal<VehicleModel[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly modelsLoading = signal(false);

  readonly fuelTypes = FUEL_TYPES;
  readonly transmissions = TRANSMISSION_TYPES;
  readonly vehicleTypes = VEHICLE_TYPES;
  readonly statuses = VEHICLE_STATUSES;
  readonly fuelLabels = FUEL_LABELS;
  readonly transmissionLabels = TRANSMISSION_LABELS;
  readonly vehicleTypeLabels = VEHICLE_TYPE_LABELS;
  readonly statusLabels = VEHICLE_STATUS_LABELS;
  readonly currentYear = new Date().getFullYear();

  readonly selectedModel = computed(() => {
    const id = this.form.controls.modelId.value;
    return this.models().find((model) => model.id === id) ?? null;
  });

  readonly selectedBrand = computed(() => {
    const id = this.form.controls.brandId.value;
    return this.brands().find((brand) => brand.id === id) ?? null;
  });

  readonly form = this.fb.group({
    brandId: this.fb.nonNullable.control(0, [Validators.required, Validators.min(1)]),
    modelId: this.fb.nonNullable.control(0, [Validators.required, Validators.min(1)]),
    nickname: this.fb.nonNullable.control('', [Validators.maxLength(80)]),
    year: this.fb.nonNullable.control(new Date().getFullYear(), [
      Validators.required,
      Validators.min(1950),
      Validators.max(2100),
    ]),
    plate: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(20)]),
    plateCountry: this.fb.nonNullable.control('TN', [Validators.maxLength(3)]),
    vin: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(11),
      Validators.maxLength(17),
      Validators.pattern(/^[A-Za-z0-9]+$/),
    ]),
    mileage: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
    fuelType: this.fb.nonNullable.control<FuelType>('PETROL', [Validators.required]),
    transmission: this.fb.nonNullable.control<TransmissionType>('MANUAL'),
    vehicleType: this.fb.nonNullable.control<VehicleType>('CAR'),
    engineCapacityCc: this.fb.control<number | null>(null, [Validators.min(50), Validators.max(15000)]),
    powerHp: this.fb.control<number | null>(null, [Validators.min(1), Validators.max(2000)]),
    registrationDate: this.fb.nonNullable.control(''),
    color: this.fb.nonNullable.control('', [Validators.maxLength(40)]),
    status: this.fb.nonNullable.control<VehicleStatus>('ACTIVE', [Validators.required]),
    ownerId: this.fb.control<number | null>(null, [Validators.min(1)]),
    photoUrl: this.fb.nonNullable.control('', [Validators.maxLength(500)]),
    notes: this.fb.nonNullable.control('', [Validators.maxLength(2000)]),
  });

  ngOnInit(): void {
    if (this.vehicleId !== null) {
      this.loadVehicle(this.vehicleId);
    } else {
      this.loadBrands();
    }
  }

  private loadBrands(): void {
    this.loading.set(true);
    this.api.brands().subscribe({
      next: (brands) => {
        this.brands.set(brands);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Unable to load the vehicle catalogue.'));
      },
    });
  }

  private loadVehicle(id: number): void {
    this.loading.set(true);
    forkJoin({ vehicle: this.api.vehicle(id), brands: this.api.brands() }).subscribe({
      next: ({ vehicle, brands }) => {
        this.brands.set(brands);
        this.patchForm(vehicle);
        this.loadModels(vehicle.brandId, vehicle.modelId);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Unable to load this vehicle.'));
      },
    });
  }

  private patchForm(vehicle: Vehicle): void {
    this.form.patchValue({
      brandId: vehicle.brandId,
      modelId: vehicle.modelId,
      nickname: vehicle.nickname ?? '',
      year: vehicle.year,
      plate: vehicle.plate,
      plateCountry: vehicle.plateCountry ?? 'TN',
      vin: vehicle.vin,
      mileage: vehicle.mileage,
      fuelType: vehicle.fuelType,
      transmission: vehicle.transmission ?? 'MANUAL',
      vehicleType: vehicle.vehicleType ?? 'CAR',
      engineCapacityCc: vehicle.engineCapacityCc ?? null,
      powerHp: vehicle.powerHp ?? null,
      registrationDate: vehicle.registrationDate ?? '',
      color: vehicle.color ?? '',
      status: vehicle.status,
      ownerId: vehicle.ownerId,
      photoUrl: vehicle.photoUrl ?? '',
      notes: vehicle.notes ?? '',
    });
  }

  onBrandChange(): void {
    const brandId = this.form.controls.brandId.value;
    this.form.controls.modelId.setValue(0);
    this.models.set([]);
    if (brandId > 0) {
      this.loadModels(brandId);
    }
  }

  onModelChange(): void {
    const model = this.selectedModel();
    if (!model) {
      return;
    }
    if (this.vehicleTypes.includes(model.bodyType as VehicleType)) {
      this.form.controls.vehicleType.setValue(model.bodyType as VehicleType);
    }
  }

  private loadModels(brandId: number, selectModelId?: number): void {
    this.modelsLoading.set(true);
    this.api.models(brandId).subscribe({
      next: (models) => {
        this.models.set(models);
        this.modelsLoading.set(false);
        if (selectModelId && models.some((model) => model.id === selectModelId)) {
          this.form.controls.modelId.setValue(selectModelId);
        }
      },
      error: (error) => {
        this.modelsLoading.set(false);
        this.toast.error(apiErrorMessage(error, 'Unable to load models for this brand.'));
      },
    });
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.error.set('Please review the highlighted fields before saving.');
      return;
    }
    const raw = this.form.getRawValue();
    const payload: VehiclePayload = {
      brandId: raw.brandId,
      modelId: raw.modelId,
      vin: raw.vin.trim().toUpperCase(),
      plate: raw.plate.trim().toUpperCase(),
      plateCountry: raw.plateCountry.trim().toUpperCase() || 'TN',
      year: raw.year,
      fuelType: raw.fuelType,
      mileage: raw.mileage,
      registrationDate: raw.registrationDate || null,
      color: raw.color.trim() || null,
      nickname: raw.nickname.trim() || null,
      vehicleType: raw.vehicleType,
      transmission: raw.transmission,
      engineCapacityCc: raw.engineCapacityCc || null,
      powerHp: raw.powerHp || null,
      photoUrl: raw.photoUrl.trim() || null,
      notes: raw.notes.trim() || null,
      status: raw.status,
    };
    if (this.isEdit && this.auth.isManager()) {
      payload.ownerId = raw.ownerId ?? null;
    } else if (!this.isEdit && this.auth.isManager() && raw.ownerId) {
      payload.ownerId = raw.ownerId;
    }

    this.saving.set(true);
    this.error.set('');
    const request$ =
      this.isEdit && this.vehicleId !== null
        ? this.api.updateVehicle(this.vehicleId, payload)
        : this.api.createVehicle(payload);
    request$.subscribe({
      next: (vehicle) => {
        this.saving.set(false);
        this.toast.success(this.isEdit ? 'Vehicle updated.' : 'Vehicle registered. Default 10,000 km reminders were created.');
        void this.router.navigate(['/vehicles', vehicle.id]);
      },
      error: (error) => {
        this.saving.set(false);
        this.error.set(apiErrorMessage(error, 'Unable to save this vehicle.'));
      },
    });
  }

  invalid(control: 'brandId' | 'modelId' | 'vin' | 'plate' | 'year' | 'mileage'): boolean {
    const field = this.form.controls[control];
    return field.touched && field.invalid;
  }
}
