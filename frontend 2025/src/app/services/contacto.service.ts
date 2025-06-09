import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ContactoService {
  // Número de WhatsApp de la empresa (número real)
  private numeroWhatsApp: string = '5493517460026';
  
  // URL base para WhatsApp Web
  private baseUrl: string = 'https://wa.me/';

  constructor() { }

  /**
   * Genera una URL para contacto directo por WhatsApp
   * @param mensaje Mensaje predeterminado para enviar (opcional)
   * @param referencia Número de referencia como pedido o pago (opcional)
   * @returns URL completa para abrir chat de WhatsApp
   */
  getUrlWhatsApp(mensaje?: string, referencia?: string): string {
    let mensajeCompleto = 'Hola, me comunico desde la web de ISPC Food.';
    
    if (mensaje) {
      mensajeCompleto = mensaje;
    }
    
    if (referencia) {
      mensajeCompleto += ` Referencia: ${referencia}`;
    }
    
    // Codificar el mensaje para URL
    const mensajeCodificado = encodeURIComponent(mensajeCompleto);
    
    return `${this.baseUrl}${this.numeroWhatsApp}?text=${mensajeCodificado}`;
  }
  
  /**
   * Genera una URL para consulta sobre un pedido específico
   * @param idPedido ID del pedido a consultar
   * @returns URL completa para abrir chat de WhatsApp con consulta sobre el pedido
   */
  getUrlConsultaPedido(idPedido: number | string): string {
    const mensaje = `Hola, tengo una consulta sobre mi pedido #${idPedido}.`;
    return this.getUrlWhatsApp(mensaje);
  }
  
  /**
   * Genera una URL para consulta sobre un pago específico
   * @param idPago ID del pago a consultar
   * @returns URL completa para abrir chat de WhatsApp con consulta sobre el pago
   */
  getUrlConsultaPago(idPago: string): string {
    const mensaje = `Hola, tengo una consulta sobre mi pago con ID ${idPago}.`;
    return this.getUrlWhatsApp(mensaje);
  }
  
  /**
   * Abre directamente una ventana de WhatsApp con un mensaje predefinido
   * @param mensaje Mensaje predeterminado para enviar
   * @param referencia Número de referencia como pedido o pago (opcional)
   */
  abrirWhatsApp(mensaje?: string, referencia?: string): void {
    const url = this.getUrlWhatsApp(mensaje, referencia);
    window.open(url, '_blank');
  }
}
