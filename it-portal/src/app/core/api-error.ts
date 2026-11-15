import { HttpErrorResponse } from '@angular/common/http';

export function apiError(e: unknown): string {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) {
      return 'No se pudo conectar con el servidor. Verifica que la API esté encendida.';
    }
    const body = e.error;
    const msg = body?.message ?? 'Ocurrió un error inesperado.';
    const details = body?.details ? Object.values(body.details).join(' · ') : '';
    return details ? `${msg}: ${details}` : msg;
  }
  return 'Ocurrió un error inesperado.';
}