import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgFor, CommonModule } from '@angular/common';
import { PedidosService } from '../../services/pedidos.service';
import { Carrito } from '../../model/Carrito.model';
import { CarritoService } from '../../services/carrito.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Pedido } from '../../model/pedido.model';
declare var bootstrap: any;

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [NgFor, ReactiveFormsModule, CommonModule],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css',
})
export class CarritoComponent implements OnInit, OnDestroy {
  detallePedido: Carrito[] = [];
  detallePedidoParcial: Carrito[] = [];
  total: number = 0;
  subscription: Subscription = new Subscription();
  direccion: string = 'Sin especificar';
  info: string = '';
  isVisible: boolean = false;
  form: FormGroup;

  constructor(
    private pedidoService: PedidosService,
    private carritoService: CarritoService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private formBuilder: FormBuilder
  ) {
    this.form = this.formBuilder.group({
      domicilio: ['', [Validators.required]],
      info: [''],
    });
  }
  ngOnInit(): void {
    this.subscription = this.carritoService.carritoVisible$.subscribe(
      (visible) => {
        this.isVisible = visible;
      }
    );

    this.carritoService.actualizarCarrito$.subscribe({
      next: () => {
        if (localStorage.getItem('authToken') != null) {
          this.cargarDetalle();
        }
      },
      error: (error) => {
        console.log(error);
      },
    });

    // Cargar el detalle al inicializar si hay token
    if (localStorage.getItem('authToken') != null) {
      this.cargarDetalle();
    }
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  sidebarVisible: boolean = false;

  cerrarSidebar() {
    this.sidebarVisible = !this.sidebarVisible;
    this.carritoService.toggleCarrito();
  }

  public cargarDetalle() {
    this.pedidoService.getDetallePedido().subscribe({
      next: (detalle: Carrito[]) => {
        this.total = 0;
        this.detallePedido = detalle;
        for (let det of detalle) {
          this.total += det.precio * det.cantidad;
        }
      },
      error: (error) => {
        if (error.error.detail == 'Given token not valid for any token type') {
          this.toastr.info(
            'Su sesión a expirado. Debe iniciar sesión nuevamente'
          );
          this.authService.logout();
        }
      },
    });
  }
  irAPagar() {
    if (this.direccion == 'Sin especificar') {
      this.toastr.error('Debe especificar el domicilio de entrega');
      this.abrirModal();
    } else {
      let nameUser: any = localStorage.getItem('nameUser')
        ? localStorage.getItem('nameUser')
        : 'Sin nombre';

      let pedido: Pedido = new Pedido(
        1,
        this.total,
        'Pedido realizado',
        this.direccion + '-' + this.info,
        nameUser,
        this.detallePedido
      );
      this.pedidoService.setPedido(pedido);
      this.cerrarSidebar();
      this.router.navigate(['/pagar']);
    }
  }

  eliminarDetalle(detalle: Carrito) {
    this.pedidoService.deleteDetallePedido(detalle).subscribe({
      next: () => {
        this.toastr.success('Se eliminó el producto del carrito');
        this.cargarDetalle();
      },
      error: (error) => {
        this.toastr.error(error);
      },
    });
  }

  onEnviar(event: Event) {
    event.preventDefault;
    if (this.form.valid) {
      this.cerrarModal();
      this.direccion = this.form.value.domicilio;
      this.info = this.form.value.info;
    } else {
      this.form.markAllAsTouched();
    }
  }
  cerrarModal() {
    const modalElement = document.getElementById('modalCambioDireccion');
    if (modalElement) {
      const modalInstance =
        bootstrap.Modal.getInstance(modalElement) ||
        new bootstrap.Modal(modalElement);
      modalInstance.hide();
    }
  }
  abrirModal() {
    const modalElement = document.getElementById('modalCambioDireccion');
    if (modalElement) {
      const modalInstance =
        bootstrap.Modal.getInstance(modalElement) ||
        new bootstrap.Modal(modalElement);
      modalInstance.show();
    }
  }

  get Domicilio() {
    return this.form.get('domicilio');
  }
}
