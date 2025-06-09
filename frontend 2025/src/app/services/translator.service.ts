import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { DirectTranslationService } from './direct-translation.service';

@Injectable({
  providedIn: 'root'
})
export class TranslatorService {
  private renderer: Renderer2;
  private script: HTMLScriptElement | null = null;
  private currentLanguage = new BehaviorSubject<string>('es'); // Idioma por defecto: español
  // Lista de idiomas soportados
  public availableLanguages = [
    { code: 'es', name: 'Español', flag: 'assets/flags/es-circle.svg' },
    { code: 'en', name: 'English', flag: 'assets/flags/en-circle.svg' },
    { code: 'pt', name: 'Português', flag: 'assets/flags/pt-circle.svg' }
  ];
  constructor(
    rendererFactory: RendererFactory2,
    private directTranslationService: DirectTranslationService
  ) {
    this.renderer = rendererFactory.createRenderer(null, null);
    // Verificar si hay un idioma guardado previamente
    this.checkSavedLanguage();
  }

  // Comprobar si hay un idioma guardado previamente
  private checkSavedLanguage(): void {
    const savedLanguage = localStorage.getItem('selectedLanguage');
    // Leer la cookie actual de Google Translate
    const googtrans = document.cookie
      .split('; ')
      .find(row => row.startsWith('googtrans='));
    const cookieLang = googtrans ? googtrans.split('/')[2] : 'es';

    // Solo cambiar si el idioma guardado es distinto al de la cookie
    if (
      savedLanguage &&
      savedLanguage !== 'es' &&
      savedLanguage !== cookieLang
    ) {
      this.changeLanguage(savedLanguage);
    }
  }

  // Obtener el idioma currente
  get language() {
    return this.currentLanguage.asObservable();
  }
  // Cambiar el idioma
  changeLanguage(languageCode: string): void {
    // Si seleccionamos español, simplemente eliminamos el widget y recargamos la página
    if (languageCode === 'es') {
      this.removeTranslateWidget();
      localStorage.removeItem('selectedLanguage');
      window.location.reload();
      return;
    }

    // Leer la cookie actual de Google Translate
    const googtrans = document.cookie
      .split('; ')
      .find(row => row.startsWith('googtrans='));
    const cookieLang = googtrans ? googtrans.split('/')[2] : 'es';

    // Si el idioma es distinto al de la cookie, setear y recargar
    if (languageCode !== cookieLang) {
      localStorage.setItem('selectedLanguage', languageCode);
      this.setTranslateCookie(languageCode);
      window.location.reload();
      return;
    }

    // Si ya está en el idioma correcto, solo actualiza el observable (opcional)
    this.currentLanguage.next(languageCode);
    
    // Almacenar el idioma seleccionado en localStorage
    localStorage.setItem('selectedLanguage', languageCode);
    
    // Método directo para establecer la cookie que activa la traducción
    this.setTranslateCookie(languageCode);
    
    // Inicializar el widget
    this.initTranslateWidget(languageCode);
    
    // Método alternativo: usar el iframe de traducción directamente
    this.useDirectTranslation(languageCode);
    
    // Utilizar el servicio de traducción directa como backup (sólo configuración de cookies)
    this.directTranslationService.translateDirectly(languageCode);
  }
  
  // Método adicional para establecer la cookie de traducción directamente
  private setTranslateCookie(lang: string): void {
    // Establecer las cookies para Google Translate
    document.cookie = `googtrans=/es/${lang}`;
    document.cookie = `googtrans=/es/${lang}; domain=.${window.location.host}`;
    document.cookie = `googtrans=/es/${lang}; domain=${window.location.host}`;
  }
  
