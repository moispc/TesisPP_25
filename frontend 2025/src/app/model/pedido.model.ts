import { Carrito } from "./Carrito.model";

export class Pedido{
    constructor(idPedido:number, total:number, descripcion:string, direccion:string, nombreCliente:string, carrito:Carrito[] ){
        this.idPedido=idPedido;
        this.total=total;
        this.descripcion=descripcion;
        this.direccion=direccion;
        this.nombreCliente=nombreCliente;
        this.carrito=carrito;
    }

    idPedido:number=0;
    total:number=0;
    descripcion:string="";
    direccion:string="";
    nombreCliente:string="";
    carrito: Carrito[]=[];
  }

