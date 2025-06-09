import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable, Subject } from 'rxjs';
import { DetallePedido } from '../model/detallePedido.model';
import { Carrito } from '../model/Carrito.model';
import { CarritoComponent } from '../pages/carrito/carrito.component';
import { Pedido } from '../model/pedido.model';


@Injectable({
  providedIn: 'root'
})
export class PedidosService {

  url:string="appCART/";
  private pedido: Pedido = new Pedido(0,0,"","","",[]);


  constructor(private http:HttpClient) { }


  setPedido(pedido: Pedido) {
    this.pedido = pedido;
  }

  getPedido() {
    return this.pedido;
  }

  getPedidos() {
    return this.http.get(this.url + 'pedidos');
  }

  getDetallePedido():Observable <Carrito[]> {

    return this.http.get<Carrito[]>(this.url + 'ver');
  }  confirmarPedido():Observable<any>{
    // Verificar si hay una dirección de entrega en el pedido actual
    // Si no hay, intentar usar la dirección del perfil
    const pedidoActual = this.getPedido();
    if (pedidoActual && (!pedidoActual.direccion || pedidoActual.direccion.trim() === '')) {
      const direccionPerfil = localStorage.getItem('direccion');
      if (direccionPerfil && direccionPerfil.trim() !== '') {
        pedidoActual.direccion = direccionPerfil;
        this.setPedido(pedidoActual);
      } else {
        // Si no hay dirección de perfil, usar un valor informativo
        pedidoActual.direccion = "Por favor contactar para confirmar dirección";
        this.setPedido(pedidoActual);
      }
    }
    
    const hola={};
    return this.http.post(this.url + 'confirmar/',hola);
  }

  deleteDetallePedido(detalle:Carrito):Observable<void>{

    return this.http.delete<void>(this.url + 'eliminar/'+detalle.id)

  }

  public agregarProducto(product:DetallePedido):Observable<any> {

    return this.http.post(this.url + 'agregar/'+ product.id_producto+'/',product  );
  }
}
