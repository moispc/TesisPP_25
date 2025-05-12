from django.db import models
from appUSERS.models import Usuario
from appFOOD.models import Producto
from django.conf import settings

class Pedido(models.Model):
    id_pedidos = models.AutoField(primary_key=True)
    id_usuario = models.ForeignKey(Usuario, models.DO_NOTHING)
    fecha_pedido = models.DateField(null=True)  
    hora_pedido = models.TimeField(null=True)  
    direccion_entrega = models.CharField(max_length=100, null=True)
    estado = models.CharField(max_length=50, default='pendiente', null=True)  

    class Meta:
        managed = True
        db_table = 'pedido'
        verbose_name = 'Pedido'
        verbose_name_plural = 'Pedidos'
    def __unicode__(self):
        return self.id_pedidos
    #def __str__(self):
    #    return self.id_pedidos 

class Carrito(models.Model):
    producto = models.ForeignKey(Producto, on_delete=models.CASCADE, related_name='carrito_producto')
    cantidad = models.PositiveIntegerField(default=1)
    usuario = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    comprado = models.BooleanField(default=False)
    id_pedido = models.ForeignKey(Pedido, on_delete=models.CASCADE)
    class Meta:
        db_table = 'carrito'
    
class DetallePedido(models.Model):
    id_detalle = models.AutoField(primary_key=True)  
    id_pedido = models.ForeignKey(Pedido, models.DO_NOTHING, related_name='detalles')
    id_producto = models.ForeignKey(Producto, models.DO_NOTHING)
    cantidad_productos = models.IntegerField(null=True)  
    precio_producto = models.FloatField()
    subtotal = models.FloatField(null=True)  
    class Meta:
        managed = True
        db_table = 'detalle_pedido'
        verbose_name = 'Detallepedido'
        verbose_name_plural = 'Detallepedidos'
    def __unicode__(self):
        return self.id_detalle
    #def __str__(self):
    #    return self.id_detalle