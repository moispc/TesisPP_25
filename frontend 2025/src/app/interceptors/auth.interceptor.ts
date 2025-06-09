import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastr = inject(ToastrService);
  
  let token = localStorage.getItem('authToken');
  
  // Limpiar comillas si las hay (común en tokens guardados en localStorage)
  if (token) {
    token = token.replace(/"/g, '');
  }
  
  const baseURL = 'https://backmobile1.onrender.com/';

  // Verificar si la URL ya tiene el baseURL para evitar duplicación
  const isAbsoluteUrl = req.url.startsWith('http://') || req.url.startsWith('https://');

  // No agregar Authorization si es Cloudinary
  const isCloudinary = req.url.includes('cloudinary.com');

  // Comprobar si el token está presente y si no es una petición a Cloudinary
  // Si ya tiene headers de Authorization, no los sobrescribimos
  if (token && !isCloudinary && !req.headers.has('Authorization')) {
    // Clonamos la solicitud y añadimos el header de Authorization
    req = req.clone({
      setHeaders: {
        'Authorization': `Bearer ${token}`
      }
    });

    // Log adicional para verificar
    if (req.url.includes('checkout')) {
      console.log('[INTERCEPTOR] Authorization header añadido:', `Bearer ${token}`);
    }
  }

  // Ahora manejar la base URL si es necesario
  if (!isAbsoluteUrl) {
    // Construir la URL absoluta
    req = req.clone({
      url: baseURL + req.url
    });

    // Log para verificar
    if (req.url.includes('checkout')) {
      console.log('[INTERCEPTOR] URL construida:', baseURL + req.url);
    }
  }

  // LOG para depuración en rutas específicas
  if (req.url.includes('checkout') || req.url.includes('webhook')) {
    console.log('[INTERCEPTOR] URL final:', req.url);
    console.log('[INTERCEPTOR] Método:', req.method);
    console.log('[INTERCEPTOR] Headers completos:', req.headers);
    if (req.body) {
      console.log('[INTERCEPTOR] Body:', JSON.stringify(req.body));
    }
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 || error.status === 403) {
        console.log('Error de autenticación:', error.status, error.message);
        
        // Si es un error de autenticación y estamos en una ruta que requiere autenticación
        if (!req.url.includes('login')) {
          // Mostrar mensaje al usuario
          toastr.error('Tu sesión ha expirado. Por favor, inicia sesión nuevamente.');
          
          // Redirigir al login si es necesario
          if (error.status === 401) {
            // Limpiamos el token
            localStorage.removeItem('authToken');
            router.navigate(['/login']);
          }
        }
      } else if (error.status === 500) {
        console.error(`Error HTTP 500:`, error.message);
        
        // Si es un error del servidor en el proceso de pago
        if (req.url.includes('checkout')) {
          console.error('Error en la solicitud de pago:', {
            url: req.url,
            method: req.method,
            headers: req.headers,
            body: req.body
          });
          
          // Mostrar mensaje específico para errores de pago
          toastr.error('Error al procesar el pago. Por favor, intenta nuevamente.');
        } else {
          toastr.error('Error en el servidor. Por favor, intenta nuevamente más tarde.');
        }
      } else if (error.status === 400) {
        console.error(`Error HTTP 400:`, error);
        
        // Mostrar mensaje específico para errores de validación
        if (error.error && error.error.detail) {
          toastr.error(error.error.detail);
        } else {
          toastr.error('Error en los datos enviados. Por favor, verifica la información.');
        }
      } else {
        console.error(`Error HTTP ${error.status}:`, error.message);
        toastr.error('Error de conexión. Por favor, verifica tu conexión a internet.');
      }
      
      return throwError(() => error);
    })
  );
};
