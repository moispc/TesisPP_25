import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { Pedido } from '../../model/pedido.model';
import { PedidosService } from '../../services/pedidos.service';
import { MercadoPagoService } from '../../services/mercado-pago.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-exito',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './exito.component.html',
  styleUrl: './exito.component.css',
})
export class ExitoComponent implements OnInit {
  pedido: Pedido = new Pedido(0, 0, '', '', '', []);
  fechaActual: Date;
  paymentId: string | null = null;
  status: string | null = null;
  paymentType: string | null = null;
  mercadoPagoTicket: any = null;
  paymentMethod: string = '';
  
  // URLs de los iconos de métodos de pago
  paymentIcons = {
    'paypal': 'https://cdn.icon-icons.com/icons2/2108/PNG/512/paypal_icon_130860.png',
    'mercadopago': 'https://contactopuro.com/files/mercadopago-81090.png',
    'card': 'https://cdn.icon-icons.com/icons2/1186/PNG/512/1490135017-visa_82256.png'
  };
  
  // Número de factura generado aleatoriamente para esta demo
  numeroFactura: string = '';
  
  constructor(
    private router: Router, 
    private route: ActivatedRoute,
    private pedidoService: PedidosService,
    private mercadoPagoService: MercadoPagoService,
    private toastr: ToastrService
  ) {
    this.fechaActual = new Date();
    this.generarNumeroFactura();
  }
  
  // Método para generar un número de factura único
  generarNumeroFactura() {
    const fecha = new Date();
    const año = fecha.getFullYear().toString().slice(-2);
    const mes = (fecha.getMonth() + 1).toString().padStart(2, '0');
    const dia = fecha.getDate().toString().padStart(2, '0');
    const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
    
    this.numeroFactura = `${año}${mes}${dia}-${random}`;
  }
  
  // Método mejorado para imprimir el recibo
  printDiv() {
    // Guardar el título original
    const originalTitle = document.title;
    
    // Cambiar el título para la impresión
    document.title = `Recibo ISPC Food #${this.numeroFactura}`;
    
    // Imprimir la página
    window.print();
    
    // Restaurar el título original
    document.title = originalTitle;
  }
  ngOnInit(): void {
    this.pedido = this.pedidoService.getPedido();
    
    // Si no hay pedido en el servicio, intentar recuperarlo del localStorage
    if (!this.pedido || !this.pedido.total) {
      try {
        const savedPedido = localStorage.getItem('ultimoPedido');
        if (savedPedido) {
          this.pedido = JSON.parse(savedPedido);
        }
      } catch (e) {
        console.error('Error al recuperar pedido del localStorage:', e);
      }
    } else {
      // Guardar el pedido actual en localStorage para recuperarlo si se refresca la página
      localStorage.setItem('ultimoPedido', JSON.stringify(this.pedido));
    }
    
    // Recuperar el método de pago del sessionStorage o localStorage
    this.paymentMethod = sessionStorage.getItem('paymentMethod') || 
                         localStorage.getItem('paymentMethod') || 
                         'card'; // Por defecto, asumimos tarjeta
    
    // Si el paymentId existe, asumimos que el pago fue con MercadoPago
    if (this.paymentId) {
      this.paymentMethod = 'mercadopago';
    }
    
    // Verificar si hay parámetros de MercadoPago en la URL
    this.route.queryParams.subscribe(params => {
      this.paymentId = params['payment_id'] || null;
      this.status = params['status'] || null;
      this.paymentType = params['payment_type'] || null;
      
      // Si hay un parámetro de método de pago, usarlo
      if (params['payment_method']) {
        this.paymentMethod = params['payment_method'];
      }
      
      // Si viene el ticket de MercadoPago en los queryParams, parsearlo
      if (params['mp_ticket']) {
        try {
          this.mercadoPagoTicket = JSON.parse(params['mp_ticket']);
          this.paymentMethod = 'mercadopago'; // Establecer método como MercadoPago
        } catch (e) {
          this.mercadoPagoTicket = null;
        }
      }
      
      if (this.paymentId && this.status && !this.mercadoPagoTicket) {
        this.verificarPagoMercadoPago();
        this.paymentMethod = 'mercadopago'; // Establecer método como MercadoPago
      }
    });
  }
  
  verificarPagoMercadoPago() {
    if (!this.paymentId) return;
    
    // Confirmar el pedido en el backend al volver de Mercado Pago
    this.pedidoService.confirmarPedido().subscribe({
      next: () => {
        this.toastr.success('¡Pedido confirmado y ticket generado!');
        // No asignar paymentId, status ni paymentType al pedido porque no existen en el modelo
      },
      error: (error) => {
        this.toastr.error('No se pudo confirmar el pedido automáticamente.');
      }
    });
    
    this.mercadoPagoService.verificarPago(this.paymentId).subscribe({
      next: (response) => {
        if (response && response.status === 'approved') {
          this.toastr.success('¡Pago confirmado con MercadoPago!');
          // Redirigir a /exito si no estamos ya allí
          if (this.router.url !== '/exito') {
            this.router.navigate(['/exito'], {
              queryParams: {
                payment_id: this.paymentId,
                status: this.status,
                payment_type: this.paymentType,
                mp_ticket: JSON.stringify(response)
              }
            });
            return;
          }
          // Guardar el ticket de MercadoPago para mostrarlo
          this.mercadoPagoTicket = response;
        } else if (response && response.status === 'in_process') {
          this.toastr.info('El pago está siendo procesado.');
        } else if (response && response.status === 'rejected') {
          this.toastr.error('El pago fue rechazado.');
          setTimeout(() => {
            this.router.navigate(['/checkout']);
          }, 5000);
          return;
        }
      },
      error: (error) => {
        console.error('Error al verificar estado del pago:', error);
        this.toastr.warning('No se pudo verificar el estado del pago.');
      }
    });
  }
  
  // Método para obtener el nombre legible del método de pago
  getPaymentMethodName(): string {
    switch(this.paymentMethod) {
      case 'paypal':
        return 'PayPal';
      case 'mercadopago':
        return 'Mercado Pago';
      case 'card':
        return 'Tarjeta de Crédito/Débito';
      default:
        return 'Pago en línea';
    }
  }
  
  // Método para obtener el icono del método de pago
  getPaymentMethodIcon(): string {
    return this.paymentIcons[this.paymentMethod as keyof typeof this.paymentIcons] || '';
  }
}
