from django.test import TestCase
from appFOOD.models import Producto, CategoriaProducto

class ProductModelTests(TestCase):
    """
    Pruebas unitarias para el modelo de Producto
    """
    
    def setUp(self):
        # Crear categoría
        self.categoria1 = CategoriaProducto.objects.create(
            nombre_categoria='Hamburguesas',
            descripcion='Hamburguesas clásicas'
        )
        
        self.categoria2 = CategoriaProducto.objects.create(
            nombre_categoria='Pizzas',
            descripcion='Pizzas variadas'
        )
        
        # Crear productos
        self.producto1 = Producto.objects.create(
            nombre_producto='Hamburguesa clásica',
            descripcion='Hamburguesa con queso, lechuga y tomate',
            precio=500.0,
            stock=10,
            imageURL='hamburguesa.jpg',
            id_categoria=self.categoria1
        )
        
        self.producto2 = Producto.objects.create(
            nombre_producto='Pizza Muzarella',
            descripcion='Pizza con queso muzarella',
            precio=800.0,
            stock=5,
            imageURL='pizza.jpg',
            id_categoria=self.categoria2
        )
    
    def test_creacion_producto(self):
        """Verifica que un producto se crea correctamente"""
        self.assertEqual(self.producto1.nombre_producto, 'Hamburguesa clásica')
        self.assertEqual(self.producto1.precio, 500.0)
        self.assertEqual(self.producto1.stock, 10)
        self.assertEqual(self.producto1.id_categoria, self.categoria1)
        
        self.assertEqual(self.producto2.nombre_producto, 'Pizza Muzarella')
        self.assertEqual(self.producto2.precio, 800.0)
        self.assertEqual(self.producto2.stock, 5)
        self.assertEqual(self.producto2.id_categoria, self.categoria2)
    
    def test_producto_str_representation(self):
        """Verifica que el método __str__ del modelo devuelve el nombre del producto"""
        self.assertEqual(str(self.producto1), 'Hamburguesa clásica')
        self.assertEqual(str(self.producto2), 'Pizza Muzarella')
    
    def test_categoria_str_representation(self):
        """Verifica que el método __str__ del modelo de categoría devuelve el nombre de la categoría"""
        self.assertEqual(str(self.categoria1), 'Hamburguesas')
        self.assertEqual(str(self.categoria2), 'Pizzas')
        
    def test_actualizar_stock_producto(self):
        """Verifica que se puede actualizar el stock de un producto"""
        # Reducir stock (simular venta)
        self.producto1.stock -= 2
        self.producto1.save()
        
        producto_actualizado = Producto.objects.get(pk=self.producto1.id_producto)
        self.assertEqual(producto_actualizado.stock, 8)
        
        # Aumentar stock (simular reposición)
        producto_actualizado.stock += 5
        producto_actualizado.save()
        
        producto_final = Producto.objects.get(pk=self.producto1.id_producto)
        self.assertEqual(producto_final.stock, 13)
