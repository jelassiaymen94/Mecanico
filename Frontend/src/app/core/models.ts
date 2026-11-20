export type UserRole = 'ADMIN' | 'DRIVER' | 'FLEET_MANAGER' | 'PROVIDER';

export type VehicleStatus = 'ACTIVE' | 'IN_REPAIR' | 'SOLD' | 'STOLEN' | 'ARCHIVED';
export type FuelType = 'PETROL' | 'DIESEL' | 'HYBRID' | 'ELECTRIC' | 'LPG' | 'CNG';
export type TransmissionType = 'MANUAL' | 'AUTOMATIC' | 'SEMI_AUTOMATIC';
export type VehicleType =
  | 'CAR'
  | 'SEDAN'
  | 'HATCHBACK'
  | 'SUV'
  | 'COUPE'
  | 'WAGON'
  | 'VAN'
  | 'PICKUP'
  | 'TRUCK'
  | 'MOTORCYCLE'
  | 'OTHER';
export type BodyType =
  | 'SEDAN'
  | 'HATCHBACK'
  | 'SUV'
  | 'COUPE'
  | 'WAGON'
  | 'PICKUP'
  | 'VAN'
  | 'TRUCK'
  | 'MOTORCYCLE'
  | 'OTHER';
export type RecordType = 'SERVICE' | 'REPAIR' | 'INSPECTION' | 'PART_REPLACED';
export type DocumentType = 'INSURANCE' | 'INSPECTION' | 'REGISTRATION' | 'VIGNETTE';
export type ReminderType =
  | 'OIL_CHANGE'
  | 'TIRE_ROTATION'
  | 'BRAKE_CHECK'
  | 'BATTERY_CHECK'
  | 'TECHNICAL_INSPECTION'
  | 'INSURANCE_RENEWAL'
  | 'CUSTOM';
export type ReminderStatus = 'PENDING' | 'DUE' | 'OVERDUE' | 'COMPLETED' | 'DISMISSED';
export type HealthGrade = 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR';

export interface SessionUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: UserRole;
  status: string;
  avatarUrl?: string;
  companyName?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: SessionUser;
}

export interface InternalUser {
  id: number;
  email: string;
  fullName: string;
  role: string;
  status: string;
}

export interface VehicleBrand {
  id: number;
  name: string;
  slug: string;
  country?: string;
  logoUrl?: string;
  modelCount: number;
}

export interface VehicleModel {
  id: number;
  brandId: number;
  brandName: string;
  name: string;
  bodyType: BodyType;
  yearFrom?: number;
  yearTo?: number;
}

export interface Vehicle {
  id: number;
  ownerId: number;
  brandId: number;
  brandName: string;
  modelId: number;
  modelName: string;
  vin: string;
  plate: string;
  plateCountry?: string;
  year: number;
  fuelType: FuelType;
  mileage: number;
  registrationDate?: string;
  color?: string;
  nickname?: string;
  vehicleType: VehicleType;
  transmission?: TransmissionType;
  engineCapacityCc?: number;
  powerHp?: number;
  photoUrl?: string;
  notes?: string;
  status: VehicleStatus;
  createdAt: string;
  updatedAt: string;
  healthScore: number;
  healthGrade: HealthGrade;
}

export interface VehiclePayload {
  ownerId?: number | null;
  brandId: number;
  modelId: number;
  vin: string;
  plate: string;
  plateCountry?: string;
  year: number;
  fuelType: FuelType;
  mileage: number;
  registrationDate?: string | null;
  color?: string | null;
  nickname?: string | null;
  vehicleType: VehicleType;
  transmission?: TransmissionType | null;
  engineCapacityCc?: number | null;
  powerHp?: number | null;
  photoUrl?: string | null;
  notes?: string | null;
  status: VehicleStatus;
}

export interface MaintenanceRecord {
  id: number;
  vehicleId: number;
  type: RecordType;
  serviceDate: string;
  mileage: number;
  description?: string;
  providerId?: number;
  providerName?: string;
  requestId?: number;
  cost?: number;
  currency?: string;
  laborCost?: number;
  partsCost?: number;
  partName?: string;
  partReference?: string;
  invoiceUrl?: string;
  nextDueDate?: string;
  nextDueMileage?: number;
  createdAt: string;
}

export interface MaintenanceRecordPayload {
  type: RecordType;
  serviceDate: string;
  mileage: number;
  description?: string | null;
  providerId?: number | null;
  providerName?: string | null;
  cost?: number | null;
  currency?: string | null;
  laborCost?: number | null;
  partsCost?: number | null;
  partName?: string | null;
  partReference?: string | null;
  invoiceUrl?: string | null;
  nextDueDate?: string | null;
  nextDueMileage?: number | null;
}

