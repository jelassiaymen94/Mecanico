import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { apiErrorMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import {
  DashboardSummary,
  ROLE_LABELS,
  Vehicle,
  VEHICLE_STATUS_LABELS,
  formatDate,
  formatKm,
  formatMoney,
  gradeClass,
  statusClass,
} from '../../core/models';
import { PassportApi } from '../../core/passport-api.service';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(PassportApi);
  private readonly toast = inject(ToastService);

  readonly summary = signal<DashboardSummary | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly scanning = signal(false);

  readonly roleLabel = computed(() => {
    const role = this.auth.user()?.role;
    return role ? (ROLE_LABELS[role] ?? role) : '';
  });

  readonly averageGrade = computed(() => this.gradeFor(this.summary()?.averageHealthScore ?? 0));

  readonly reminderCount = computed(() => {
    const data = this.summary();
    return data ? data.remindersDue + data.remindersOverdue : 0;
  });

  ngOnInit(): void {
    this.load();
    this.auth.refreshProfile().subscribe({ error: () => undefined });
  }

  load(showSpinner = true): void {
    if (showSpinner) {
      this.loading.set(true);
    }
    this.error.set('');
    this.api.summary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Unable to load the dashboard.'));
      },
    });
  }

  scan(): void {
    this.scanning.set(true);
    this.api.scan().subscribe({
      next: (result) => {
        this.scanning.set(false);
        this.toast.success(
          `Scan complete · ${result.documentsNotified} document(s) and ${result.remindersNotified} reminder(s) notified.`,
        );
        this.load(false);
      },
      error: (error) => {
        this.scanning.set(false);
        this.toast.error(apiErrorMessage(error, 'The notification scan failed.'));
      },
    });
  }

  vehicleTitle(vehicle: Vehicle): string {
    return vehicle.nickname?.trim() || `${vehicle.brandName} ${vehicle.modelName}`;
  }

  statusLabel(vehicle: Vehicle): string {
    return VEHICLE_STATUS_LABELS[vehicle.status] ?? vehicle.status;
  }

  statusCss(vehicle: Vehicle): string {
    return `badge-soft badge-soft--${statusClass(vehicle.status)}`;
  }

  gradeFor(score: number): string {
    if (score >= 85) {
      return 'EXCELLENT';
    }
    if (score >= 70) {
      return 'GOOD';
    }
    if (score >= 50) {
      return 'FAIR';
    }
    return 'POOR';
  }

  healthCss(score: number): string {
    return `health-dot ${gradeClass(this.gradeFor(score))}`;
  }

  alertIcon(type: string): string {
    return type === 'DOCUMENT' ? 'bi-file-earmark-text' : 'bi-bell';
  }

  severityCss(severity: string): string {
    return severity.toLowerCase();
  }

  dueText(dueDate?: string, mileage?: number): string {
    if (dueDate) {
      return `Due ${formatDate(dueDate)}`;
    }
    if (mileage !== undefined && mileage !== null) {
      return `Due at ${formatKm(mileage)}`;
    }
    return '';
  }

  readonly formatKm = formatKm;
  readonly formatMoney = formatMoney;
}
