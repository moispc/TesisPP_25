import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DirectTranslationService {
  private iframeId = 'direct-translation-frame';

  constructor() {}

  // Método para traducir directamente utilizando la API de Google Translate
  translateDirectly(targetLanguage: string): void {
    if (targetLanguage === 'es') {
      // Eliminar iframe si existe
      this.removeTranslationIframe();
      return;
    }
    
    try {
      // Eliminar iframe previo si existe
      this.removeTranslationIframe();
      
      // Crear iframe
      const iframe = document.createElement('iframe');
      iframe.id = this.iframeId;
      iframe.style.position = 'absolute';
      iframe.style.top = '-9999px';
      iframe.style.left = '-9999px';
      iframe.style.height = '1px';
      iframe.style.width = '1px';
      
      // URL de traducción directa
      const translationUrl = `https://translate.google.com/translate?sl=es&tl=${targetLanguage}&u=${encodeURIComponent(window.location.href)}`;
      iframe.src = translationUrl;
      
      // Agregar iframe al DOM
      document.body.appendChild(iframe);
        // No extraemos la traducción para evitar bucles de recarga
      // El iframe solo se usa para establecer las cookies de traducción
      setTimeout(() => {
        // Eliminamos el iframe después de un tiempo para evitar problemas
        this.removeTranslationIframe();
      }, 2000);
    } catch (error) {
      console.error('Error al traducir directamente:', error);
    }
  }
  
  // Método para eliminar el iframe de traducción
  private removeTranslationIframe(): void {
    const existingIframe = document.getElementById(this.iframeId);
    if (existingIframe) {
      existingIframe.remove();
    }
  }
}
