export interface ApiErrorBody {
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  errors?: Record<string, string>;
}

export function apiErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  const err = error as { error?: ApiErrorBody; message?: string; status?: number } | null;
  if (!err) {
    return fallback;
  }
  if (err.error?.errors && Object.keys(err.error.errors).length > 0) {
    return Object.values(err.error.errors).join(' · ');
  }
  if (err.error?.message) {
    return err.error.message;
  }
  if (err.status === 0) {
    return 'Cannot reach a Mecanico service. Check that the Users API (8083) and Passport API (8082) are running.';
  }
  return err.message || fallback;
}
