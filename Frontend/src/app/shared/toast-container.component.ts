import { Component, inject } from '@angular/core';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-toast-container',
  template: `
    <div class="toast-stack" aria-live="polite" aria-atomic="true">
      @for (toast of toasts.messages(); track toast.id) {
        <div class="app-toast" [class]="'app-toast app-toast--' + toast.kind" role="status">
          <i
            class="bi"
            [class.bi-check-circle-fill]="toast.kind === 'success'"
            [class.bi-exclamation-triangle-fill]="toast.kind === 'warning'"
            [class.bi-x-circle-fill]="toast.kind === 'danger'"
            [class.bi-info-circle-fill]="toast.kind === 'info'"
          ></i>
          <span>{{ toast.text }}</span>
          <button type="button" class="btn-close" aria-label="Dismiss" (click)="toasts.dismiss(toast.id)"></button>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .toast-stack {
        position: fixed;
        top: 1.25rem;
        right: 1.25rem;
        z-index: 2000;
        display: flex;
        flex-direction: column;
        gap: 0.65rem;
        width: min(24rem, calc(100vw - 2rem));
      }
      .app-toast {
        display: flex;
        align-items: flex-start;
        gap: 0.65rem;
        padding: 0.85rem 1rem;
        border-radius: 0.85rem;
        background: #ffffff;
        box-shadow: 0 18px 45px rgba(15, 23, 42, 0.18);
        border-left: 4px solid #0d9488;
        font-size: 0.9rem;
        color: #0f172a;
      }
      .app-toast span {
        flex: 1;
      }
      .app-toast .bi {
        font-size: 1.05rem;
        margin-top: 0.05rem;
      }
      .app-toast--success {
        border-left-color: #10b981;
      }
      .app-toast--success .bi {
        color: #10b981;
      }
      .app-toast--danger {
        border-left-color: #ef4444;
      }
      .app-toast--danger .bi {
        color: #ef4444;
      }
      .app-toast--warning {
        border-left-color: #f59e0b;
      }
      .app-toast--warning .bi {
        color: #f59e0b;
      }
      .app-toast--info {
        border-left-color: #0d9488;
      }
      .app-toast--info .bi {
        color: #0d9488;
      }
      .btn-close {
        font-size: 0.7rem;
      }
    `,
  ],
})
export class ToastContainerComponent {
  readonly toasts = inject(ToastService);
}
