import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app" [class.app--auth]="auth.isLoggedIn()" [class.nav-open]="navOpen()">
      @if (auth.isLoggedIn()) {
        <aside class="sidebar" aria-label="Navegación principal">
          <div class="brand">
            <span class="brand__mark">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M21 8l-9-5-9 5 9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>
            </span>
            <span>Inven-Track</span>
          </div>

          <nav class="nav">
            <span class="nav-label">General</span>
            <a routerLink="/dashboard" routerLinkActive="active" (click)="navOpen.set(false)">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/></svg>
              Inicio
            </a>

            <span class="nav-label">Inventario</span>
            <a routerLink="/products" routerLinkActive="active" (click)="navOpen.set(false)">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M21 8l-9-5-9 5 9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>
              Productos
            </a>
            <a routerLink="/batches" routerLinkActive="active" (click)="navOpen.set(false)">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3l9 5-9 5-9-5 9-5z"/><path d="M3 13l9 5 9-5"/></svg>
              Lotes
            </a>
            <a routerLink="/suppliers" routerLinkActive="active" (click)="navOpen.set(false)">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h11v10H3z"/><path d="M14 10h4l3 3v3h-7z"/><circle cx="7" cy="18" r="2"/><circle cx="17" cy="18" r="2"/></svg>
              Proveedores
            </a>

            <span class="nav-label">Movimientos</span>
            <a routerLink="/entries" routerLinkActive="active" (click)="navOpen.set(false)">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3v12"/><path d="M7 11l5 5 5-5"/><path d="M4 21h16"/></svg>
              Entradas
            </a>
            <a routerLink="/exits" routerLinkActive="active" (click)="navOpen.set(false)">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 17V5"/><path d="M7 9l5-5 5 5"/><path d="M4 21h16"/></svg>
              Salidas
            </a>
          </nav>

          <div class="sidebar__foot">
            <div class="usercard">
              <span class="avatar" aria-hidden="true">{{ initials() }}</span>
              <div class="usercard__text">
                <strong>{{ displayName() }}</strong>
                <span>{{ roleLabel() }}</span>
              </div>
            </div>
            <button type="button" class="btn btn--sidebar" (click)="logout()">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></svg>
              Cerrar sesión
            </button>
          </div>
        </aside>
        <div class="scrim" (click)="navOpen.set(false)"></div>
      }

      <div class="content">
        @if (auth.isLoggedIn()) {
          <header class="topbar">
            <button type="button" class="icon-btn menu-btn" aria-label="Abrir menú" (click)="navOpen.set(true)">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 6h16M4 12h16M4 18h16"/></svg>
            </button>
            <span class="topbar__date">{{ today }}</span>
          </header>
        }
        <main class="main" [class.main--bare]="!auth.isLoggedIn()">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class Shell {
  auth = inject(AuthService);
  private router = inject(Router);

  navOpen = signal(false);
  today = new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).format(new Date());

  displayName = computed(() => (this.auth.user()?.fullName ?? '').replace(/\(.*?\)/g, '').trim());
  roleLabel = computed(() => (this.auth.user()?.role === 'DUENO' ? 'Dueño' : 'Empleada'));
  initials = computed(() =>
    this.displayName()
      .split(/\s+/)
      .filter((w) => w.length > 2)
      .slice(0, 2)
      .map((w) => w[0].toUpperCase())
      .join(''),
  );

  logout() {
    this.navOpen.set(false);
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}