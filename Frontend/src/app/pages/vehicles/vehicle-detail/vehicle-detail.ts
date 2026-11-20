import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { apiErrorMessage } from '../../../core/api-error';
import { AuthService } from '../../../core/auth.service';
import {
  DOCUMENT_TYPE_LABELS,
  DOCUMENT_TYPES,
  DocumentType,
  FUEL_LABELS,
  RECORD_TYPE_LABELS,
  RECORD_TYPES,
  REMINDER_STATUS_LABELS,
  REMINDER_TYPE_LABELS,
  REMINDER_TYPES,
  RecordType,
  ReminderStatus,
  ReminderType,
  ServiceReminder,
  TRANSMISSION_LABELS,
  VEHICLE_STATUS_LABELS,
  VEHICLE_TYPE_LABELS,
  VehicleDocument,
  VehiclePassport,
  daysText,
  formatDate,
  formatDateTime,
  formatKm,
  formatMoney,
  gradeClass,
  kmUntilText,
  statusClass,
} from '../../../core/models';
import { OwnerDirectory } from '../../../core/owner-directory.service';
import { PassportApi } from '../../../core/passport-api.service';
import { ToastService } from '../../../core/toast.service';

type TabId = 'overview' | 'records' | 'documents' | 'reminders' | 'health';

interface ConfirmAction {
  kind: 'record' | 'document' | 'reminder';
  id: number;
  title: string;
  message: string;
}

