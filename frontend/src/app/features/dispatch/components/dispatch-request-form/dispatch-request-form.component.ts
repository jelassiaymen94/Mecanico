import { Component, inject, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  DispatchRequest,
  PICKUP_POINTS,
  SERVICE_TYPE_OPTIONS,
  ServiceType,
} from '../../models/dispatch.models';

@Component({
  selector: 'app-dispatch-request-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './dispatch-request-form.component.html',
  styleUrl: './dispatch-request-form.component.css',
})
export class DispatchRequestFormComponent {
  private readonly formBuilder = inject(FormBuilder);

  readonly serviceTypeOptions = SERVICE_TYPE_OPTIONS;
  readonly pickupPoints = PICKUP_POINTS;
  readonly requestSubmitted = output<DispatchRequest>();

  readonly form = this.formBuilder.nonNullable.group({
    serviceType: ['TOWING' as ServiceType, [Validators.required]],
    pickupPointId: [this.pickupPoints[0].id, [Validators.required]],
    destination: [''],
    vehicle: ['', [Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const pickup = this.pickupPoints.find((point) => point.id === value.pickupPointId);

    if (!pickup) {
      return;
    }

    this.requestSubmitted.emit({
      serviceType: value.serviceType,
      pickup,
      destination: value.destination.trim(),
      vehicle: value.vehicle.trim(),
      description: value.description.trim(),
    });
  }

  isInvalid(controlName: 'destination' | 'vehicle' | 'description'): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.touched || control.dirty);
  }
}
