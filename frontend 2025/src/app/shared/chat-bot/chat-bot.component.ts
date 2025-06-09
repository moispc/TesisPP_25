import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-chat-bot',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chat-bot.component.html',
  styleUrl: './chat-bot.component.css'
})
export class ChatBotComponent {
  isOpen = false;
  showOptions = true;
  isHome = false;
  messages: { text: string, from: 'user' | 'bot' }[] = [];

  constructor(private router: Router) {
    this.isHome = this.router.url === '/home' || this.router.url === '/';
    this.router.events?.subscribe(() => {
      this.isHome = this.router.url === '/home' || this.router.url === '/';
    });
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.messages = [];
    }
  }

  handleOption(option: string) {
    this.showOptions = false;
    let response = '';
    switch(option) {
      case 'quienes':
        response = 'Somos ISPC Food, un equipo dedicado a ofrecerte la mejor experiencia gastronómica.';
        break;
      case 'quehacemos':
        response = 'Ofrecemos una carta variada de comidas, pedidos online y atención personalizada.';
        break;
      case 'carta':
        response = 'Te llevo a la carta.';
        this.router.navigate(['/carta']);
        break;
      case 'registro':
        response = 'Te llevo al registro.';
        this.router.navigate(['/registro']);
        break;
      case 'pagos':
        response = 'Aceptamos pagos con tarjeta, efectivo y Mercado Pago.';
        break;
      default:
        response = 'No entendí tu consulta.';
    }
    this.messages = [
      { text: response, from: 'bot' }
    ];
  }

  resetOptions() {
    this.showOptions = true;
    this.messages = [
      { text: '¡Hola! Soy tu asistente virtual. ¿En qué puedo ayudarte?', from: 'bot' }
    ];
  }

  getOptionText(option: string): string {
    switch(option) {
      case 'quienes': return '¿Quiénes son?';
      case 'quehacemos': return '¿Qué hacen?';
      case 'carta': return 'Ver la carta';
      case 'registro': return 'Registrarme';
      case 'pagos': return 'Métodos de pago';
      default: return option;
    }
  }
}
