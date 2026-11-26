import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../core/auth.service';
import { ROLE_LABELS } from '../core/models';

interface NavItem {
  label: string;
  icon: string;
  path: string;
}

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly mobileOpen = signal(false);
  readonly pageTitle = signal('Dashboard');
  readonly usersPortalUrl = environment.usersPortalUrl;
  readonly roleLabel = computed(() => {
    const role = this.auth.user()?.role;
    return role ? (ROLE_LABELS[role] ?? role) : '';
  });

  readonly navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'bi-grid-1x2-fill', path: '/dashboard' },
    { label: 'Vehicles', icon: 'bi-car-front-fill', path: '/vehicles' },
    { label: 'Notifications', icon: 'bi-bell-fill', path: '/notifications' },
  ];

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((event) => {
        this.pageTitle.set(this.titleFor(event.urlAfterRedirects));
        this.mobileOpen.set(false);
      });
  }

  toggleSidebar(): void {
    this.mobileOpen.update((open) => !open);
  }

  closeSidebar(): void {
    this.mobileOpen.set(false);
  }

  logout(): void {
    this.auth.logout();
  }

  private titleFor(url: string): string {
    if (/^\/vehicles\/new/.test(url)) {
      return 'Register a vehicle';
    }
    if (/^\/vehicles\/\d+\/edit/.test(url)) {
      return 'Edit vehicle';
    }
    if (/^\/vehicles\/\d+/.test(url)) {
      return 'Vehicle passport';
    }
    if (url.startsWith('/vehicles')) {
      return 'My vehicles';
    }
    if (url.startsWith('/notifications')) {
      return 'Notifications';
    }
    return 'Dashboard';
  }
}
