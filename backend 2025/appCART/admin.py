from django.contrib import admin

# Register your models here.
from .models import Pedido
from .models import DetallePedido
from .models import Carrito

class PedidoAdmin(admin.ModelAdmin):
    list_display = ( 'id_pedidos', 'id_usuario', 'fecha_pedido', 'hora_pedido', 'direccion_entrega')

class DetallePedidoAdmin(admin.ModelAdmin):
    list_display = ( 'id_pedido', 'id_producto', 'cantidad_productos', 'precio_producto')    

class CarritoAdmin(admin.ModelAdmin):
    list_display = ['id', 'producto', 'cantidad', 'usuario']
    list_filter = ['usuario']
    search_fields = ['producto__nombre']

admin.site.register(Carrito)
admin.site.register( Pedido, PedidoAdmin )
admin.site.register( DetallePedido, DetallePedidoAdmin )