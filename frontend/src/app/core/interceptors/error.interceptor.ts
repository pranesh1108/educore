import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

// Normalizes the different error body shapes returned by the backend:
//  - plain string body (most business exceptions, e.g. UserNotFoundException)
//  - field-error map (validation failures, e.g. { "email": "Invalid email format" })
//  - structured object (401/403 from CustomAuthenticationEntryPoint / CustomAccessDeniedHandler)
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message = 'Internal Server Error.';

      const body = error.error;

      if (typeof body === 'string' && body.trim().length > 0) {
        message = body;
      } else if (body && typeof body === 'object') {
        if (typeof body.message === 'string') {
          message = body.message;
        } else {
          const firstFieldError = Object.values(body)[0];
          if (typeof firstFieldError === 'string') {
            message = firstFieldError;
          }
        }
      }

      return throwError(() => ({ status: error.status, message }));
    })
  );
};
