import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'danger' | 'warning' | 'info';

export interface ToastMessage {
  id: number;
  kind: ToastKind;
  text: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private sequence = 0;
  readonly messages = signal<ToastMessage[]>([]);

  success(text: string): void {
    this.show(text, 'success');
  }

  error(text: string): void {
    this.show(text, 'danger');
  }

  info(text: string): void {
    this.show(text, 'info');
  }

  warning(text: string): void {
    this.show(text, 'warning');
  }

  dismiss(id: number): void {
    this.messages.update((items) => items.filter((item) => item.id !== id));
  }

  private show(text: string, kind: ToastKind): void {
    const id = ++this.sequence;
    this.messages.update((items) => [...items, { id, kind, text }]);
    window.setTimeout(() => this.dismiss(id), 4500);
  }
}
