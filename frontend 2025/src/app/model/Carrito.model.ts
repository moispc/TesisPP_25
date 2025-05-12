export class Carrito{
  constructor(producto:string, id:number, precio:number, cantidad:number, imageURL:string, id_pedido:number){
      this.producto=producto,
      this.id=id,
      this.precio=precio,
      this.cantidad=cantidad,
      this.imageURL=imageURL,
      this.id_pedido=id_pedido
  }

  producto:string="0";
  id:number=0;
  precio:number=0;
  cantidad:number=1;
  imageURL:string="0";
  id_pedido=0;
}
