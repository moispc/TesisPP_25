import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, map } from 'rxjs';
import { PedidosService } from './pedidos.service';

@Injectable({
  providedIn: 'root'
})
export class MercadoPagoService {
  private apiUrl = 'appCART/';
  private mercadoPagoUrl = 'https://backmp.onrender.com/';

  constructor(
    private http: HttpClient,
    private pedidoService: PedidosService
  ) { }

  /**
   * Crea una preferencia de pago en MercadoPago
   * @returns Observable con la URL de pago (init_point)
   */
  crearPreferencia(): Observable<any> {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      console.error('No se encontr� el token de autenticaci�n');
      return new Observable(observer => {
        observer.error({ message: 'No se encontr� el token de autenticaci�n' });
        observer.complete();
      });
    }
    
    // Eliminar comillas si las hay
    const cleanToken = token.replace(/"/g, '');
    
    // Obtener el pedido del servicio
    const pedido = this.pedidoService.getPedido();
    
    if (!pedido || !pedido.carrito || pedido.carrito.length === 0) {
      console.error('No hay items en el carrito');
      return new Observable(observer => {
        observer.error({ message: 'No hay items en el carrito' });
        observer.complete();
      });
    }
    
    // Preparar items para MercadoPago
    const items = pedido.carrito.map(item => {
      return {
        title: item.producto,
        quantity: item.cantidad,
        unit_price: item.precio,
        currency_id: 'ARS',
        id: item.id.toString()
      };
    });
    
    // Preparar payload para la solicitud
    const email = pedido.email || localStorage.getItem('emailUser') || 'usuario@ejemplo.com';
    
    const payload = {
      user_token: cleanToken,
      email: email,
      items: items,
      payer: {
        name: pedido.nombreCliente.split(' ')[0] || 'Cliente',
        surname: pedido.nombreCliente.split(' ')[1] || 'An�nimo',
        email: email,
        address: {
          street_name: pedido.direccion || 'Direcci�n no especificada'
        }
      },
      back_urls: {
        success: 'https://backmp.onrender.com/payment/success/',
        failure: window.location.origin + '/checkout',
        pending: window.location.origin + '/exito'
      },
      auto_return: 'approved',
      external_reference: pedido.idPedido.toString() || Date.now().toString()
    };
    
    console.log('Enviando solicitud a MercadoPago:', payload);
    
    // Configurar headers para la solicitud
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${cleanToken}`);
    
    // Llamar al endpoint para crear preferencia de pago
    console.log('URL del endpoint:', `${this.mercadoPagoUrl}payment/create-preference/`);
    console.log('Headers enviados:', headers);
    
    return this.http.post<any>(`${this.mercadoPagoUrl}payment/create-preference/`, payload, { 
      headers: headers
    }).pipe(
      map((response: any) => {
        // Asegurarnos de que la respuesta incluya el ID de preferencia en un formato consistente
        console.log('Respuesta original de MercadoPago:', response);
        
        // La respuesta puede tener diferentes estructuras seg�n el backend
        // Buscamos el init_point en diferentes ubicaciones de la respuesta
        if (!response.init_point) {
          // Verificar si init_point est� en response.body
          if (response.body && response.body.init_point) {
            response.init_point = response.body.init_point;
          }
          // Verificar si init_point est� en response.data
          else if (response.data && response.data.init_point) {
            response.init_point = response.data.init_point;
          }
          // Verificar si init_point est� en response.response
          else if (response.response && response.response.init_point) {
            response.init_point = response.response.init_point;
          }
          // Verificar si init_point est� en response.sandbox_init_point (para ambiente de prueba)
          else if (response.sandbox_init_point) {
            response.init_point = response.sandbox_init_point;
          }
          // Verificar si hay un payment_request_id
          else if (response.payment_request_id) {
            console.log('Se detect� payment_request_id:', response.payment_request_id);
            
            // Intentamos construir diferentes formatos de URL usando el payment_request_id
            // Para identificar el formato correcto, verificamos si parece ser un UUID (contiene guiones)
            if (response.payment_request_id.includes('-')) {
              response.init_point = `https://www.mercadopago.com.ar/checkout/v1/redirect?payment-id=${response.payment_request_id}`;
            } else {
              response.init_point = `https://www.mercadopago.com.ar/checkout/v1/redirect?preference-id=${response.payment_request_id}`;
            }
            
