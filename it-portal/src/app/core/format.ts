const numberFormat = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 3 });

export const EXPIRING_DAYS = 7; // mismo umbral que app.alerts.expiring-days de la API

export type Tone = 'ok' | 'warn' | 'danger' | 'info' | 'muted';
export interface Badge {
  label: string;
  tone: Tone;
}

const BADGES: Record<string, Badge> = {
  ACTIVE: { label: 'Activo', tone: 'ok' },
  INACTIVE: { label: 'Inactivo', tone: 'muted' },
  AVAILABLE: { label: 'Disponible', tone: 'ok' },
  DEPLETED: { label: 'Agotado', tone: 'muted' },
  EXPIRED: { label: 'Vencido', tone: 'danger' },
  SALE: { label: 'Venta', tone: 'info' },
  EXPIRED_DISPOSAL: { label: 'Baja por vencimiento', tone: 'danger' },
  DAMAGE: { label: 'Daño', tone: 'warn' },
  INTERNAL_USE: { label: 'Uso interno', tone: 'muted' },
  OTHER: { label: 'Otro', tone: 'muted' },
  ENTRY: { label: 'Entrada', tone: 'ok' },
  EXIT: { label: 'Salida', tone: 'info' },
  EXPIRING: { label: 'Por vencer', tone: 'warn' },
  LOW_STOCK: { label: 'Stock bajo', tone: 'warn' },
};

export function badgeFor(value: unknown): Badge | null {
  return typeof value === 'string' ? (BADGES[value] ?? null) : null;
}

export function fmtNumber(value: unknown): string {
  if (value === null || value === undefined || value === '') return '—';
  return typeof value === 'number' ? numberFormat.format(value) : String(value);
}

export function fmtDate(value: unknown): string {
  if (!value) return '—';
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(String(value));
  return m ? `${m[3]}/${m[2]}/${m[1]}` : String(value);
}

export function fmtDateTime(value: unknown): string {
  if (!value) return '—';
  const d = new Date(String(value));
  return isNaN(d.getTime())
    ? String(value)
    : d.toLocaleString('es-CO', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
}

/** Días desde hoy hasta la fecha (negativo = ya venció). */
export function daysUntil(value: string): number {
  const [y, m, d] = value.slice(0, 10).split('-').map(Number);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Math.round((new Date(y, m - 1, d).getTime() - today.getTime()) / 86400000);
}

export function expiryBadge(date: unknown, status?: unknown): Badge | null {
  if (!date || status === 'DEPLETED') return null;
  const d = daysUntil(String(date));
  if (d < 0) return { label: 'Vencido', tone: 'danger' };
  if (d === 0) return { label: 'Vence hoy', tone: 'warn' };
  if (d <= EXPIRING_DAYS) return { label: `Vence en ${d} d`, tone: 'warn' };
  return null;
}