export interface VehicleDocument {
  id: number;
  vehicleId: number;
  type: DocumentType;
  number?: string;
  issuer?: string;
  issueDate?: string;
  expiryDate: string;
  fileUrl?: string;
  cost?: number;
  notes?: string;
  notification30Sent: boolean;
  notification7Sent: boolean;
  active: boolean;
  daysUntilExpiry: number;
  expired: boolean;
  expiringSoon: boolean;
  createdAt: string;
}

export interface VehicleDocumentPayload {
  type: DocumentType;
  number?: string | null;
  issuer?: string | null;
  issueDate?: string | null;
  expiryDate: string;
  fileUrl?: string | null;
  cost?: number | null;
  notes?: string | null;
  active?: boolean;
}

export interface ServiceReminder {
  id: number;
  vehicleId: number;
  type: ReminderType;
  title: string;
  mileageTrigger?: number;
  nextDueMileage?: number;
  expiryDate?: string;
  status: ReminderStatus;
  storedStatus: ReminderStatus;
  notified: boolean;
  lastNotifiedAt?: string;
  notes?: string;
  completedAt?: string;
  createdAt: string;
  kmUntilDue?: number;
  daysUntilExpiry?: number;
  due: boolean;
  overdue: boolean;
}

export interface ServiceReminderPayload {
  type: ReminderType;
  title: string;
  mileageTrigger?: number | null;
  nextDueMileage?: number | null;
  expiryDate?: string | null;
  notes?: string | null;
}

export interface HealthFactor {
  code: string;
  label: string;
  status: string;
  detail: string;
  penalty: number;
}

export interface HealthScore {
  score: number;
  grade: HealthGrade;
  summary: string;
  breakdown: Record<string, number>;
  factors: HealthFactor[];
}

export interface PassportStats {
  recordCount: number;
  totalMaintenanceCost: number;
  documentsTotal: number;
  documentsExpiring: number;
  documentsExpired: number;
  remindersDue: number;
  remindersOverdue: number;
  lastServiceDate?: string;
  currentMileage: number;
}

export interface VehiclePassport {
  vehicle: Vehicle;
  records: MaintenanceRecord[];
  documents: VehicleDocument[];
  reminders: ServiceReminder[];
  health: HealthScore;
  stats: PassportStats;
}

export interface DashboardAlert {
  type: string;
  vehicleId: number;
  vehicleLabel: string;
  title: string;
  message: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO' | string;
  dueDate?: string;
  mileage?: number;
}

export interface DashboardSummary {
  totalVehicles: number;
  activeVehicles: number;
  documentsExpiring: number;
  documentsExpired: number;
  remindersDue: number;
  remindersOverdue: number;
  totalMaintenanceCost: number;
  averageHealthScore: number;
  vehicles: Vehicle[];
  alerts: DashboardAlert[];
}

export interface ScanResult {
  documentsNotified: number;
  remindersNotified: number;
  scannedAt: string;
}

export interface Notification {
  id: number;
  vehicleId?: number;
  documentId?: number;
  reminderId?: number;
  eventType: string;
  message: string;
  channel: string;
  status: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface VehicleFilters {
  search?: string;
  status?: VehicleStatus | '';
  ownerId?: number | null;
  page?: number;
  size?: number;
}

export const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Administrator',
  DRIVER: 'Driver',
  FLEET_MANAGER: 'Fleet manager',
  PROVIDER: 'Service provider',
};

export const FUEL_LABELS: Record<FuelType, string> = {
  PETROL: 'Petrol',
  DIESEL: 'Diesel',
  HYBRID: 'Hybrid',
  ELECTRIC: 'Electric',
  LPG: 'LPG',
  CNG: 'CNG',
};

export const TRANSMISSION_LABELS: Record<TransmissionType, string> = {
  MANUAL: 'Manual',
  AUTOMATIC: 'Automatic',
  SEMI_AUTOMATIC: 'Semi-automatic',
};

export const VEHICLE_STATUS_LABELS: Record<VehicleStatus, string> = {
  ACTIVE: 'Active',
  IN_REPAIR: 'In repair',
  SOLD: 'Sold',
  STOLEN: 'Stolen',
  ARCHIVED: 'Archived',
};

export const VEHICLE_TYPE_LABELS: Record<VehicleType, string> = {
  CAR: 'Car',
  SEDAN: 'Sedan',
  HATCHBACK: 'Hatchback',
  SUV: 'SUV',
  COUPE: 'Coupe',
  WAGON: 'Wagon',
  VAN: 'Van',
  PICKUP: 'Pickup',
  TRUCK: 'Truck',
  MOTORCYCLE: 'Motorcycle',
  OTHER: 'Other',
};

