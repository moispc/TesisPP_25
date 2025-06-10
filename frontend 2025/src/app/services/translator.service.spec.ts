import { TestBed } from '@angular/core/testing';
import { TranslatorService } from './translator.service';
import { DirectTranslationService } from './direct-translation.service';

describe('TranslatorService', () => {
  let service: TranslatorService;
  let directTranslationService: jasmine.SpyObj<DirectTranslationService>;

  beforeEach(() => {
    // Crear un spy para DirectTranslationService
    const directTranslationSpy = jasmine.createSpyObj('DirectTranslationService', ['translateDirectly']);
    
    TestBed.configureTestingModule({
      providers: [
        TranslatorService,
        { provide: DirectTranslationService, useValue: directTranslationSpy }
      ]
    });
    
    service = TestBed.inject(TranslatorService);
    directTranslationService = TestBed.inject(DirectTranslationService) as jasmine.SpyObj<DirectTranslationService>;

    // Limpiar localStorage antes de cada prueba
    localStorage.clear();
    
    // Mock para document.cookie
    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: '',
    });
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have Spanish as default language', (done) => {
    service.language.subscribe(lang => {
      expect(lang).toBe('es');
      done();
    });
  });

  it('should change language', () => {
    // Espiar el método window.location.reload
    spyOn(window.location, 'reload').and.callFake(() => {});
    
    // Iniciar el cambio de idioma
    service.changeLanguage('en');
    
    // Verificar que se guardó en localStorage
    expect(localStorage.getItem('selectedLanguage')).toBe('en');
    
    // Verificar que se llamó al servicio de traducción directa
    expect(directTranslationService.translateDirectly).toHaveBeenCalledWith('en');
  });

  it('should remove translation when changing to Spanish', () => {
    // Espiar el método window.location.reload
    spyOn(window.location, 'reload').and.callFake(() => {});
    
    // Cambiar a español
    service.changeLanguage('es');
    
    // Verificar que no se guardó en localStorage
    expect(localStorage.getItem('selectedLanguage')).toBeNull();
  });

  it('should have a list of available languages', () => {
    expect(service.availableLanguages.length).toBe(3);
    expect(service.availableLanguages[0].code).toBe('es');
    expect(service.availableLanguages[1].code).toBe('en');
    expect(service.availableLanguages[2].code).toBe('pt');
  });
});
