import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router';
import { NavComponent } from './shared/nav/nav.component';
import { CarritoComponent } from './pages/carrito/carrito.component';
import { FooterComponent } from './shared/footer/footer.component';
import { ContactoService } from './services/contacto.service';
import { LanguageSelectorComponent } from './shared/language-selector/language-selector.component';
import { ChatBotComponent } from './shared/chat-bot/chat-bot.component';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    NavComponent,
    RouterOutlet,
    CarritoComponent,
    CommonModule,
    FooterComponent,
    LanguageSelectorComponent,
    ChatBotComponent
  ],

  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'ISPC Food';
  vistaUsuario = false;
  estaAutenticado = false;
  esAdmin = false;
  isHome = false;

  constructor(public contactoService: ContactoService, private router: Router) {
    const email = localStorage.getItem('emailUser');
    this.estaAutenticado = !!localStorage.getItem('authToken');
    this.esAdmin = email === 'admin@admin.com';

    // Detectar ruta actual
    this.router.events.subscribe(() => {
      this.isHome = this.router.url === '/home' || this.router.url === '/';
    });
  }

  setVistaUsuario(valor: boolean) {
    this.vistaUsuario = valor;
  }
}
