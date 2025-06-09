from rest_framework import serializers
from .models import Carrito, DetallePedido, Pedido

class CarritoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Carrito
        fields = ['id', 'producto', 'cantidad', 'usuario']

class DetallePedidoSerializer(serializers.ModelSerializer):
    id_producto = serializers.ReadOnlyField(source='id_producto.id_producto')
    nombre_producto = serializers.ReadOnlyField(source='id_producto.nombre_producto')
    
    class Meta:
        model = DetallePedido
        fields = ["id_detalle", "id_producto", "nombre_producto", "cantidad_productos", "precio_producto", "subtotal", "direccion_entrega"]

class ModificarCantidadSerializer(serializers.Serializer):
    cantidad = serializers.IntegerField(min_value=1)
