import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private apiUrlAgregar = 'appCART/agregar/';
  private apiUrlVer = 'appCART/ver/';
  private apiUrlEliminar = 'appCART/eliminar/';
  private apiUrlModificarCantidad = 'appCART/modificar_cantidad/';

  private actualizarCarritoSubject = new Subject<void>();
  actualizarCarrito$ = this.actualizarCarritoSubject.asObservable();
  
  private carritoVisible = new BehaviorSubject<boolean>(false);
  carritoVisible$ = this.carritoVisible.asObservable();
  
  constructor(private http: HttpClient, private toastr: ToastrService) {}

  // Método para obtener el contenido del carrito
  obtenerCarrito(): Observable<any> {
    return this.http.get<any>(this.apiUrlVer);
  }

  // Método para agregar un producto al carrito
  agregarProducto(idProducto: string, cantidad: number = 1): Observable<any> {
    return this.http.post<any>(`${this.apiUrlAgregar}${idProducto}/`, { cantidad });
  }

  // Método para eliminar un item del carrito
  eliminarItemCarrito(idCarrito: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrlEliminar}${idCarrito}/`);
  }

  // Método que elimina un item del carrito y actualiza la UI
  eliminarItemYActualizar(idCarrito: string): void {
    this.eliminarItemCarrito(idCarrito).subscribe({
      next: (response) => {
        this.toastr.success('Producto eliminado del carrito');
        this.tiggerActualizarCarrito();
      },
      error: (error) => {
        this.toastr.error('Error al eliminar producto del carrito');
        console.error('Error al eliminar del carrito:', error);
      }
    });
  }

  // Método que llama al API y luego dispara la actualización de la UI
  agregarProductoYActualizar(idProducto: string, cantidad: number = 1): void {
    this.agregarProducto(idProducto, cantidad).subscribe({
      next: (response) => {
        this.toastr.success('Producto agregado al carrito');
        this.tiggerActualizarCarrito();
      },
      error: (error) => {
        this.toastr.error('Error al agregar producto al carrito');
        console.error('Error al agregar al carrito:', error);
      }
    });
  }

  // Método para modificar la cantidad de un producto en el carrito
  modificarCantidad(idCarrito: string, cantidad: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrlModificarCantidad}${idCarrito}/`, { cantidad });
  }

  // Método que modifica la cantidad y actualiza la UI
  modificarCantidadYActualizar(idCarrito: string, cantidad: number): void {
    this.modificarCantidad(idCarrito, cantidad).subscribe({
      next: (response) => {
        this.toastr.success('Cantidad actualizada correctamente');
        this.tiggerActualizarCarrito();
      },
      error: (error) => {
        this.toastr.error('Error al actualizar la cantidad');
        console.error('Error al modificar cantidad:', error);
      }
    });
  }

  tiggerActualizarCarrito() {
    this.actualizarCarritoSubject.next();
  }

  triggerCerrarSidebar(visible: boolean) {
    this.carritoVisible.next(visible);
  }

  cerrarCarrito() {
    this.carritoVisible.next(false);
  }
  
  mostrarCarrito() {
    this.carritoVisible.next(true);
  }
  
  toggleCarrito() {
    this.carritoVisible.next(!this.carritoVisible.value);
  }
  
  esVisible() {
    return this.carritoVisible.value;
  }
}
