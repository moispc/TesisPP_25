import { Carrito } from "./Carrito.model";

export class Pedido{
    constructor(
        idPedido:number, 
        total:number, 
        descripcion:string, 
        direccion:string, 
        nombreCliente:string, 
        carrito:Carrito[],
        email:string = ""
    ){
        this.idPedido=idPedido;
        this.total=total;
        this.descripcion=descripcion;
        this.direccion=direccion;
        this.nombreCliente=nombreCliente;
        this.carrito=carrito;
        this.email=email;
    }

    idPedido:number=0;
    total:number=0;
    descripcion:string="";
    direccion:string="";
    nombreCliente:string="";
    carrito: Carrito[]=[];
    email:string="";
}

