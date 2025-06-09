import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebhookService {
  private apiUrl = 'appCART/';

  constructor(private http: HttpClient) { }

  /**
   * Verifica el estado de un pago mediante la API
   * @param pagoId ID del pago a verificar
   * @returns Observable con la respuesta del servidor
   */
  verificarEstadoPago(pagoId: string): Observable<any> {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      console.error('No se encontró el token de autenticación');
      return new Observable(observer => {
        observer.error({ message: 'No se encontró el token de autenticación' });
        observer.complete();
      });
    }
    
    // Eliminar comillas si las hay
    const cleanToken = token.replace(/"/g, '');
    
    // Configurar headers para la solicitud
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${cleanToken}`);
    
    console.log('Verificando estado del pago:', pagoId);
    console.log('Token usado para verificación:', cleanToken);
    
    // Usamos el endpoint de status general
    return this.http.get<any>(`${this.apiUrl}payment/status/${pagoId}/`, { 
      headers: headers,
      withCredentials: true // Incluir credenciales en la solicitud
    });
  }
}
