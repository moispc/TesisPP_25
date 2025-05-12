import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Pedido } from '../../model/pedido.model';
import { PedidosService } from '../../services/pedidos.service';

@Component({
  selector: 'app-exito',
  standalone: true,
  imports: [],
  templateUrl: './exito.component.html',
  styleUrl: './exito.component.css',
})
export class ExitoComponent implements OnInit {
  contador = 30;
  pedido: Pedido = new Pedido(0, 0, '', '', '', []);
  fechaActual: Date;
  constructor(private router: Router, private pedidoService: PedidosService) {
    this.fechaActual = new Date();
  }
  printDiv() {
    window.print();
    window.location.href = './home';
  }
  ngOnInit(): void {
    this.pedido = this.pedidoService.getPedido();
    this.startCountdown();
  }

  startCountdown() {
    const contdownInterval = setInterval(() => {
      this.contador--;
      if (this.contador == 0) {
        clearInterval(contdownInterval);
        this.router.navigate(['/']);
      }
    }, 1000);
  }
}
