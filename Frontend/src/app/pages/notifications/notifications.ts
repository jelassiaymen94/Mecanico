import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { apiErrorMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import { EVENT_LABELS, Notification, formatDateTime } from '../../core/models';
import { PassportApi } from '../../core/passport-api.service';
import { ToastService } from '../../core/toast.service';

type NotificationFilter = 'ALL' | 'DOCUMENT' | 'REMINDER';

@Component({
  selector: 'app-notifications',
  imports: [RouterLink],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss',
})
export class NotificationsComponent implements OnInit {
  private readonly api = inject(PassportApi);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);

  readonly notifications = signal<Notification[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly scanning = signal(false);
  readonly filter = signal<NotificationFilter>('ALL');

  readonly filtered = computed(() => {
    const filter = this.filter();
    const items = this.notifications();
    if (filter === 'ALL') {
      return items;
    }
    return items.filter((item) =>
      filter === 'DOCUMENT' ? item.eventType.startsWith('DOCUMENT') : item.eventType.startsWith('REMINDER'),
    );
  });

  readonly counters = computed(() => {
    const items = this.notifications();
    return {
      all: items.length,
      documents: items.filter((item) => item.eventType.startsWith('DOCUMENT')).length,
      reminders: items.filter((item) => item.eventType.startsWith('REMINDER')).length,
    };
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.notifications().subscribe({
      next: (items) => {
        this.notifications.set(items);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Unable to load notifications.'));
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
        this.load();
      },
      error: (error) => {
        this.scanning.set(false);
        this.toast.error(apiErrorMessage(error, 'The notification scan failed.'));
      },
    });
  }

  eventLabel(type: string): string {
    return EVENT_LABELS[type] ?? type;
  }

  eventIcon(type: string): string {
    switch (type) {
      case 'DOCUMENT_EXPIRING_30':
        return 'bi-file-earmark-text';
      case 'DOCUMENT_EXPIRING_7':
        return 'bi-file-earmark-excel';
      case 'REMINDER_DUE':
        return 'bi-bell';
      case 'REMINDER_OVERDUE':
        return 'bi-exclamation-octagon';
      default:
        return 'bi-info-circle';
    }
  }

  eventClass(type: string): string {
    if (type === 'REMINDER_OVERDUE') {
      return 'critical';
    }
    if (type.startsWith('DOCUMENT')) {
      return 'document';
    }
    return 'reminder';
  }

  readonly formatDateTime = formatDateTime;
}
