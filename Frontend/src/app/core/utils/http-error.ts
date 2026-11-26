import { HttpErrorResponse } from '@angular/common/http';

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Le serveur est injoignable. Vérifie que le backend est démarré sur http://localhost:8081.';
    }

    const body = error.error as { message?: unknown } | null;
    if (body && typeof body.message === 'string' && body.message.trim().length > 0) {
      return body.message;
    }

    if (error.status === 404) {
      return 'La ressource demandée est introuvable.';
    }
    if (error.status === 409) {
      return 'L’opération entre en conflit avec les données existantes.';
    }
    if (error.status >= 500) {
      return 'Le serveur a rencontré une erreur. Réessaie plus tard.';
    }
  }

  return fallback;
}
