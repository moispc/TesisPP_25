from rest_framework import serializers
from .models import Carrito, DetallePedido, Pedido

class CarritoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Carrito
        fields = ['id', 'producto', 'cantidad', 'usuario']

class DetallePedidoSerializer(serializers.ModelSerializer):
    class Meta:
        model = DetallePedido
        fields = ["cantidad_productos", "precio_producto", "subtotal"]

class ModificarCantidadSerializer(serializers.Serializer):
    cantidad = serializers.IntegerField(min_value=1)