            console.log('URL construida con payment_request_id:', response.init_point);
          }
        }
        
        // Si response.id no existe, intentamos extraerlo de otros lugares
        if (!response.id) {
          if (response.body && response.body.id) {
            response.id = response.body.id;
          } else if (response.data && response.data.id) {
            response.id = response.data.id;
          } else if (response.preference_id) {
            response.id = response.preference_id;
          } else if (response.payment_request_id) {
            response.id = response.payment_request_id; // Usamos el payment_request_id como id
          }
        }
        
        console.log('Respuesta de MercadoPago procesada:', response);
        return response;
      }),
      catchError((error: any) => {
        console.error('Error completo de MercadoPago:', error);
        if (error.error && error.error.detail) {
          console.error('Detalle del error:', error.error.detail);
        }
        throw error;
      })
    );
  }
  
  /**
   * Verifica el estado del servicio de MercadoPago
   * @returns Observable con el estado del servicio
   */
  verificarServicio(): Observable<any> {
    return this.http.get<any>(`${this.mercadoPagoUrl}payment/health/`);
  }

  /**
   * Verifica el estado de un pago de MercadoPago
   * @param paymentId ID del pago de MercadoPago
   * @returns Observable con el estado del pago
   */
  verificarPago(paymentId: string): Observable<any> {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      console.error('No se encontr� el token de autenticaci�n');
      return new Observable(observer => {
        observer.error({ message: 'No se encontr� el token de autenticaci�n' });
        observer.complete();
      });
    }
    
    const cleanToken = token.replace(/"/g, '');
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${cleanToken}`);
    
    return this.http.get<any>(`${this.mercadoPagoUrl}payment/status/${paymentId}/`, { 
      headers: headers
    });
  }

  /**
   * Verifica y procesa un pago usando el ID de solicitud de pago
   * @param paymentRequestId ID de la solicitud de pago
   * @returns Observable con el estado del pago y la URL de redirecci�n
   */
  verificarPagoConRequestId(paymentRequestId: string): Observable<any> {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      console.error('No se encontr� el token de autenticaci�n');
      return new Observable(observer => {
        observer.error({ message: 'No se encontr� el token de autenticaci�n' });
        observer.complete();
      });
    }
    
    const cleanToken = token.replace(/"/g, '');
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set('Authorization', `Bearer ${cleanToken}`);
    
    // Este endpoint puede no existir. Si es necesario, habr�a que implementarlo en el backend
    return this.http.get<any>(`${this.mercadoPagoUrl}payment/request/${paymentRequestId}/`, { 
      headers: headers
    }).pipe(
      map((response: any) => {
        console.log('Respuesta de verificaci�n con payment_request_id:', response);
        
        // Si la respuesta contiene una URL de pago, la extraemos
        if (response && response.payment_url) {
          response.redirectUrl = response.payment_url;
        } else if (response && response.init_point) {
          response.redirectUrl = response.init_point;
        }
        
        return response;
      })
    );
  }

  /**
   * Recibe notificaciones de MercadoPago v�a webhook
   * @param data Datos de la notificaci�n
   * @returns Observable con la respuesta del servidor
   */
  recibirWebhook(data: any): Observable<any> {
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json');
    
    return this.http.post<any>(`${this.mercadoPagoUrl}payment/webhook/`, data, { 
      headers: headers
    });
  }

  /**
   * Consulta el estado y detalles de una PaymentRequest por su UUID
   * @param paymentRequestId UUID de la PaymentRequest
   * @returns Observable con el estado y detalles
   */
  consultarEstadoPaymentRequest(paymentRequestId: string) {
    // Asume que el backend expone el endpoint: http://127.0.0.1:8000/<payment_request_id>/
    const url = `${this.mercadoPagoUrl}${paymentRequestId}/`;
    return this.http.get<any>(url);
  }
}
