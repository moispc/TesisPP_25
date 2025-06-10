// Este script sirve para cargar directamente la traducción
// Se carga después de que la página se ha renderizado completamente

document.addEventListener('DOMContentLoaded', function() {
  // Verificar si hay un idioma guardado
  const savedLang = localStorage.getItem('selectedLanguage');
  
  // Verificar si hay una traducción en progreso para evitar bucles
  const translationInProgress = sessionStorage.getItem('translation_in_progress');
  
  // Limpiar el flag de traducción en progreso
  sessionStorage.removeItem('translation_in_progress');
  
  if (savedLang && savedLang !== 'es' && !translationInProgress) {
    // Establecer la cookie de Google Translate
    document.cookie = `googtrans=/es/${savedLang}`;
    
    // Intentar activar la traducción de varias formas
    setTimeout(function() {
      try {
        // Método 1: Cargar el script de Google Translate
        const googleScript = document.createElement('script');
        googleScript.src = `https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit`;
        document.body.appendChild(googleScript);
        
        // Método 2: Usar Google Translate API directamente
        const translateDiv = document.createElement('div');
        translateDiv.id = 'google_translate_element';
        translateDiv.style.display = 'none';
        document.body.appendChild(translateDiv);
        
        // Definir la función que inicializa Google Translate
        window.googleTranslateElementInit = function() {
          new google.translate.TranslateElement({
            pageLanguage: 'es',
            includedLanguages: 'en,pt',
            autoDisplay: true
          }, 'google_translate_element');
        };
      } catch (e) {
        console.error('Error al intentar la traducción automática:', e);
      }
    }, 1000);
  }
});
