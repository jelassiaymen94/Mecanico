import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { apiErrorMessage } from '../../../core/api-error';
import { AuthService } from '../../../core/auth.service';
import {
  FUEL_LABELS,
  PageResponse,
  TRANSMISSION_LABELS,
  Vehicle,
  VEHICLE_STATUS_LABELS,
  VEHICLE_STATUSES,
  VehicleStatus,
  formatKm,
  gradeClass,
  statusClass,
} from '../../../core/models';
import { PassportApi } from '../../../core/passport-api.service';
import { ToastService } from '../../../core/toast.service';

@Component({
  selector: 'app-vehicle-list',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './vehicle-list.html',
  styleUrl: './vehicle-list.scss',
})
export class VehicleListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(PassportApi);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);

  readonly page = signal<PageResponse<Vehicle> | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly deleteTarget = signal<Vehicle | null>(null);
  readonly deleting = signal(false);
  readonly readonlyPageSize = 8;

  readonly statusLabels = VEHICLE_STATUS_LABELS;
  readonly statuses = VEHICLE_STATUSES;

  readonly filters = this.fb.nonNullable.group({
    search: [''],
    status: ['' as VehicleStatus | ''],
  });

  readonly visiblePages = computed(() => {
    const data = this.page();
    if (!data || data.totalPages <= 1) {
      return [];
    }
    const total = data.totalPages;
    const current = data.page;
    const start = Math.max(0, Math.min(current - 2, total - 5));
    const end = Math.min(total, start + 5);
    const pages: number[] = [];
    for (let index = start; index < end; index++) {
      pages.push(index);
    }
    return pages;
  });

  ngOnInit(): void {
    this.load(0);
  }

  load(pageIndex: number): void {
    this.loading.set(true);
    this.error.set('');
    const { search, status } = this.filters.getRawValue();
    this.api
      .vehicles({ search, status, page: pageIndex, size: this.readonlyPageSize })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: (error) => {
          this.loading.set(false);
          this.error.set(apiErrorMessage(error, 'Unable to load vehicles.'));
        },
      });
  }

  applyFilters(): void {
    this.load(0);
  }

  resetFilters(): void {
    this.filters.reset({ search: '', status: '' });
    this.load(0);
  }

  changePage(pageIndex: number): void {
    const data = this.page();
    if (!data || pageIndex < 0 || pageIndex >= data.totalPages || pageIndex === data.page) {
      return;
    }
    this.load(pageIndex);
  }

  askDelete(vehicle: Vehicle): void {
    this.deleteTarget.set(vehicle);
  }

  cancelDelete(): void {
    this.deleteTarget.set(null);
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) {
      return;
    }
    this.deleting.set(true);
    this.api.deleteVehicle(target.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.deleteTarget.set(null);
        this.toast.success(`${this.vehicleTitle(target)} was removed.`);
        const data = this.page();
        const nextPage = data && data.content.length === 1 && data.page > 0 ? data.page - 1 : (data?.page ?? 0);
        this.load(nextPage);
      },
      error: (error) => {
        this.deleting.set(false);
        this.toast.error(apiErrorMessage(error, 'Unable to delete this vehicle.'));
      },
    });
  }

  vehicleTitle(vehicle: Vehicle): string {
    return vehicle.nickname?.trim() || `${vehicle.brandName} ${vehicle.modelName}`;
  }

  fuelLabel(vehicle: Vehicle): string {
    return FUEL_LABELS[vehicle.fuelType] ?? vehicle.fuelType;
  }

  transmissionLabel(vehicle: Vehicle): string {
    return vehicle.transmission ? (TRANSMISSION_LABELS[vehicle.transmission] ?? vehicle.transmission) : '—';
  }

  statusLabel(vehicle: Vehicle): string {
    return VEHICLE_STATUS_LABELS[vehicle.status] ?? vehicle.status;
  }

  statusCss(vehicle: Vehicle): string {
    return `badge-soft badge-soft--${statusClass(vehicle.status)}`;
  }

  gradeCss(vehicle: Vehicle): string {
    return `grade-text ${gradeClass(vehicle.healthGrade)}`;
  }

  readonly formatKm = formatKm;
  readonly gradeClass = gradeClass;
}