  // Método que usa un iframe para traducción directa
  private useDirectTranslation(lang: string): void {
    // Solo para inglés y portugués
    if (lang !== 'en' && lang !== 'pt') return;
    
    try {
      // Intentar traducir usando la API de Google Translate directamente
      const gtUrl = `https://translate.google.com/translate?&anno=2&u=${encodeURIComponent(window.location.href)}&hl=${lang}&sl=es&tl=${lang}`;
      
      // Crear un iframe oculto para realizar la traducción
      const iframe = document.createElement('iframe');
      iframe.style.display = 'none';
      iframe.src = gtUrl;
      
      // Agregar el iframe al DOM
      document.body.appendChild(iframe);
      
      // Remover después de 2 segundos
      setTimeout(() => {
        iframe.remove();
      }, 2000);
    } catch (e) {
      console.error('Error al intentar traducción directa:', e);
    }
  }
  // Inicializar el widget de traducción de Google
  private initTranslateWidget(lang: string): void {
    // Eliminar cualquier script previo
    this.removeTranslateWidget();
    
    // Establecer cookie directamente (esto es crucial para la traducción)
    document.cookie = `googtrans=/es/${lang}`;
    
    // Crear elemento div para el widget de Google Translate (invisible)
    const translateDiv = this.renderer.createElement('div');
    this.renderer.setAttribute(translateDiv, 'id', 'google_translate_element');
    this.renderer.setStyle(translateDiv, 'display', 'none');
    this.renderer.appendChild(document.body, translateDiv);

    // Almacenar el idioma seleccionado en localStorage
    localStorage.setItem('selectedLanguage', lang);

    // Crear el script de Google Translate
    const script = this.renderer.createElement('script') as HTMLScriptElement;
    script.type = 'text/javascript';
    script.innerHTML = `
      function googleTranslateElementInit() {
        new google.translate.TranslateElement({
          pageLanguage: 'es',
          includedLanguages: 'en,pt',
          layout: google.translate.TranslateElement.InlineLayout.SIMPLE,
          autoDisplay: true,
          gaTrack: false
        }, 'google_translate_element');          // Función para ocultar los elementos de Google Translate
        const hideTranslateElements = () => {
          // Ocultar elementos visuales
          const elements = ['.goog-te-banner-frame', '.skiptranslate', '.goog-te-gadget-icon'];
          elements.forEach(selector => {
            const els = document.querySelectorAll(selector);
            els.forEach(el => {
              if (el instanceof HTMLElement) {
                el.style.display = 'none';
              }
            });
          });
          
          // Forzar el cambio de idioma de manera manual
          const iframe = document.querySelector('iframe.goog-te-menu-frame') as HTMLIFrameElement;
          if (iframe) {
            const innerDoc = iframe.contentDocument || iframe.contentWindow?.document;
            if (innerDoc) {
              // Buscar y hacer clic en el elemento del idioma deseado
              setTimeout(() => {
                const items = innerDoc.querySelectorAll('.goog-te-menu2-item');
                items.forEach((item: any) => {
                  if (item.innerText.includes('${lang === 'en' ? 'English' : 'Português'}')) {
                    item.click();
                  }
                });
              }, 300);
            }
          }
          
          // Eliminar el desplazamiento de página que añade Google Translate
          document.body.style.top = '0px';
          document.documentElement.style.height = 'auto';
        };
          // Ejecutar varias veces para asegurar que se aplica
        setTimeout(hideTranslateElements, 500);
        setTimeout(hideTranslateElements, 1000);
        setTimeout(hideTranslateElements, 2000);
        
        // Método de contingencia para garantizar la traducción
        setTimeout(() => {
          // Método alternativo: forzar traducción con método doGoogleLanguageTranslator
          if(window.location && window.location.href) {
            const currentUrl = window.location.href;
            const langParam = '${lang}';
            if (langParam === 'en' || langParam === 'pt') {
              // Aplicar traducción
              const translateApiUrl = 'https://translate.google.com/translate?hl=' + 
                langParam + '&sl=es&tl=' + langParam + 
                '&u=' + encodeURIComponent(currentUrl);
              
              // Crear un iframe invisible para realizar la traducción
              const hiddenFrame = document.createElement('iframe');
              hiddenFrame.style.display = 'none';
              hiddenFrame.src = translateApiUrl;
              document.body.appendChild(hiddenFrame);
              
              // Eliminar el iframe después de un tiempo
              setTimeout(() => hiddenFrame.remove(), 5000);
            }
          }
        }, 2500);
      }
    `;
    this.renderer.appendChild(document.body, script);
    this.script = script;

    // Usar un enfoque directo de traducción
    const directTranslateScript = this.renderer.createElement('script') as HTMLScriptElement;
    directTranslateScript.type = 'text/javascript';
    directTranslateScript.innerHTML = `
      // Método adicional para asegurar traducción
      function translatePage() {
        document.cookie = 'googtrans=/es/${lang}';
      }
      translatePage();
    `;
    this.renderer.appendChild(document.body, directTranslateScript);

    // Cargar el script de Google Translate API de manera asíncrona
    const translatorScript = this.renderer.createElement('script') as HTMLScriptElement;
    translatorScript.async = true;
    this.renderer.setAttribute(translatorScript, 'src', 
      'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit');
    this.renderer.appendChild(document.body, translatorScript);
  }
  // Eliminar el widget de traducción
  private removeTranslateWidget(): void {
    // Eliminar elementos de Google Translate
    const elements = [
      '.goog-te-banner-frame',
      '.goog-te-menu-frame',
      '#goog-gt-',
      '#google_translate_element',
      '.skiptranslate',
      '.VIpgJd-ZVi9od-aZ2wEe-wOHMyf',
      '.VIpgJd-ZVi9od-aZ2wEe',
      'iframe.goog-te-menu-frame'
    ];

    elements.forEach(selector => {
      const elements = document.querySelectorAll(selector);
      elements.forEach(el => el.remove());
    });

    // Eliminar cookies de Google Translate (enfoque exhaustivo)
    const domains = [
      '/', 
      window.location.hostname, 
      '.' + window.location.hostname, 
      window.location.host, 
      '.' + window.location.host
    ];
    
    const paths = ['/', '/.*'];
    
    domains.forEach(domain => {
      paths.forEach(path => {
        document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=' + path + ';';
        document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=' + path + '; domain=' + domain + ';';
      });
    });
  }
}
