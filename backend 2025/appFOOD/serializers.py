from rest_framework import serializers
from .models import Producto, CategoriaProducto

class ProductoSerializer(serializers.ModelSerializer):
    class Meta:
            model = Producto
            fields = '__all__'

class CategoriaProductoSerializer(serializers.ModelSerializer):
    class Meta:
            model = CategoriaProducto
            fields = '__all__'
