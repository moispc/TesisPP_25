import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Pedido } from '../../model/pedido.model';
import { PedidosService } from '../../services/pedidos.service';
import { MercadoPagoService } from '../../services/mercado-pago.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-exito',
  standalone: true,
  imports: [CommonModule],
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
  
  constructor(
    private router: Router, 
    private route: ActivatedRoute,
    private pedidoService: PedidosService,
    private mercadoPagoService: MercadoPagoService,
    private toastr: ToastrService
  ) {
    this.fechaActual = new Date();
  }
  
  printDiv() {
    window.print();
    window.location.href = './home';
  }
  
  ngOnInit(): void {
    this.pedido = this.pedidoService.getPedido();
    
    // Verificar si hay parámetros de MercadoPago en la URL
    this.route.queryParams.subscribe(params => {
      this.paymentId = params['payment_id'] || null;
      this.status = params['status'] || null;
      this.paymentType = params['payment_type'] || null;
      
      // Si viene el ticket de MercadoPago en los queryParams, parsearlo
      if (params['mp_ticket']) {
        try {
          this.mercadoPagoTicket = JSON.parse(params['mp_ticket']);
        } catch (e) {
          this.mercadoPagoTicket = null;
        }
      }
      
      if (this.paymentId && this.status && !this.mercadoPagoTicket) {
        this.verificarPagoMercadoPago();
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
}
