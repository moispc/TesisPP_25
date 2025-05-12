from django.contrib import admin

from .models import CategoriaProducto
from .models import Producto


# Register your models here.

class CategoriaProductoAdmin(admin.ModelAdmin):
    list_display = ( 'nombre_categoria', 'descripcion')




class ProductoAdmin(admin.ModelAdmin):
    list_display = ( 'nombre_producto', 'descripcion', 'precio', 'id_categoria', 'stock' )
    





admin.site.register( CategoriaProducto, CategoriaProductoAdmin )

admin.site.register( Producto, ProductoAdmin )
