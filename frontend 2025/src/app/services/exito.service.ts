import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Usuario } from '../model/usuario.model';
import { Pedido } from '../model/pedido.model';
import { Carrito } from '../model/Carrito.model';
import { DetallePedido } from '../model/detallePedido.model';
import { ToastrService } from 'ngx-toastr';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ExitosService {
  private apiUrlConfirmar = 'appCART/confirmar/';
  
  constructor(private http: HttpClient, private toastr: ToastrService) { }

  // Método para confirmar el pedido
  confirmarPedido(datosPedido: any): Observable<any> {
    return this.http.post<any>(this.apiUrlConfirmar, datosPedido).pipe(
      map(response => {
        this.toastr.success('Pedido confirmado con éxito');
        return response;
      })
    );
  }

  getFactura(): Observable<{usuario:Usuario, pedidos: Pedido[], carrito:Carrito, detallePedido: DetallePedido[]}> {
    // Este método se puede actualizar si es necesario para obtener la factura del nuevo backend
    return this.http.get<{usuario:Usuario, pedidos: Pedido[], carrito:Carrito, detallePedido: DetallePedido[]}>(this.apiUrlConfirmar);
  }
}
