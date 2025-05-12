import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  let token = localStorage.getItem('authToken');
  const baseURL = 'https://backmobile1.onrender.com/';

  // Verificar si la URL ya tiene el baseURL para evitar duplicación
  const isAbsoluteUrl = req.url.startsWith('http://') || req.url.startsWith('https://');

  // No agregar Authorization si es Cloudinary
  const isCloudinary = req.url.includes('cloudinary.com');

  if (isAbsoluteUrl) {
    if (token && !isCloudinary) {
      req = req.clone({
        setHeaders: {
          'Authorization': `Bearer ${token}`
        }
      });
    }
  } else {
    if (token) {
      req = req.clone({
        url: baseURL + req.url,
        setHeaders: {
          'Authorization': `Bearer ${token}`
        }
      });
    } else {
      req = req.clone({
        url: baseURL + req.url
      });
    }
  }

  // LOG para depuración
  if (req.url.includes('update')) {
    console.log('[INTERCEPTOR] URL:', req.url);
    console.log('[INTERCEPTOR] Método:', req.method);
    console.log('[INTERCEPTOR] Authorization header:', req.headers.get('Authorization'));
    if (req.body) {
      console.log('[INTERCEPTOR] Body:', req.body);
    }
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('Error en la autenticación HttpErrorResponse', error, 'es es el error, no estas mando el token del login en el cuerpo de la solicitud');
      if (error.status === 401) {
        console.log('Error de autenticación 401: Token inválido o expirado');
      } else if (error.status === 404) {
        console.log('Error 404: Recurso no encontrado. URL solicitada:', req.url);
      }
      return throwError(() => error);
    })
  );
};
