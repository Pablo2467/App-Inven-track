import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API_URL } from '../core/config';
import { apiError } from '../core/api-error';
import { Badge, badgeFor, fmtDate, fmtDateTime, fmtNumber } from '../core/format';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <h1 class="page__title">Inicio</h1>
          <p class="page__sub">Mira qué tienes disponible y qué necesita atención hoy.</p>
        </div>
        <div class="page__actions">
          <a class="btn btn--secondary" routerLink="/exits" [queryParams]="{ nuevo: 1 }">Registrar salida</a>
          <a class="btn btn--primary" routerLink="/entries" [queryParams]="{ nuevo: 1 }">Registrar entrada</a>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--error" role="alert">
          <span>{{ error() }}</span>
          <button type="button" class="btn btn--secondary" (click)="load()">Reintentar</button>
        </div>
      }

      @if (loading()) {
        <div class="kpis">
          @for (i of placeholders; track i) {
            <div class="kpi"><div class="skeleton" style="width: 40%"></div><div class="skeleton" style="height: 32px; width: 60%"></div></div>
          }
        </div>
      }

      @if (data(); as d) {
        <div class="kpis">
          <div class="kpi">
            <span class="kpi__icon"><svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M21 8l-9-5-9 5 9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg></span>
            <span class="kpi__label">Productos</span>
            <span class="kpi__value">{{ fmtNumber(d.totalProducts) }}</span>
            <span class="kpi__hint">en tu catálogo</span>
          </div>
          <div class="kpi">
            <span class="kpi__icon"><svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3l9 5-9 5-9-5 9-5z"/><path d="M3 13l9 5 9-5"/></svg></span>
            <span class="kpi__label">Unidades disponibles</span>
            <span class="kpi__value">{{ fmtNumber(d.availableUnits) }}</span>
            <span class="kpi__hint">sumando todos los lotes</span>
          </div>
          <div class="kpi kpi--warn">
            <span class="kpi__icon"><svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg></span>
            <span class="kpi__label">Lotes por vencer</span>
            <span class="kpi__value">{{ fmtNumber(d.expiringLots) }}</span>
            <span class="kpi__hint">prioriza su salida</span>
          </div>
          <div class="kpi kpi--danger">
            <span class="kpi__icon"><svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3l10 18H2L12 3z"/><path d="M12 10v5"/><path d="M12 18h.01"/></svg></span>
            <span class="kpi__label">Lotes vencidos</span>
            <span class="kpi__value">{{ fmtNumber(d.expiredLots) }}</span>
            <span class="kpi__hint">no se pueden vender</span>
          </div>
        </div>

        <div class="grid-2">
          <div class="panel">
            <div class="panel__head">
              <h2 class="panel__title">Para revisar hoy</h2>
              <span class="muted">{{ d.alerts.length }} alertas</span>
            </div>
            <ul class="list">
              @for (a of d.alerts; track $index) {
                <li class="list__row">
                  <span class="badge" [class]="'badge--' + alertBadge(a).tone">{{ alertBadge(a).label }}</span>
                  <div class="list__main">
                    <strong>{{ a.productName }}</strong>
                    <span class="muted">{{ a.message }}</span>
                  </div>
                  @if (a.expirationDate) {
                    <span class="muted nowrap">{{ fmtDate(a.expirationDate) }}</span>
                  }
                </li>
              } @empty {
                <li class="list__row"><span class="muted">Todo en orden: no hay alertas por ahora.</span></li>
              }
            </ul>
          </div>

          <div class="panel">
            <div class="panel__head"><h2 class="panel__title">Salud del inventario</h2></div>
            <div class="panel__body">
              <div class="health" role="img" [attr.aria-label]="'Productos en orden ' + d.greenProducts + ', por revisar ' + d.yellowProducts + ', críticos ' + d.redProducts">
                <span class="health__seg health__seg--ok" [style.flex-grow]="d.greenProducts"></span>
                <span class="health__seg health__seg--warn" [style.flex-grow]="d.yellowProducts"></span>
                <span class="health__seg health__seg--danger" [style.flex-grow]="d.redProducts"></span>
              </div>
              <ul class="legend">
                <li><span class="badge badge--ok">En orden</span><strong>{{ d.greenProducts }}</strong></li>
                <li><span class="badge badge--warn">Por revisar</span><strong>{{ d.yellowProducts }}</strong></li>
                <li><span class="badge badge--danger">Críticos</span><strong>{{ d.redProducts }}</strong></li>
              </ul>
            </div>
          </div>
        </div>

        <div class="grid-2">
          <div class="panel">
            <div class="panel__head">
              <h2 class="panel__title">Entradas y salidas recientes</h2>
              <div class="legend legend--inline">
                <span><i class="dot dot--in"></i> Entradas</span>
                <span><i class="dot dot--out"></i> Salidas</span>
              </div>
            </div>
            <div class="panel__body">
              @if (d.chart.length) {
                <div class="bars">
                  @for (p of d.chart; track p.date) {
                    <div class="bars__col">
                      <div class="bars__pair">
                        <span class="bar bar--in" [style.height.%]="height(p.entries)" [attr.title]="'Entradas: ' + fmtNumber(p.entries)"></span>
                        <span class="bar bar--out" [style.height.%]="height(p.exits)" [attr.title]="'Salidas: ' + fmtNumber(p.exits)"></span>
                      </div>
                      <span class="bars__label">{{ fmtDate(p.date).slice(0, 5) }}</span>
                    </div>
                  }
                </div>
              } @else {
                <p class="muted">Aún no hay movimientos para graficar.</p>
              }
            </div>
          </div>

          <div class="panel">
            <div class="panel__head"><h2 class="panel__title">Stock bajo el mínimo</h2></div>
            <ul class="list">
              @for (p of d.lowStockProducts; track p.productId) {
                <li class="list__row list__row--stack">
                  <div class="list__split">
                    <strong>{{ p.name }}</strong>
                    <span class="muted">{{ fmtNumber(p.stock) }} / {{ fmtNumber(p.minStock) }} {{ p.unit }}</span>
                  </div>
                  <div class="meter"><span [style.width.%]="meter(p)"></span></div>
                </li>
              } @empty {
                <li class="list__row"><span class="muted">Ningún producto está bajo su mínimo.</span></li>
              }
            </ul>
          </div>
        </div>

        <div class="panel">
          <div class="panel__head"><h2 class="panel__title">Últimos movimientos</h2></div>
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr><th scope="col">Tipo</th><th scope="col">Producto</th><th scope="col">Lote</th><th scope="col">Cantidad</th><th scope="col">Registró</th><th scope="col">Cuándo</th></tr>
              </thead>
              <tbody>
                @for (m of d.recentMovements; track m.id) {
                  <tr>
                    <td>@if (badge(m.type); as b) { <span class="badge" [class]="'badge--' + b.tone">{{ b.label }}</span> }</td>
                    <td>{{ m.productName }}</td>
                    <td>{{ m.lotNumber }}</td>
                    <td>{{ fmtNumber(m.quantity) }}</td>
                    <td>{{ m.userName }}</td>
                    <td>{{ fmtDateTime(m.createdAt) }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="6"><span class="muted">Todavía no hay movimientos.</span></td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      }
    </section>
  `,
})
export class Dashboard implements OnInit {
  private http = inject(HttpClient);

  data = signal<any | null>(null);
  loading = signal(true);
  error = signal('');
  placeholders = [1, 2, 3, 4];

  fmtNumber = fmtNumber;
  fmtDate = fmtDate;
  fmtDateTime = fmtDateTime;
  badge = badgeFor;

  private chartMax = computed(() => {
    const points: any[] = this.data()?.chart ?? [];
    return Math.max(1, ...points.flatMap((p) => [Number(p.entries), Number(p.exits)]));
  });

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.http.get<any>(`${API_URL}/dashboard`).subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(apiError(e));
        this.loading.set(false);
      },
    });
  }

  height(value: unknown): number {
    return Math.max(2, Math.round((Number(value) / this.chartMax()) * 100));
  }

  meter(p: any): number {
    const min = Number(p.minStock);
    return min > 0 ? Math.min(100, Math.round((Number(p.stock) / min) * 100)) : 0;
  }

  alertBadge(a: any): Badge {
    return badgeFor(a.type) ?? { label: 'Aviso', tone: 'muted' };
  }
}