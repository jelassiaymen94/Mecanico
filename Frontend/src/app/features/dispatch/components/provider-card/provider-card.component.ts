import { DecimalPipe } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { DispatchProvider } from '../../models/dispatch.models';

@Component({
  selector: 'app-provider-card',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './provider-card.component.html',
  styleUrl: './provider-card.component.css',
})
export class ProviderCardComponent {
  readonly provider = input.required<DispatchProvider>();
  readonly selected = input(false);
  readonly select = output<DispatchProvider>();

  readonly initials = computed(() =>
    this.provider()
      .name.split(' ')
      .map((part) => part.charAt(0))
      .slice(0, 2)
      .join('')
      .toUpperCase(),
  );
}