@Component({
  selector: 'app-vehicle-detail',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './vehicle-detail.html',
  styleUrl: './vehicle-detail.scss',
})
export class VehicleDetailComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(PassportApi);
  private readonly owners = inject(OwnerDirectory);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);

  readonly vehicleId = Number(this.route.snapshot.paramMap.get('id'));
  readonly passport = signal<VehiclePassport | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly activeTab = signal<TabId>('overview');
  readonly ownerName = signal<string | null>(null);
  readonly ownerRole = signal<string | null>(null);

  readonly editingMileage = signal(false);
  readonly mileageInput = signal<number | null>(null);
  readonly savingMileage = signal(false);

  readonly deleteVehicleOpen = signal(false);
  readonly deletingVehicle = signal(false);

  readonly recordModalOpen = signal(false);
  readonly recordSaving = signal(false);
  readonly recordError = signal('');

  readonly documentModalOpen = signal(false);
  readonly editingDocument = signal<VehicleDocument | null>(null);
  readonly documentSaving = signal(false);
  readonly documentError = signal('');

  readonly reminderModalOpen = signal(false);
  readonly reminderSaving = signal(false);
  readonly reminderError = signal('');

  readonly confirmAction = signal<ConfirmAction | null>(null);
  readonly confirmBusy = signal(false);

  readonly recordTypes = RECORD_TYPES;
  readonly documentTypes = DOCUMENT_TYPES;
  readonly reminderTypes = REMINDER_TYPES;
  readonly recordLabels = RECORD_TYPE_LABELS;
  readonly documentLabels = DOCUMENT_TYPE_LABELS;
  readonly reminderLabels = REMINDER_TYPE_LABELS;
  readonly reminderStatusLabels = REMINDER_STATUS_LABELS;
  readonly fuelLabels = FUEL_LABELS;
  readonly transmissionLabels = TRANSMISSION_LABELS;
  readonly statusLabels = VEHICLE_STATUS_LABELS;
  readonly vehicleTypeLabels = VEHICLE_TYPE_LABELS;

  readonly recordForm = this.fb.group({
    type: this.fb.nonNullable.control<RecordType>('SERVICE', [Validators.required]),
    serviceDate: this.fb.nonNullable.control(this.today(), [Validators.required]),
    mileage: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
    description: this.fb.nonNullable.control('', [Validators.maxLength(2000)]),
    providerName: this.fb.nonNullable.control('', [Validators.maxLength(150)]),
    cost: this.fb.control<number | null>(null, [Validators.min(0)]),
    laborCost: this.fb.control<number | null>(null, [Validators.min(0)]),
    partsCost: this.fb.control<number | null>(null, [Validators.min(0)]),
    partName: this.fb.nonNullable.control('', [Validators.maxLength(150)]),
    partReference: this.fb.nonNullable.control('', [Validators.maxLength(80)]),
    nextDueDate: this.fb.nonNullable.control(''),
    nextDueMileage: this.fb.control<number | null>(null, [Validators.min(0)]),
  });

  readonly documentForm = this.fb.group({
    type: this.fb.nonNullable.control<DocumentType>('INSURANCE', [Validators.required]),
    number: this.fb.nonNullable.control('', [Validators.maxLength(80)]),
    issuer: this.fb.nonNullable.control('', [Validators.maxLength(150)]),
    issueDate: this.fb.nonNullable.control(''),
    expiryDate: this.fb.nonNullable.control(this.oneYearFromNow(), [Validators.required]),
    cost: this.fb.control<number | null>(null, [Validators.min(0)]),
    notes: this.fb.nonNullable.control('', [Validators.maxLength(2000)]),
    active: this.fb.nonNullable.control(true),
  });

  readonly reminderForm = this.fb.group({
    type: this.fb.nonNullable.control<ReminderType>('OIL_CHANGE', [Validators.required]),
    title: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(150)]),
    nextDueMileage: this.fb.control<number | null>(null, [Validators.min(0)]),
    expiryDate: this.fb.nonNullable.control(''),
    notes: this.fb.nonNullable.control('', [Validators.maxLength(2000)]),
  });

  readonly vehicle = computed(() => this.passport()?.vehicle ?? null);
  readonly health = computed(() => this.passport()?.health ?? null);
  readonly stats = computed(() => this.passport()?.stats ?? null);

  readonly tabs = computed(() => {
    const data = this.passport();
    return [
      { id: 'overview' as TabId, label: 'Overview', icon: 'bi-grid-1x2', count: null as number | null },
      { id: 'records' as TabId, label: 'Maintenance', icon: 'bi-tools', count: data?.records.length ?? 0 },
      { id: 'documents' as TabId, label: 'Documents', icon: 'bi-file-earmark-text', count: data?.documents.length ?? 0 },
      { id: 'reminders' as TabId, label: 'Reminders', icon: 'bi-bell', count: data?.reminders.length ?? 0 },
      { id: 'health' as TabId, label: 'Health score', icon: 'bi-heart-pulse', count: null },
    ];
  });

  readonly breakdown = computed(() => {
    const health = this.health();
    if (!health) {
      return [];
    }
    const definitions = [
      { key: 'age', label: 'Vehicle age', max: 20, icon: 'bi-calendar3' },
      { key: 'mileage', label: 'Mileage', max: 20, icon: 'bi-speedometer2' },
      { key: 'maintenance', label: 'Maintenance history', max: 25, icon: 'bi-tools' },
      { key: 'documents', label: 'Documents', max: 21, icon: 'bi-file-earmark-text' },
      { key: 'reminders', label: 'Reminders', max: 15, icon: 'bi-bell' },
    ];
    return definitions.map((definition) => {
      const penalty = health.breakdown[definition.key] ?? 0;
      return {
        ...definition,
        penalty,
        percent: Math.min(100, Math.round((penalty / definition.max) * 100)),
      };
    });
  });

  readonly gaugeBackground = computed(() => {
    const score = this.health()?.score ?? 0;
    const color = score >= 85 ? '#10b981' : score >= 70 ? '#0d9488' : score >= 50 ? '#f59e0b' : '#ef4444';
    return `conic-gradient(${color} ${score * 3.6}deg, #e2e8f0 0deg)`;
  });

  readonly ownerLabel = computed(() => {
    const vehicle = this.vehicle();
    if (!vehicle) {
      return '—';
    }
    if (vehicle.ownerId === this.auth.user()?.id) {
      return 'You';
    }
    return this.ownerName() ?? `Owner #${vehicle.ownerId}`;
  });

  ngOnInit(): void {
    this.load();
  }

  load(showSpinner = true): void {
    if (showSpinner) {
      this.loading.set(true);
    }
    this.error.set('');
    this.api.passport(this.vehicleId).subscribe({
      next: (passport) => {
        this.passport.set(passport);
        this.mileageInput.set(passport.vehicle.mileage);
        this.loading.set(false);
        this.owners.resolve(passport.vehicle.ownerId).subscribe({
          next: (owner) => {
            if (owner) {
              this.ownerName.set(owner.fullName);
              this.ownerRole.set(owner.role);
            }
          },
        });
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Unable to load this vehicle passport.'));
      },
    });
  }

  setTab(tab: TabId): void {
    this.activeTab.set(tab);
  }

  vehicleTitle(): string {
    const vehicle = this.vehicle();
    if (!vehicle) {
      return 'Vehicle';
    }
    return vehicle.nickname?.trim() || `${vehicle.brandName} ${vehicle.modelName}`;
  }

  statusCss(): string {
    const vehicle = this.vehicle();
    return vehicle ? `badge-soft badge-soft--${statusClass(vehicle.status)}` : 'badge-soft';
  }

  statusLabel(): string {
    const vehicle = this.vehicle();
    return vehicle ? (VEHICLE_STATUS_LABELS[vehicle.status] ?? vehicle.status) : '—';
  }

  typeBadge(kind: 'record' | 'document' | 'reminder', value: string): string {
    const normalized = value.toLowerCase().replace(/_/g, '-');
    return `badge-soft badge-soft--${kind === 'reminder' ? 'reminder' : normalized}`;
  }

  reminderStatusCss(status: ReminderStatus): string {
    return `badge-soft badge-soft--${status.toLowerCase()}`;
  }

  reminderIcon(type: ReminderType): string {
    switch (type) {
      case 'OIL_CHANGE':
        return 'bi-droplet';
      case 'TIRE_ROTATION':
        return 'bi-circle';
      case 'BRAKE_CHECK':
        return 'bi-disc';
      case 'BATTERY_CHECK':
        return 'bi-battery-charging';
      case 'TECHNICAL_INSPECTION':
        return 'bi-clipboard-check';
      case 'INSURANCE_RENEWAL':
        return 'bi-shield-check';
      default:
        return 'bi-bell';
    }
  }

  recordIcon(type: RecordType): string {
    switch (type) {
      case 'SERVICE':
        return 'bi-wrench-adjustable';
      case 'REPAIR':
        return 'bi-tools';
      case 'INSPECTION':
        return 'bi-clipboard-check';
      default:
        return 'bi-gear';
    }
  }

  documentIcon(type: DocumentType): string {
    switch (type) {
      case 'INSURANCE':
        return 'bi-shield-check';
      case 'INSPECTION':
        return 'bi-clipboard-check';
      case 'REGISTRATION':
        return 'bi-person-vcard';
      default:
        return 'bi-receipt';
    }
  }

  // Mileage -------------------------------------------------------------------
  startMileageEdit(): void {
    this.mileageInput.set(this.vehicle()?.mileage ?? 0);
    this.editingMileage.set(true);
  }

  cancelMileageEdit(): void {
    this.editingMileage.set(false);
    this.mileageInput.set(this.vehicle()?.mileage ?? 0);
  }

  saveMileage(): void {
    const current = this.vehicle()?.mileage ?? 0;
    const value = this.mileageInput();
    if (value === null || value < current) {
      this.toast.error(`The new mileage cannot be lower than the current ${formatKm(current)}.`);
      return;
    }
    this.savingMileage.set(true);
    this.api.updateMileage(this.vehicleId, value).subscribe({
      next: () => {
        this.savingMileage.set(false);
        this.editingMileage.set(false);
        this.toast.success('Mileage updated.');
        this.load(false);
      },
      error: (error) => {
        this.savingMileage.set(false);
        this.toast.error(apiErrorMessage(error, 'Unable to update the mileage.'));
      },
    });
  }

  // Records -------------------------------------------------------------------
  openRecordModal(): void {
    this.recordError.set('');
    this.recordForm.reset({
      type: 'SERVICE',
      serviceDate: this.today(),
      mileage: this.vehicle()?.mileage ?? 0,
      description: '',
      providerName: '',
      cost: null,
      laborCost: null,
      partsCost: null,
      partName: '',
      partReference: '',
      nextDueDate: '',
      nextDueMileage: null,
    });
    this.recordModalOpen.set(true);
  }

  closeRecordModal(): void {
    this.recordModalOpen.set(false);
  }

  submitRecord(): void {
    this.recordForm.markAllAsTouched();
    if (this.recordForm.invalid) {
      this.recordError.set('Please review the highlighted fields.');
      return;
    }
    const raw = this.recordForm.getRawValue();
    this.recordSaving.set(true);
    this.recordError.set('');
    this.api
      .addRecord(this.vehicleId, {
        type: raw.type,
        serviceDate: raw.serviceDate,
        mileage: raw.mileage,
        description: raw.description.trim() || null,
        providerName: raw.providerName.trim() || null,
        cost: raw.cost,
        currency: 'TND',
        laborCost: raw.laborCost,
        partsCost: raw.partsCost,
        partName: raw.partName.trim() || null,
        partReference: raw.partReference.trim() || null,
        nextDueDate: raw.nextDueDate || null,
        nextDueMileage: raw.nextDueMileage,
      })
      .subscribe({
        next: () => {
          this.recordSaving.set(false);
          this.recordModalOpen.set(false);
          this.toast.success('Maintenance record added.');
          this.load(false);
        },
        error: (error) => {
          this.recordSaving.set(false);
          this.recordError.set(apiErrorMessage(error, 'Unable to add this maintenance record.'));
        },
      });
  }

  // Documents -----------------------------------------------------------------
  openDocumentModal(document?: VehicleDocument): void {
    this.documentError.set('');
    this.editingDocument.set(document ?? null);
    if (document) {
      this.documentForm.reset({
        type: document.type,
        number: document.number ?? '',
        issuer: document.issuer ?? '',
        issueDate: document.issueDate ?? '',
        expiryDate: document.expiryDate,
        cost: document.cost ?? null,
        notes: document.notes ?? '',
        active: document.active,
      });
    } else {
      this.documentForm.reset({
        type: 'INSURANCE',
        number: '',
        issuer: '',
        issueDate: this.today(),
        expiryDate: this.oneYearFromNow(),
        cost: null,
        notes: '',
        active: true,
      });
    }
    this.documentModalOpen.set(true);
  }

  closeDocumentModal(): void {
    this.documentModalOpen.set(false);
  }

  submitDocument(): void {
    this.documentForm.markAllAsTouched();
    if (this.documentForm.invalid) {
      this.documentError.set('A document type and expiry date are required.');
      return;
    }
    const raw = this.documentForm.getRawValue();
    const payload = {
      type: raw.type,
      number: raw.number.trim() || null,
      issuer: raw.issuer.trim() || null,
      issueDate: raw.issueDate || null,
      expiryDate: raw.expiryDate,
      cost: raw.cost,
      notes: raw.notes.trim() || null,
      active: raw.active,
    };
    const editing = this.editingDocument();
    this.documentSaving.set(true);
    this.documentError.set('');
    const request$ = editing
      ? this.api.updateDocument(this.vehicleId, editing.id, payload)
      : this.api.addDocument(this.vehicleId, payload);
    request$.subscribe({
      next: () => {
        this.documentSaving.set(false);
        this.documentModalOpen.set(false);
        this.toast.success(editing ? 'Document updated.' : 'Document added.');
        this.load(false);
      },
      error: (error) => {
        this.documentSaving.set(false);
        this.documentError.set(apiErrorMessage(error, 'Unable to save this document.'));
      },
    });
  }

  // Reminders -----------------------------------------------------------------
  openReminderModal(): void {
    this.reminderError.set('');
    const mileage = this.vehicle()?.mileage ?? 0;
    this.reminderForm.reset({
      type: 'OIL_CHANGE',
      title: 'Oil change - every 10,000 km',
      nextDueMileage: mileage + 10_000,
      expiryDate: '',
      notes: '',
    });
    this.reminderModalOpen.set(true);
  }

  onReminderTypeChange(): void {
    const type = this.reminderForm.controls.type.value;
    const mileage = this.vehicle()?.mileage ?? 0;
    const defaults: Record<string, { title: string; km: number | null }> = {
      OIL_CHANGE: { title: 'Oil change - every 10,000 km', km: 10_000 },
      TIRE_ROTATION: { title: 'Tire rotation', km: 10_000 },
      BRAKE_CHECK: { title: 'Brake system check', km: 15_000 },
      BATTERY_CHECK: { title: 'Battery health check', km: 20_000 },
      TECHNICAL_INSPECTION: { title: 'Technical inspection renewal', km: null },
      INSURANCE_RENEWAL: { title: 'Insurance renewal', km: null },
      CUSTOM: { title: '', km: null },
    };
    const preset = defaults[type] ?? defaults['CUSTOM'];
    this.reminderForm.controls.title.setValue(preset.title);
    this.reminderForm.controls.nextDueMileage.setValue(preset.km ? mileage + preset.km : null);
  }

  closeReminderModal(): void {
    this.reminderModalOpen.set(false);
  }

  submitReminder(): void {
    this.reminderForm.markAllAsTouched();
    if (this.reminderForm.invalid) {
      this.reminderError.set('A title is required.');
      return;
    }
    const raw = this.reminderForm.getRawValue();
    if (!raw.nextDueMileage && !raw.expiryDate) {
      this.reminderError.set('A reminder needs a due mileage or an expiry date.');
      return;
    }
    this.reminderSaving.set(true);
    this.reminderError.set('');
    this.api
      .addReminder(this.vehicleId, {
        type: raw.type,
        title: raw.title.trim(),
        nextDueMileage: raw.nextDueMileage,
        expiryDate: raw.expiryDate || null,
        notes: raw.notes.trim() || null,
      })
      .subscribe({
        next: () => {
          this.reminderSaving.set(false);
          this.reminderModalOpen.set(false);
          this.toast.success('Reminder created.');
          this.load(false);
        },
        error: (error) => {
          this.reminderSaving.set(false);
          this.reminderError.set(apiErrorMessage(error, 'Unable to create this reminder.'));
        },
      });
  }

  updateReminder(reminder: ServiceReminder, status: ReminderStatus): void {
    this.api.updateReminderStatus(this.vehicleId, reminder.id, status).subscribe({
      next: () => {
        this.toast.success(`Reminder marked ${status.toLowerCase()}.`);
        this.load(false);
      },
      error: (error) => this.toast.error(apiErrorMessage(error, 'Unable to update this reminder.')),
    });
  }

  // Delete confirmation -------------------------------------------------------
  askDeleteRecord(recordId: number, label: string, date: string): void {
    this.confirmAction.set({
      kind: 'record',
      id: recordId,
      title: 'Delete maintenance record',
      message: `Remove the ${label} record from ${formatDate(date)}? This cannot be undone.`,
    });
  }

  askDeleteDocument(document: VehicleDocument): void {
    this.confirmAction.set({
      kind: 'document',
      id: document.id,
      title: 'Delete document',
      message: `Remove the ${DOCUMENT_TYPE_LABELS[document.type]} document${document.number ? ` (${document.number})` : ''}?`,
    });
  }

  askDeleteReminder(reminder: ServiceReminder): void {
    this.confirmAction.set({
      kind: 'reminder',
      id: reminder.id,
      title: 'Delete reminder',
      message: `Remove the reminder "${reminder.title}"?`,
    });
  }

  closeConfirm(): void {
    this.confirmAction.set(null);
  }

  confirmDelete(): void {
    const action = this.confirmAction();
    if (!action) {
      return;
    }
    this.confirmBusy.set(true);
    const request$ =
      action.kind === 'record'
        ? this.api.deleteRecord(this.vehicleId, action.id)
        : action.kind === 'document'
          ? this.api.deleteDocument(this.vehicleId, action.id)
          : this.api.deleteReminder(this.vehicleId, action.id);
    request$.subscribe({
      next: () => {
        this.confirmBusy.set(false);
        this.confirmAction.set(null);
        this.toast.success('Deleted successfully.');
        this.load(false);
      },
      error: (error) => {
        this.confirmBusy.set(false);
        this.toast.error(apiErrorMessage(error, 'Unable to delete this item.'));
      },
    });
  }

  askDeleteVehicle(): void {
    this.deleteVehicleOpen.set(true);
  }

  deleteVehicle(): void {
    this.deletingVehicle.set(true);
    this.api.deleteVehicle(this.vehicleId).subscribe({
      next: () => {
        this.deletingVehicle.set(false);
        this.toast.success('Vehicle passport deleted.');
        void this.router.navigate(['/vehicles']);
      },
      error: (error) => {
        this.deletingVehicle.set(false);
        this.toast.error(apiErrorMessage(error, 'Unable to delete this vehicle.'));
      },
    });
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private oneYearFromNow(): string {
    const date = new Date();
    date.setFullYear(date.getFullYear() + 1);
    return date.toISOString().slice(0, 10);
  }

  readonly formatDate = formatDate;
  readonly formatDateTime = formatDateTime;
  readonly formatKm = formatKm;
  readonly formatMoney = formatMoney;
  readonly daysText = daysText;
  readonly kmUntilText = kmUntilText;
  readonly gradeClass = gradeClass;
}
