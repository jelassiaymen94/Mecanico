import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { apiErrorMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';

interface DemoAccount {
  label: string;
  email: string;
  role: string;
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly error = signal('');
  readonly showPassword = signal(false);
  readonly usersPortalUrl = environment.usersPortalUrl;

  readonly demoAccounts: DemoAccount[] = [
    { label: 'Amine Ben Salah', email: 'admin@mecanico.tn', role: 'Admin' },
    { label: 'Yassine Trabelsi', email: 'driver@mecanico.tn', role: 'Driver' },
    { label: 'Sami Bouazizi', email: 'driver2@mecanico.tn', role: 'Driver' },
    { label: 'Nizar Gharbi', email: 'fleet@mecanico.tn', role: 'Fleet manager' },
  ];

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set('');
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        void this.router.navigateByUrl(returnUrl && returnUrl.startsWith('/') ? returnUrl : '/dashboard');
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Unable to sign in with these credentials.'));
      },
    });
  }

  useDemo(account: DemoAccount): void {
    this.form.setValue({ email: account.email, password: 'Password123!' });
    this.error.set('');
  }
}
