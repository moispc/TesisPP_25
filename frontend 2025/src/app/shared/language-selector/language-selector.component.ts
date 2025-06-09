import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatorService } from '../../services/translator.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [CommonModule],  template: `
    <div class="language-selector-container">
      <button 
        class="language-selector-button" 
        [class.active]="isDropdownOpen" 
        (click)="toggleDropdown()">
        <img class="current-flag" [src]="getCurrentLanguageFlag()" [alt]="getCurrentLanguageName() + ' flag'">
        <span class="language-tooltip">Cambiar idioma</span>
      </button>
      
      <div class="language-dropdown" [class.show]="isDropdownOpen">
        <div 
          class="language-option" 
          *ngFor="let lang of translatorService.availableLanguages" 
          (click)="selectLanguage(lang.code)"
          [class.active]="currentLanguage === lang.code">
          <img class="language-flag" [src]="lang.flag" [alt]="lang.name + ' flag'">
          <span class="language-name">{{ lang.name }}</span>
          <i *ngIf="currentLanguage === lang.code" class="bi bi-check2"></i>
        </div>
      </div>
    </div>
  `,  styles: [`    .language-selector-container {
      position: fixed;
      bottom: 40px;
      left: 30px;
      z-index: 1060; /* Mayor que el z-index del botón de WhatsApp (1050) */
    }    .language-selector-button {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background-color: transparent;
      color: white;
      border: none;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
      position: relative;
      overflow: visible;
      padding: 0;
      box-sizing: border-box;
    }.current-flag {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      object-fit: cover;
      position: relative;
    }.language-selector-button:hover {
      transform: scale(1.05);
      box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
    }
      .language-tooltip {
      position: absolute;
      left: 70px;
      background-color: #333;
      color: white;
      padding: 5px 10px;
      border-radius: 4px;
      font-size: 14px;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.2s;
      white-space: nowrap;
      z-index: 1070; /* Asegurar que esté por encima de todo */
    }
    
    .language-selector-button:hover .language-tooltip {
      opacity: 1;
    }.language-dropdown {
      position: absolute;
      bottom: 70px;
      left: 0;
      background-color: white;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      min-width: 150px;
      overflow: hidden;
      max-height: 0;
      transition: max-height 0.3s ease, opacity 0.3s ease;
      opacity: 0;
      visibility: hidden;
      z-index: 1060; /* Asegurar que esté por encima del botón de WhatsApp */
    }

    .language-dropdown.show {
      max-height: 200px;
      opacity: 1;
      visibility: visible;
    }

    .language-option {
      padding: 12px 16px;
      cursor: pointer;
      display: flex;
      align-items: center;
      transition: background-color 0.2s ease;
    }

    .language-option:hover {
      background-color: #f5f5f5;
    }

    .language-option.active {
      background-color: #f0f0f0;
    }
    
    .language-option i {
      margin-left: auto;
      color: #25D366;
    }

    .language-flag {
      width: 20px;
      height: 15px;
      margin-right: 12px;
      border-radius: 2px;
      object-fit: cover;
    }
    
    .language-name {
      font-size: 14px;
      font-weight: 500;
    }    @media (max-width: 768px) {
      .language-selector-container {
        bottom: 30px;
        left: 20px;
      }
      
      .language-selector-button {
        width: 50px;
        height: 50px;
      }
    }
  `]
})
export class LanguageSelectorComponent implements OnInit, OnDestroy {
  isDropdownOpen = false;
  currentLanguage = 'es';
  private langSubscription: Subscription | null = null;
  
  constructor(public translatorService: TranslatorService) {}

  ngOnInit(): void {
    // Obtener el idioma current del servicio
    this.langSubscription = this.translatorService.language.subscribe(lang => {
      this.currentLanguage = lang;
    });

    // Verificar si hay un idioma guardado en localStorage
    const savedLang = localStorage.getItem('selectedLanguage');
    if (savedLang) {
      this.currentLanguage = savedLang;
    }
    
    // Cerrar el dropdown cuando se hace clic en cualquier parte fuera de él
    document.addEventListener('click', (event) => {
      const target = event.target as HTMLElement;
      if (!target.closest('.language-selector-container')) {
        this.isDropdownOpen = false;
      }
    });
  }
  
  // Obtener el nombre del idioma actual
  getCurrentLanguageName(): string {
    const lang = this.translatorService.availableLanguages.find(l => l.code === this.currentLanguage);
    return lang ? lang.name : 'Español';
  }
  
  // Obtener la bandera del idioma actual
  getCurrentLanguageFlag(): string {
    const lang = this.translatorService.availableLanguages.find(l => l.code === this.currentLanguage);
    return lang ? lang.flag : 'assets/flags/es.svg';
  }

  ngOnDestroy(): void {
    // Cancelar la suscripción cuando el componente se destruye
    if (this.langSubscription) {
      this.langSubscription.unsubscribe();
    }
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectLanguage(langCode: string): void {
    if (this.currentLanguage === langCode) {
      this.isDropdownOpen = false;
      return; // No hacer nada si ya estamos en ese idioma
    }
    
    this.currentLanguage = langCode;
    this.translatorService.changeLanguage(langCode);
    this.isDropdownOpen = false;
  }
}
