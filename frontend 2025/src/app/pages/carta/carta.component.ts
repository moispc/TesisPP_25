import { Component, Input } from '@angular/core';
import { OnInit } from '@angular/core';
import { NgFor, CommonModule } from '@angular/common';
import { Producto } from '../../../app/model/producto.model';
import { ProductsService } from '../../services/products.service';
import { FormsModule } from '@angular/forms';
import { PedidosService } from '../../services/pedidos.service';
import { DetallePedido } from '../../model/detallePedido.model';
import { CarritoComponent } from '../carrito/carrito.component';
import { CarritoService } from '../../services/carrito.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { NgxPaginationModule } from 'ngx-pagination';
declare var bootstrap: any;

@Component({
  selector: 'app-carta',
  standalone: true,
  imports: [
    NgFor,
    CommonModule,
    FormsModule,
    CarritoComponent,
    NgxPaginationModule,
  ],
  templateUrl: './carta.component.html',
  styleUrl: './carta.component.css',
})
export class CartaComponent implements OnInit {
  @Input() public producto: Producto;
  productos: Producto[] = [];
  cantidadIngresada: number = 1;
  subtotal: number = 0;
  p: number = 1;
  idUser: number = 0;
  filtroCategoria: string = 'todas';
  mostrarCarrito: boolean = true;

  constructor(
    private productService: ProductsService,
    private pedidoService: PedidosService,
    private carritoService: CarritoService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.producto = {
      id_producto: 0,
      nombre_producto: '',
      imageURL: {},
      precio: 0,
      descripcion: '',
      stock: 0,
      id_categoria: 0,
    };
    // Si es admin, ocultar el carrito
    const email = localStorage.getItem('emailUser');
    if (email === 'admin@admin.com') {
      this.mostrarCarrito = false;
    }
  }
  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (productos: Producto[]) => {
        this.productos = productos;
      },
      error: (error) => {
        if (error.status === 0) this.router.navigate(['serverError']);
      },
    });
  }

  cargarModal(producto: Producto) {
    if (this.estaLogueado()) {
      this.abrirModal();
      this.producto = producto;
      this.calcularSubtotal();
    } else {
      this.toastr.info('Se requiere el inicio de sesión');
      this.router.navigate(['/login']);
    }
  }

  estaLogueado() {
    return localStorage.getItem('authToken') != null ? true : false;
  }
  calcularSubtotal() {
    this.subtotal = this.producto.precio * this.cantidadIngresada;
  }

  addProducto() {
    if (localStorage.getItem('userId') != null) {
      this.idUser = parseFloat(localStorage.getItem('userId')!);
    }    // Usar la dirección del perfil si está disponible
    let direccionEntrega = '';
    const direccionPerfil = localStorage.getItem('direccion');
    if (direccionPerfil && direccionPerfil.trim() !== '') {
      direccionEntrega = direccionPerfil;
    }
    
    const detallePedido = new DetallePedido(
      this.idUser,
      0,
      this.producto.id_producto,
      this.cantidadIngresada,
      direccionEntrega
    );

    this.pedidoService.agregarProducto(detallePedido).subscribe({
      next: (pedido) => {
        this.carritoService.tiggerActualizarCarrito();
        this.toastr.success(pedido.message);
        this.toggleCarrito();
        this.cerrarModal();
      },
      error: (error) => {
        this.toastr.error('Ocurrió un error inesperado.');

        if (error.status === 0) this.router.navigate(['serverError']);
      },
    });
  }

  toggleCarrito() {
    this.carritoService.tiggerActualizarCarrito();
    this.carritoService.toggleCarrito();
  }

  cerrarModal() {
    const modalElement = document.getElementById('modalCompra');
    if (modalElement) {
      const modalInstance =
        bootstrap.Modal.getInstance(modalElement) ||
        new bootstrap.Modal(modalElement);
      modalInstance.hide();
    }
  }

  abrirModal() {
    const modalElement = document.getElementById('modalCompra');
    if (modalElement) {
      const modalInstance =
        bootstrap.Modal.getInstance(modalElement) ||
        new bootstrap.Modal(modalElement);
      modalInstance.show();
    }
  }

  get productosFiltrados(): Producto[] {
    if (this.filtroCategoria === 'todas') return this.productos;
    if (this.filtroCategoria === 'empanadas') return this.productos.filter(p => p.id_categoria === 1);
    if (this.filtroCategoria === 'lomos') return this.productos.filter(p => p.id_categoria === 2);
    if (this.filtroCategoria === 'hamburguesas') return this.productos.filter(p => p.id_categoria === 3);
    return this.productos;
  }
}
