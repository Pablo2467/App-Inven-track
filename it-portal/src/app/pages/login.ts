import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { apiError } from '../core/api-error';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  template: `
    <div class="login-shell">
      <section class="login-brand">
        <div class="brand">
          <span class="brand__mark">
            <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M21 8l-9-5-9 5 9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>
          </span>
          <span>Inven-Track</span>
        </div>
        <div>
          <p class="eyebrow">Tu salsamentaria, en orden</p>
          <h1>Cada lote,<br />bajo control.</h1>
          <p class="lead">Menos cuentas en papel. Más tranquilidad para cuidar cada producto de tu tienda.</p>
          <ul class="ticks">
            <li>Consulta lo que tienes</li>
            <li>Revisa qué vence primero</li>
            <li>Registra sin complicaciones</li>
          </ul>
        </div>
        <p class="login-brand__foot">Inventario para embutidos, quesos y otros perecederos.</p>
      </section>

      <section class="login-side">
        <form class="login-card" (ngSubmit)="submit()">
          <h2>Qué bueno verte de nuevo</h2>
          <p class="muted">Ingresa con tu cuenta para revisar el inventario de tu tienda.</p>

          @if (error()) {
            <div class="alert alert--error" role="alert">{{ error() }}</div>
          }

          <div class="field">
            <label class="field__label" for="username">Usuario</label>
            <input id="username" name="username" autocomplete="username" [(ngModel)]="username" />
          </div>

          <div class="field">
            <label class="field__label" for="password">Contraseña</label>
            <div class="input-group">
              <input id="password" name="password" autocomplete="current-password"
                     [type]="show() ? 'text' : 'password'" [(ngModel)]="password" />
              <button type="button" class="input-group__btn" (click)="show.set(!show())">
                {{ show() ? 'Ocultar' : 'Mostrar' }}
              </button>
            </div>
          </div>

          <button type="submit" class="btn btn--primary btn--block" [disabled]="loading()">
            {{ loading() ? 'Ingresando…' : 'Ingresar' }}
          </button>
          <p class="hint">¿Necesitas acceso? Habla con tu administrador.</p>
        </form>
      </section>
    </div>
  `,
})
export class LoginPage {
  private auth = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  show = signal(false);
  error = signal('');
  loading = signal(false);

  submit() {
    this.error.set('');
    if (!this.username || !this.password) {
      this.error.set('Escribe tu usuario y tu contraseña.');
      return;
    }
    this.loading.set(true);
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (e) => {
        this.error.set(apiError(e));
        this.loading.set(false);
      },
    });
  }
}