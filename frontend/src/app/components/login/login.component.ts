import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { User } from '../../core/models';

/**
 * The entry screen. Asks for a display name (colour is optional — the server
 * assigns one if left blank) and emits the created user once login succeeds.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly api = inject(ApiService);

  @Output() loggedIn = new EventEmitter<User>();

  name = '';
  color = '';
  readonly busy = signal(false);
  readonly error = signal('');

  readonly swatches = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#06b6d4', '#6366f1', '#a855f7', '#ec4899'];

  pick(c: string): void {
    this.color = this.color === c ? '' : c;
  }

  submit(): void {
    const name = this.name.trim();
    if (!name || this.busy()) return;

    this.busy.set(true);
    this.error.set('');
    this.api.login(name, this.color || undefined).subscribe({
      next: (user) => this.loggedIn.emit(user),
      error: () => {
        this.error.set('Could not connect to the server. Is the backend running?');
        this.busy.set(false);
      },
    });
  }
}
