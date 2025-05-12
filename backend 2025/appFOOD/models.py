from django.db import models

# Create your models here.

class CategoriaProducto(models.Model):
    id_categoria = models.AutoField(primary_key=True)  
    nombre_categoria = models.CharField(max_length=45)  
    descripcion = models.CharField(max_length=45)

    class Meta:
        managed = True
        db_table = 'categoria_producto'
        verbose_name = 'Categoriaproducto'
        verbose_name_plural = 'Categoriaproductos'
    def __unicode__(self):
        return self.nombre_categoria
    def __str__(self):
        return self.nombre_categoria


class Producto(models.Model):
    id_producto = models.AutoField(primary_key=True)  
    nombre_producto = models.CharField(max_length=45)  
    descripcion = models.CharField(max_length=200)
    precio = models.FloatField()
    stock = models.IntegerField(default = 0 )
    imageURL = models.CharField( max_length=100, null=True )
    id_categoria = models.ForeignKey(CategoriaProducto, models.DO_NOTHING)

    class Meta:
        managed = True
        db_table = 'producto'
        verbose_name = 'Producto'
        verbose_name_plural = 'Productos'
    def __unicode__(self):
        return self.nombre_producto
    def __str__(self):
        return self.nombre_producto


