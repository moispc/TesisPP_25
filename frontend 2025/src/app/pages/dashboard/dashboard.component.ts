import { Component, OnInit } from '@angular/core';
import { DashboardService, IPedido, IPedidosData } from '../../services/dashboard.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { CarritoService } from '../../services/carrito.service';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})

export class DashboardComponent implements OnInit{

  pedidosData: IPedidosData = { pendientes: [], aprobados: [], entregados: [] };
  pedidosFiltrados: IPedido[] = [];
  activeTab: string = 'Pendientes';
  nombre: string = '';
  isLoading: boolean = true; // Añadir indicador de carga
  
  constructor(private dashboardService: DashboardService, private authService: AuthService, private toastr: ToastrService, private carritoService: CarritoService) {}

  ngOnInit(): void {
    this.nombre = localStorage.getItem('nameUser') || '';
    this.obtenerPedidos();
  }
  obtenerPedidos(): void {
    this.isLoading = true;
    this.dashboardService.obtenerPedidos().subscribe({
      next: (data: any) => {
        // Adaptar los datos recibidos para que todo lo que venga en results vaya a entregados
        this.pedidosData = {
          pendientes: (data.results || []).filter((p: IPedido) => p.estado === 'Pendiente'),
          aprobados: (data.results || []).filter((p: IPedido) => p.estado === 'Aprobado por Chayanne' || p.estado === 'Aprobado'),
          entregados: (data.results || [])
            .filter((p: IPedido) => p.estado !== 'Pendiente' && p.estado !== 'Aprobado por Chayanne' && p.estado !== 'Aprobado')
            .sort((a: IPedido, b: IPedido) => new Date(b.fecha_pedido).getTime() - new Date(a.fecha_pedido).getTime())
        };
        this.setActiveTab(this.activeTab);
        this.isLoading = false;
        // Mover cada pedido aprobado (Aprobado o Aprobado por Chayanne) a entregados después de 1 minuto
        this.pedidosData.aprobados.forEach((pedido, idx) => {
          setTimeout(() => {
            const aprobadoIndex = this.pedidosData.aprobados.findIndex(p => p.id_pedidos === pedido.id_pedidos);
            if (aprobadoIndex !== -1) {
              // Permitir ambos estados para el cambio
              this.dashboardService.marcarComoEntregado(pedido.id_pedidos).subscribe({
                next: () => {
                  const pedidoEntregado = { ...this.pedidosData.aprobados[aprobadoIndex], estado: 'Entregado' };
                  this.pedidosData.aprobados.splice(aprobadoIndex, 1);
                  this.pedidosData.entregados.push(pedidoEntregado);
                  if (this.activeTab === 'Aprobados') {
                    this.setActiveTab('Aprobados');
                  }
                  if (this.activeTab === 'Entregados') {
                    this.setActiveTab('Entregados');
                  }
                },
                error: () => {
                  this.toastr.error('No se pudo marcar como entregado en el backend.');
                }
              });
            }
          }, 15000 * (idx + 1)); // 15 segundos por cada uno
        });
      },
      error: (error) => {
        console.error('Error al obtener pedidos:', error);
        if (error.error && error.error.detail === 'Given token not valid for any token type') {
          this.toastr.info('Su sesión ha expirado. Debe iniciar sesión nuevamente');
          this.authService.logout();
        } else {
          this.toastr.error('No se pudieron cargar los pedidos. Intente nuevamente más tarde.');
        }
        this.isLoading = false;
      },
    });
  }


  //filtro los pedidos
  setActiveTab(tab: string): void {
    this.activeTab = tab;
    let pedidos: IPedido[] = [];
    switch (tab) {
      case 'Pendientes':
        pedidos = this.pedidosData.pendientes || [];
        break;
      case 'Aprobados':
        pedidos = this.pedidosData.aprobados || [];
        break;
      case 'Entregados':
        pedidos = this.pedidosData.entregados || [];
        break;
      default:
        pedidos = [];
    }
    // Ordenar siempre por fecha_pedido descendente (más nuevos primero)
    this.pedidosFiltrados = pedidos.slice().sort((a: IPedido, b: IPedido) => {
      // Si hay hora_pedido, concatenar para mayor precisión
      const fechaA = a.fecha_pedido ? new Date(a.fecha_pedido).getTime() : 0;
      const fechaB = b.fecha_pedido ? new Date(b.fecha_pedido).getTime() : 0;
      return fechaB - fechaA;
    });
  }

  cancelarPedido(pedido: IPedido): void {
    this.pedidosData.pendientes = this.pedidosData.pendientes.filter(p => p.id_pedidos !== pedido.id_pedidos);
    if (this.activeTab === 'Pendientes') {
      this.setActiveTab('Pendientes');
    }
    // Vaciar el carrito y actualizar la UI
    if (typeof window !== 'undefined' && localStorage) {
      localStorage.removeItem('carrito');
    }
    this.carritoService.tiggerActualizarCarrito();
    this.toastr.info('Pedido cancelado y carrito vaciado.');
  }
}
