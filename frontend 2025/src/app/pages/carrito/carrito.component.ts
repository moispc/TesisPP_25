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
import { SharedDataService } from '../../services/shared-data.service';
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
  direccion: string = '';  // Inicializamos vacío en lugar de 'Sin especificar'
  info: string = '';
  isVisible: boolean = false;
  form: FormGroup;
  isDireccionPerfil: boolean = false;  // Indica si la dirección actual es la del perfil
  private direccionSubscription: Subscription = new Subscription();  // Suscripción a la dirección compartida

  constructor(
    private pedidoService: PedidosService,
    private carritoService: CarritoService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private formBuilder: FormBuilder,
    private sharedDataService: SharedDataService  // Inyectamos el servicio compartido
  ) {
    this.form = this.formBuilder.group({
      domicilio: ['', [Validators.required]],
      info: [''],
    });
  }  ngOnInit(): void {
    // Suscribirse a los cambios de dirección desde el servicio compartido
    this.direccionSubscription = this.sharedDataService.direccion$.subscribe(
      (nuevaDireccion: string) => {
        if (nuevaDireccion && nuevaDireccion.trim() !== '') {
          this.direccion = nuevaDireccion;
          this.form.patchValue({ domicilio: nuevaDireccion });
          this.isDireccionPerfil = true;  // La dirección viene del perfil
        }
      }
    );
    
    // Intentar obtener la dirección del perfil del usuario desde localStorage
    const direccionPerfil = localStorage.getItem('direccion') || '';
    
    // Si existe una dirección en el perfil del usuario, establecerla como valor predeterminado
    if (direccionPerfil && direccionPerfil.trim() !== '') {
      this.direccion = direccionPerfil;
      this.form.patchValue({ domicilio: direccionPerfil });
      this.isDireccionPerfil = true;  // La dirección actual es la del perfil    } else {
      // Ya que no queremos hacer llamadas al backend para la dirección,
      // simplemente dejamos la dirección vacía y permitimos que el usuario la especifique
      this.direccion = '';
      this.isDireccionPerfil = false;
    }
    
    // Cargar los detalles del carrito
    this.cargarDetalle();
    
    // Suscribirse a los cambios en el carrito
    this.subscription.add(
      this.carritoService.actualizarCarrito$.subscribe(() => {
        this.cargarDetalle();
      })
    );
    
    // Suscribirse a los cambios de visibilidad del carrito
    this.subscription.add(
      this.carritoService.carritoVisible$.subscribe(visible => {
        this.isVisible = visible;
      })
    );
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.direccionSubscription) {
      this.direccionSubscription.unsubscribe();
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
  }  irAPagar() {
    // Verificar la dirección antes de continuar
    if (!this.direccion || this.direccion.trim() === '') {
      // Intentar obtener la dirección del perfil como último recurso
      const direccionPerfil = localStorage.getItem('direccion');
      
      // Si hay una dirección guardada en el perfil del usuario, úsala
      if (direccionPerfil && direccionPerfil.trim() !== '') {
        this.direccion = direccionPerfil;
        this.isDireccionPerfil = true;
        // Actualizar el formulario con la dirección del perfil
        this.form.patchValue({ domicilio: direccionPerfil });
        // Mostrar mensaje informativo
        this.toastr.info('Se utilizará la dirección guardada en tu perfil');
        this.continuarPago();
      } else {
        // Si no hay dirección en el perfil, pedir al usuario que especifique una
        this.toastr.error('Debe especificar el domicilio de entrega');
        this.abrirModal();
        return; // Detener la ejecución hasta que se especifique una dirección
      }
    } else {
      // Si ya tenemos una dirección, continuamos
      this.continuarPago();
    }
  }

  // Método separado para continuar con el proceso de pago
  private continuarPago(): void {
    // Obtener datos del usuario
    let nameUser: any = localStorage.getItem('nameUser')
      ? localStorage.getItem('nameUser')
      : 'Sin nombre';

    let emailUser: any = localStorage.getItem('emailUser')
      ? localStorage.getItem('emailUser')
      : '';    // En este punto, this.direccion ya debería tener un valor válido,
    // pero verificamos una última vez por si acaso
    let direccionFinal = this.direccion;
    if (!direccionFinal || direccionFinal.trim() === '') {
      // Último intento de obtener la dirección del perfil
      const direccionPerfil = localStorage.getItem('direccion');
      if (direccionPerfil && direccionPerfil.trim() !== '') {
        direccionFinal = direccionPerfil;
        this.isDireccionPerfil = true;
      } else {
        // Si aún no hay dirección, usamos un valor por defecto más informativo
        direccionFinal = "Por favor contactar para confirmar dirección";
        this.isDireccionPerfil = false;
      }
    }

    // Crear el pedido con la dirección (que puede ser la del perfil o la especificada por el usuario)
    let pedido: Pedido = new Pedido(
      1,
      this.total,
      'Pedido realizado',
      direccionFinal + (this.info ? ' - ' + this.info : ''),
      nameUser,
      this.detallePedido,
      emailUser
    );
    
    this.pedidoService.setPedido(pedido);
    this.cerrarSidebar();
    this.router.navigate(['/pagar']);
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
  }  onEnviar(event: Event) {
    event.preventDefault;
    if (this.form.valid) {
      this.cerrarModal();
      this.direccion = this.form.value.domicilio;
      this.info = this.form.value.info;
      // Solo sincronizar la dirección en la UI, no modificar localStorage ni perfil
      this.sharedDataService.actualizarDireccion(this.direccion);
      // Determinar si estamos usando la dirección del perfil (solo para mostrar en UI)
      const direccionPerfil = localStorage.getItem('direccion') || '';
      this.isDireccionPerfil = this.direccion === direccionPerfil;
      this.toastr.success('Dirección de entrega actualizada para este pedido');
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
  }  abrirModal() {
    // Siempre intentar obtener la dirección del perfil del usuario
    const direccionPerfil = localStorage.getItem('direccion') || '';
    if (direccionPerfil && direccionPerfil.trim() !== '') {
      // Establecer la dirección del perfil en el formulario
      this.form.patchValue({ domicilio: direccionPerfil });
      // También actualizar la dirección actual si estaba vacía
      if (!this.direccion || this.direccion.trim() === '') {
        this.direccion = direccionPerfil;
        this.isDireccionPerfil = true;
      }
    }

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

  get direccionPerfil(): string {
    return localStorage.getItem('direccion') || 'No hay dirección en perfil';
  }
}