export const RECORD_TYPE_LABELS: Record<RecordType, string> = {
  SERVICE: 'Service',
  REPAIR: 'Repair',
  INSPECTION: 'Inspection',
  PART_REPLACED: 'Part replaced',
};

export const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  INSURANCE: 'Insurance',
  INSPECTION: 'Technical inspection',
  REGISTRATION: 'Registration',
  VIGNETTE: 'Vignette',
};

export const REMINDER_TYPE_LABELS: Record<ReminderType, string> = {
  OIL_CHANGE: 'Oil change',
  TIRE_ROTATION: 'Tire rotation',
  BRAKE_CHECK: 'Brake check',
  BATTERY_CHECK: 'Battery check',
  TECHNICAL_INSPECTION: 'Technical inspection',
  INSURANCE_RENEWAL: 'Insurance renewal',
  CUSTOM: 'Custom reminder',
};

export const REMINDER_STATUS_LABELS: Record<ReminderStatus, string> = {
  PENDING: 'Pending',
  DUE: 'Due soon',
  OVERDUE: 'Overdue',
  COMPLETED: 'Completed',
  DISMISSED: 'Dismissed',
};

export const EVENT_LABELS: Record<string, string> = {
  DOCUMENT_EXPIRING_30: 'Document expires within 30 days',
  DOCUMENT_EXPIRING_7: 'Document expires within 7 days',
  REMINDER_DUE: 'Service reminder due',
  REMINDER_OVERDUE: 'Service reminder overdue',
};

export const FUEL_TYPES: FuelType[] = ['PETROL', 'DIESEL', 'HYBRID', 'ELECTRIC', 'LPG', 'CNG'];
export const TRANSMISSION_TYPES: TransmissionType[] = ['MANUAL', 'AUTOMATIC', 'SEMI_AUTOMATIC'];
export const VEHICLE_TYPES: VehicleType[] = [
  'CAR',
  'SEDAN',
  'HATCHBACK',
  'SUV',
  'COUPE',
  'WAGON',
  'VAN',
  'PICKUP',
  'TRUCK',
  'MOTORCYCLE',
  'OTHER',
];
export const VEHICLE_STATUSES: VehicleStatus[] = ['ACTIVE', 'IN_REPAIR', 'SOLD', 'STOLEN', 'ARCHIVED'];
export const RECORD_TYPES: RecordType[] = ['SERVICE', 'REPAIR', 'INSPECTION', 'PART_REPLACED'];
export const DOCUMENT_TYPES: DocumentType[] = ['INSURANCE', 'INSPECTION', 'REGISTRATION', 'VIGNETTE'];
export const REMINDER_TYPES: ReminderType[] = [
  'OIL_CHANGE',
  'TIRE_ROTATION',
  'BRAKE_CHECK',
  'BATTERY_CHECK',
  'TECHNICAL_INSPECTION',
  'INSURANCE_RENEWAL',
  'CUSTOM',
];

export function initialsOf(user: { firstName?: string; lastName?: string } | null | undefined): string {
  if (!user) {
    return 'MC';
  }
  return `${(user.firstName ?? '').charAt(0)}${(user.lastName ?? '').charAt(0)}`.toUpperCase() || 'MC';
}

export function formatDate(value?: string | null): string {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '—';
  }
  return new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date);
}

export function formatDateTime(value?: string | null): string {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '—';
  }
  return new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export function formatMoney(value?: number | string | null, currency = 'TND'): string {
  if (value === null || value === undefined || value === '') {
    return `0.00 ${currency}`;
  }
  const amount = typeof value === 'string' ? Number(value) : value;
  if (Number.isNaN(amount)) {
    return `0.00 ${currency}`;
  }
  return `${new Intl.NumberFormat('en-GB', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)} ${currency}`;
}

export function formatKm(value?: number | null): string {
  if (value === null || value === undefined) {
    return '—';
  }
  return `${new Intl.NumberFormat('en-GB').format(value)} km`;
}

export function gradeClass(grade?: string | null): string {
  return (grade ?? 'POOR').toLowerCase();
}

export function statusClass(status?: string | null): string {
  return (status ?? 'ARCHIVED').toLowerCase().replace(/_/g, '-');
}

export function daysText(days?: number | null): string {
  if (days === null || days === undefined) {
    return '—';
  }
  if (days < 0) {
    return `${Math.abs(days)} day(s) ago`;
  }
  if (days === 0) {
    return 'Today';
  }
  return `in ${days} day(s)`;
}

export function kmUntilText(km?: number | null): string {
  if (km === null || km === undefined) {
    return '—';
  }
  if (km < 0) {
    return `${new Intl.NumberFormat('en-GB').format(Math.abs(km))} km overdue`;
  }
  return `${new Intl.NumberFormat('en-GB').format(km)} km left`;
}
