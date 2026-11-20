import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  DashboardSummary,
  HealthScore,
  MaintenanceRecord,
  MaintenanceRecordPayload,
  Notification,
  PageResponse,
  ReminderStatus,
  ScanResult,
  ServiceReminder,
  ServiceReminderPayload,
  Vehicle,
  VehicleBrand,
  VehicleDocument,
  VehicleDocumentPayload,
  VehicleFilters,
  VehicleModel,
  VehiclePassport,
  VehiclePayload,
} from './models';

@Injectable({ providedIn: 'root' })
export class PassportApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  // Dashboard -----------------------------------------------------------------
  summary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.base}/dashboard/summary`);
  }

  notifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/notifications`);
  }

  scan(): Observable<ScanResult> {
    return this.http.post<ScanResult>(`${this.base}/admin/scan`, {});
  }

  // Catalogue -----------------------------------------------------------------
  brands(): Observable<VehicleBrand[]> {
    return this.http.get<VehicleBrand[]>(`${this.base}/catalog/brands`);
  }

  models(brandId: number): Observable<VehicleModel[]> {
    return this.http.get<VehicleModel[]>(`${this.base}/catalog/brands/${brandId}/models`);
  }

  // Vehicles ------------------------------------------------------------------
  vehicles(filters: VehicleFilters): Observable<PageResponse<Vehicle>> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 10));
    if (filters.search?.trim()) {
      params = params.set('search', filters.search.trim());
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.ownerId !== null && filters.ownerId !== undefined) {
      params = params.set('ownerId', String(filters.ownerId));
    }
    return this.http.get<PageResponse<Vehicle>>(`${this.base}/vehicles`, { params });
  }

  vehicle(id: number): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.base}/vehicles/${id}`);
  }

  passport(id: number): Observable<VehiclePassport> {
    return this.http.get<VehiclePassport>(`${this.base}/vehicles/${id}/passport`);
  }

  health(id: number): Observable<HealthScore> {
    return this.http.get<HealthScore>(`${this.base}/vehicles/${id}/health`);
  }

  createVehicle(payload: VehiclePayload): Observable<Vehicle> {
    return this.http.post<Vehicle>(`${this.base}/vehicles`, payload);
  }

  updateVehicle(id: number, payload: VehiclePayload): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.base}/vehicles/${id}`, payload);
  }

  deleteVehicle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/vehicles/${id}`);
  }

  updateMileage(id: number, mileage: number): Observable<Vehicle> {
    return this.http.patch<Vehicle>(`${this.base}/vehicles/${id}/mileage`, { mileage });
  }

  // Maintenance records -------------------------------------------------------
  records(vehicleId: number): Observable<MaintenanceRecord[]> {
    return this.http.get<MaintenanceRecord[]>(`${this.base}/vehicles/${vehicleId}/records`);
  }

  addRecord(vehicleId: number, payload: MaintenanceRecordPayload): Observable<MaintenanceRecord> {
    return this.http.post<MaintenanceRecord>(`${this.base}/vehicles/${vehicleId}/records`, payload);
  }

  deleteRecord(vehicleId: number, recordId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/vehicles/${vehicleId}/records/${recordId}`);
  }

  // Documents -----------------------------------------------------------------
  documents(vehicleId: number): Observable<VehicleDocument[]> {
    return this.http.get<VehicleDocument[]>(`${this.base}/vehicles/${vehicleId}/documents`);
  }

  addDocument(vehicleId: number, payload: VehicleDocumentPayload): Observable<VehicleDocument> {
    return this.http.post<VehicleDocument>(`${this.base}/vehicles/${vehicleId}/documents`, payload);
  }

  updateDocument(
    vehicleId: number,
    documentId: number,
    payload: VehicleDocumentPayload,
  ): Observable<VehicleDocument> {
    return this.http.put<VehicleDocument>(
      `${this.base}/vehicles/${vehicleId}/documents/${documentId}`,
      payload,
    );
  }

  deleteDocument(vehicleId: number, documentId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/vehicles/${vehicleId}/documents/${documentId}`);
  }

  // Reminders -----------------------------------------------------------------
  reminders(vehicleId: number): Observable<ServiceReminder[]> {
    return this.http.get<ServiceReminder[]>(`${this.base}/vehicles/${vehicleId}/reminders`);
  }

  addReminder(vehicleId: number, payload: ServiceReminderPayload): Observable<ServiceReminder> {
    return this.http.post<ServiceReminder>(`${this.base}/vehicles/${vehicleId}/reminders`, payload);
  }

  updateReminderStatus(
    vehicleId: number,
    reminderId: number,
    status: ReminderStatus,
  ): Observable<ServiceReminder> {
    return this.http.patch<ServiceReminder>(
      `${this.base}/vehicles/${vehicleId}/reminders/${reminderId}/status`,
      { status },
    );
  }

  deleteReminder(vehicleId: number, reminderId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/vehicles/${vehicleId}/reminders/${reminderId}`);
  }
}
