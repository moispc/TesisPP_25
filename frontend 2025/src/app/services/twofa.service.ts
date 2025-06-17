import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TwofaService {
  private apiUrl = 'https://back2fa.onrender.com/api/2fa/'; // URL del microservicio 2FA

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });
  }

  setup2fa(email: string): Observable<any> {
    console.log('Enviando solicitud setup2fa para:', email);
    const headers = this.getHeaders();
    return this.http.post(this.apiUrl + 'setup/', { email }, { headers }).pipe(
      tap(resp => console.log('Respuesta setup2fa:', resp)),
      catchError(err => {
        console.error('Error en setup2fa:', err);
        throw err;
      })
    );
  }

  verify2fa(email: string, code: string): Observable<any> {
    console.log('Enviando solicitud verify2fa para:', email, 'con código:', code);
    const headers = this.getHeaders();
    return this.http.post(this.apiUrl + 'verify/', { email, code }, { headers }).pipe(
      tap(resp => console.log('Respuesta verify2fa:', resp)),
      catchError(err => {
        console.error('Error en verify2fa:', err);
        throw err;
      })
    );
  }

  authorizePurchase(email: string, monto: number, titular: string, code?: string): Observable<any> {
    console.log('Enviando solicitud authorizePurchase para:', email, 'monto:', monto, 'titular:', titular);
    const headers = this.getHeaders();
    return this.http.post(this.apiUrl + 'authorize/', { email, monto, titular, code }, { headers }).pipe(
      tap(resp => console.log('Respuesta authorizePurchase:', resp)),
      catchError(err => {
        console.error('Error en authorizePurchase:', err);
        throw err;
      })
    );
  }
}
