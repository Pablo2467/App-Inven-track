import { Component, HostListener, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { API_URL } from '../core/config';
import { apiError } from '../core/api-error';
import { AuthService } from '../core/auth.service';
import { Badge, badgeFor, expiryBadge, fmtDate, fmtNumber } from '../core/format';
import { CrudConfig } from './crud.models';

/** En la API solo el DUEÑO crea, edita o elimina productos y proveedores. */
const OWNER_ONLY = ['/products', '/suppliers'];
const BADGE_COLUMNS = ['status', 'reason', 'type'];

const PAGE_COPY: Record<string, { title: string; subtitle: string; empty: string }> = {
  '/products': { title: 'Productos', subtitle: 'Tu catálogo: busca, edita y mantén al día los mínimos de stock.', empty: 'Tu catálogo empieza aquí.' },
  '/suppliers': { title: 'Proveedores', subtitle: 'Ten a mano los datos de quienes abastecen tu tienda.', empty: 'Aún no hay proveedores registrados.' },
  '/stock-entries': { title: 'Entradas', subtitle: 'Cada entrada crea un lote con su fecha de vencimiento.', empty: 'Aún no hay entradas registradas.' },
  '/stock-exits': { title: 'Salidas', subtitle: 'Se descuenta primero del lote más antiguo (FIFO) y nunca de lotes vencidos.', empty: 'Aún no hay salidas registradas.' },
  '/batches': { title: 'Lotes', subtitle: 'Existencias por lote, con su vencimiento y su estado.', empty: 'No hay lotes todavía.' },
};

@Component({
  selector: 'app-crud-page',
  imports: [FormsModule],
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <h1 class="page__title">{{ copy.title }}</h1>
          <p class="page__sub">{{ copy.subtitle }}</p>
        </div>
        @if (canCreate()) {
          <button type="button" class="btn btn--primary" (click)="openNew()">
            <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>
            {{ config.newLabel }}
          </button>
        }
      </header>

      @if (error() && !showForm()) {
        <div class="alert alert--error" role="alert">
          <span>{{ error() }}</span>
          <button type="button" class="alert__close" aria-label="Cerrar aviso" (click)="error.set('')">×</button>
        </div>
      }

      <div class="panel">
        <div class="toolbar">
          <label class="search">
            <svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
            <input type="search" [placeholder]="'Buscar en ' + copy.title.toLowerCase() + '…'" [value]="search()" (input)="onSearch($event)" />
          </label>
          <span class="muted">{{ visibleRows().length }} de {{ rows().length }} registros</span>
        </div>

        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                @for (c of config.columns; track c.key) {
                  <th scope="col">{{ c.label }}</th>
                }
                @if (hasActions()) {
                  <th scope="col" class="col-actions">Acciones</th>
                }
              </tr>
            </thead>
            <tbody>
              @if (loading() && rows().length === 0) {
                @for (i of skeletonRows; track i) {
                  <tr>
                    @for (c of config.columns; track c.key) {
                      <td><div class="skeleton"></div></td>
                    }
                    @if (hasActions()) {
                      <td></td>
                    }
                  </tr>
                }
              } @else {
                @for (r of visibleRows(); track r.id) {
                  <tr>
                    @for (c of config.columns; track c.key) {
                      <td>
                        @if (badgeOf(c.key, r[c.key]); as b) {
                          <span class="badge" [class]="'badge--' + b.tone">{{ b.label }}</span>
                        } @else {
                          {{ cell(c.key, r[c.key]) }}
                          @if (expiry(r, c.key); as x) {
                            <span class="badge" [class]="'badge--' + x.tone">{{ x.label }}</span>
                          }
                        }
                      </td>
                    }
                    @if (hasActions()) {
                      <td class="col-actions">
                        @if (canEdit()) {
                          <button type="button" class="btn btn--ghost" (click)="openEdit(r)">Editar</button>
                        }
                        @if (canDelete()) {
                          <button type="button" class="btn btn--ghost btn--danger-text" (click)="askRemove(r)">Eliminar</button>
                        }
                      </td>
                    }
                  </tr>
                } @empty {
                  <tr>
                    <td [attr.colspan]="colCount()">
                      <div class="empty">
                        <p class="empty__title">{{ search() ? 'No encontramos resultados' : 'Todavía no hay registros' }}</p>
                        <p class="empty__text">{{ search() ? 'Prueba con otra palabra o borra la búsqueda.' : copy.empty }}</p>
                        @if (!search() && canCreate()) {
                          <button type="button" class="btn btn--primary" (click)="openNew()">{{ config.newLabel }}</button>
                        }
                      </div>
                    </td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>
      </div>
    </section>

    @if (showForm()) {
      <div class="overlay" (click)="closeForm()"></div>
      <aside class="drawer" role="dialog" aria-modal="true" [attr.aria-label]="drawerTitle()">
        <form class="drawer__form" (ngSubmit)="save()">
          <header class="drawer__head">
            <h2>{{ drawerTitle() }}</h2>
            <button type="button" class="icon-btn" aria-label="Cerrar" (click)="closeForm()">×</button>
          </header>
          <div class="drawer__body">
            @if (error()) {
              <div class="alert alert--error" role="alert"><span>{{ error() }}</span></div>
            }
            @for (f of config.fields; track f.key) {
              <div class="field">
                <label class="field__label" [attr.for]="'f-' + f.key">
                  {{ f.label }}@if (f.required) { <span class="req" aria-hidden="true"> *</span> }
                </label>
                @if (f.type === 'select') {
                  <select [id]="'f-' + f.key" [name]="f.key" [ngModel]="form()[f.key]" (ngModelChange)="setField(f.key, $event)">
                    <option [ngValue]="null">-- Selecciona --</option>
                    @for (o of (f.options ?? options()[f.key] ?? []); track o.value) {
                      <option [ngValue]="o.value">{{ o.label }}</option>
                    }
                  </select>
                } @else if (f.type === 'textarea') {
                  <textarea [id]="'f-' + f.key" [name]="f.key" [ngModel]="form()[f.key]" (ngModelChange)="setField(f.key, $event)"></textarea>
                } @else if (f.type === 'number') {
                  <input type="number" step="any" [id]="'f-' + f.key" [name]="f.key" [ngModel]="form()[f.key]" (ngModelChange)="setField(f.key, $event)" />
                } @else {
                  <input [type]="f.type ?? 'text'" [id]="'f-' + f.key" [name]="f.key" [ngModel]="form()[f.key]" (ngModelChange)="setField(f.key, $event)" />
                }
              </div>
            }
          </div>
          <footer class="drawer__foot">
            <button type="button" class="btn btn--secondary" (click)="closeForm()">Cancelar</button>
            <button type="submit" class="btn btn--primary" [disabled]="saving()">{{ saving() ? 'Guardando…' : submitLabel() }}</button>
          </footer>
        </form>
      </aside>
    }

    @if (pendingDelete(); as row) {
      <div class="overlay" (click)="cancelRemove()"></div>
      <div class="dialog" role="alertdialog" aria-modal="true" aria-labelledby="dlg-title">
        <div class="dialog__box">
          <h2 id="dlg-title" class="dialog__title">¿Eliminar {{ rowLabel(row) }}?</h2>
          <p class="dialog__text">Esta acción no se puede deshacer. Si el registro tiene historial, el sistema lo conserva y te avisa.</p>
          <div class="dialog__actions">
            <button type="button" class="btn btn--secondary" (click)="cancelRemove()">Cancelar</button>
            <button type="button" class="btn btn--danger" (click)="confirmRemove()">Eliminar</button>
          </div>
        </div>
      </div>
    }

    @if (message()) {
      <div class="toast-stack">
        <div class="toast" role="status">
          <span>{{ message() }}</span>
          <button type="button" class="alert__close" aria-label="Cerrar aviso" (click)="message.set('')">×</button>
        </div>
      </div>
    }
  `,
})
export class CrudPage implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  config: CrudConfig = this.route.snapshot.data['config'];
  copy = PAGE_COPY[this.config.path] ?? { title: this.config.title, subtitle: '', empty: 'No hay registros.' };
  skeletonRows = [1, 2, 3, 4, 5];

  rows = signal<any[]>([]);
  options = signal<Record<string, { value: any; label: string }[]>>({});
  form = signal<Record<string, any>>({});
  editingId = signal<number | null>(null);
  showForm = signal(false);
  loading = signal(false);
  saving = signal(false);
  search = signal('');
  pendingDelete = signal<any | null>(null);
  error = signal('');
  message = signal('');
  private toastTimer?: ReturnType<typeof setTimeout>;

  private allowed = computed(() => !OWNER_ONLY.includes(this.config.path) || this.auth.user()?.role === 'DUENO');
  canCreate = computed(() => this.config.canCreate && this.allowed());
  canEdit = computed(() => this.config.canEdit && this.allowed());
  canDelete = computed(() => this.config.canDelete && this.allowed());
  hasActions = computed(() => this.canEdit() || this.canDelete());
  colCount = computed(() => this.config.columns.length + (this.hasActions() ? 1 : 0));

  visibleRows = computed(() => {
    const q = this.search().trim().toLowerCase();
    const all = this.rows();
    if (!q) return all;
    return all.filter((r) => this.config.columns.some((c) => String(r[c.key] ?? '').toLowerCase().includes(q)));
  });

  drawerTitle = computed(() => (this.editingId() !== null ? 'Editar registro' : this.config.newLabel));
  submitLabel = computed(() => {
    if (this.config.path === '/stock-entries') return 'Confirmar entrada';
    if (this.config.path === '/stock-exits') return 'Confirmar salida';
    return 'Guardar';
  });

  ngOnInit() {
    this.load();
    for (const f of this.config.fields.filter((x) => x.optionsUrl)) {
      this.http.get<any>(`${API_URL}${f.optionsUrl}`, { params: { size: 100 } }).subscribe({
        next: (page) =>
          this.options.update((o) => ({
            ...o,
            [f.key]: (page.content ?? page).map((x: any) => ({ value: x.id, label: x[f.optionLabel ?? 'name'] })),
          })),
        error: (e) => this.error.set(apiError(e)),
      });
    }
    if (this.route.snapshot.queryParamMap.get('nuevo') && this.canCreate()) this.openNew();
  }

  ngOnDestroy() {
    clearTimeout(this.toastTimer);
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    if (this.pendingDelete()) this.cancelRemove();
    else if (this.showForm()) this.closeForm();
  }

  onSearch(event: Event) {
    this.search.set((event.target as HTMLInputElement).value);
  }

  badgeOf(key: string, value: unknown): Badge | null {
    return BADGE_COLUMNS.includes(key) ? badgeFor(value) : null;
  }

  expiry(row: any, key: string): Badge | null {
    return key === 'expirationDate' ? expiryBadge(row.expirationDate, row.status) : null;
  }

  cell(key: string, value: unknown): string {
    if (value === null || value === undefined || value === '') return '—';
    if (key.toLowerCase().endsWith('date')) return fmtDate(value);
    return typeof value === 'number' ? fmtNumber(value) : String(value);
  }

  rowLabel(row: any): string {
    return row.name ?? row.code ?? row.lotNumber ?? `#${row.id}`;
  }

  load() {
    this.loading.set(true);
    this.http.get<any>(`${API_URL}${this.config.path}`, { params: { size: 100 } }).subscribe({
      next: (page) => {
        this.rows.set(page.content ?? page);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(apiError(e));
        this.loading.set(false);
      },
    });
  }

  openNew() {
    this.clearMessages();
    this.editingId.set(null);
    this.form.set({});
    this.showForm.set(true);
  }

  openEdit(row: any) {
    this.clearMessages();
    this.editingId.set(row.id);
    const values: Record<string, any> = {};
    for (const f of this.config.fields) values[f.key] = row[f.key] ?? null;
    this.form.set(values);
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.error.set('');
  }

  setField(key: string, value: any) {
    this.form.update((f) => ({ ...f, [key]: value }));
  }

  save() {
    this.clearMessages();
    const values = this.form();
    const missing = this.config.fields.find(
      (f) => f.required && (values[f.key] === null || values[f.key] === undefined || values[f.key] === ''),
    );
    if (missing) {
      this.error.set(`El campo «${missing.label}» es obligatorio.`);
      return;
    }

    const body: Record<string, any> = {};
    for (const f of this.config.fields) {
      const v = values[f.key];
      body[f.key] = v === '' || v === undefined ? null : v;
    }

    const id = this.editingId();
    const url = `${API_URL}${this.config.path}`;
    const request = id ? this.http.put<any>(`${url}/${id}`, body) : this.http.post<any>(url, body);

    this.saving.set(true);
    request.subscribe({
      next: (res) => {
        this.saving.set(false);
        this.notify(this.successText(res, id !== null));
        this.showForm.set(false);
        this.load();
      },
      error: (e) => {
        this.saving.set(false);
        this.error.set(apiError(e));
      },
    });
  }

  askRemove(row: any) {
    this.clearMessages();
    this.pendingDelete.set(row);
  }

  cancelRemove() {
    this.pendingDelete.set(null);
  }

  confirmRemove() {
    const row = this.pendingDelete();
    if (!row) return;
    this.pendingDelete.set(null);
    this.http.delete<any>(`${API_URL}${this.config.path}/${row.id}`).subscribe({
      next: (res) => {
        this.notify(res?.message ?? 'Registro eliminado.');
        this.load();
      },
      error: (e) => this.error.set(apiError(e)),
    });
  }

  private notify(text: string) {
    this.message.set(text);
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.message.set(''), 7000);
  }

  private successText(res: any, editing: boolean): string {
    if (res?.allocations?.length) {
      const lots = res.allocations.map((a: any) => `${a.lotNumber} (${fmtNumber(a.quantity)})`).join(', ');
      return `Salida registrada. Descontado primero de los lotes más antiguos: ${lots}.`;
    }
    return editing ? 'Registro actualizado.' : 'Registro creado.';
  }

  private clearMessages() {
    this.error.set('');
    this.message.set('');
  }
}