import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../services/auth.service';

/**
 * Login screen (UI-1, FR-2, BR-3). Card-centered layout over the desktop gradient.
 * The desktop "Default credentials" hint is intentionally omitted (SEC-1).
 */
@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error.set(null);

    // BR-3 r1/2 — client-side blank check (server re-validates).
    if (!this.username.trim() || !this.password) {
      this.error.set('Please enter both username and password.');
      return;
    }

    this.submitting.set(true);
    this.auth.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/search']);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(
          err.error?.detail ?? 'Invalid username or password. Please try again.'
        );
        // BR-3 r6 — clear password on failure.
        this.password = '';
      }
    });
  }
}
