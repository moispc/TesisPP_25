export class DetallePedido{
    constructor(id_usuario:number, idPedido:number, idProducto:number, cantidad:number, direccion:string){
        this.idPedido=idPedido,
        this.id_producto=idProducto,
        this.cantidad=cantidad,
        this.direccion=direccion,
        this.id_usuario=id_usuario

    }
    id_usuario:number=0;
    idPedido:number=0;
    id_producto:number=0;
    cantidad:number=1;
    direccion:string='';
    
    
}