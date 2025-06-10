from django.test import TestCase
from appCART.models import Pedido, DetallePedido, Carrito
from appUSERS.models import Usuario
from appFOOD.models import Producto, CategoriaProducto
from decimal import Decimal
from datetime import date

class CartModelTests(TestCase):
    """
    Pruebas unitarias para los modelos de carrito y pedidos
    """
    
    def setUp(self):
        # Crear usuario
        self.usuario = Usuario.objects.create_user(
            email='cliente@example.com',
            password='password123',
            nombre='Cliente',
            apellido='Test',
            telefono='1234567890',
            direccion='Calle Falsa 123'
        )
        
        # Crear categoría
        self.categoria = CategoriaProducto.objects.create(
            nombre_categoria='Hamburguesas',
            descripcion='Hamburguesas clásicas'
        )
        
        # Crear producto
        self.producto = Producto.objects.create(
            nombre_producto='Hamburguesa clásica',
            descripcion='Hamburguesa con queso, lechuga y tomate',
            precio=500.0,
            stock=10,
            imageURL='hamburguesa.jpg',
            id_categoria=self.categoria
        )
        
        # Crear pedido
        self.pedido = Pedido.objects.create(
            id_usuario=self.usuario,
            fecha_pedido=date.today(),
            direccion_entrega=self.usuario.direccion,
            estado='Pendiente'
        )
        
        # Crear detalle de pedido
        self.detalle_pedido = DetallePedido.objects.create(
            id_pedido=self.pedido,
            id_producto=self.producto,
            cantidad_productos=2,
            precio_producto=self.producto.precio,
            subtotal=self.producto.precio * 2,
            direccion_entrega=self.pedido.direccion_entrega
        )
        
        # Crear carrito
        self.carrito = Carrito.objects.create(
            producto=self.producto,
            cantidad=2,
            usuario=self.usuario,
            comprado=False,
            id_pedido=self.pedido
        )
    
    def test_creacion_pedido(self):
        """Verifica que un pedido se crea correctamente"""
        self.assertEqual(self.pedido.id_usuario, self.usuario)
        self.assertEqual(self.pedido.fecha_pedido, date.today())
        self.assertEqual(self.pedido.direccion_entrega, 'Calle Falsa 123')
        self.assertEqual(self.pedido.estado, 'Pendiente')
    
    def test_creacion_detalle_pedido(self):
        """Verifica que un detalle de pedido se crea correctamente y calcula el subtotal"""
        self.assertEqual(self.detalle_pedido.id_pedido, self.pedido)
        self.assertEqual(self.detalle_pedido.id_producto, self.producto)
        self.assertEqual(self.detalle_pedido.cantidad_productos, 2)
        self.assertEqual(self.detalle_pedido.precio_producto, 500.0)
        self.assertEqual(self.detalle_pedido.subtotal, 1000.0)
        self.assertEqual(self.detalle_pedido.direccion_entrega, 'Calle Falsa 123')
    
    def test_modificar_estado_pedido(self):
        """Verifica que se puede modificar el estado de un pedido"""
        self.pedido.estado = 'Aprobado'
        self.pedido.save()
        
        pedido_actualizado = Pedido.objects.get(pk=self.pedido.id_pedidos)
        self.assertEqual(pedido_actualizado.estado, 'Aprobado')
        
        # Cambiar a entregado
        pedido_actualizado.estado = 'Entregado'
        pedido_actualizado.save()
        
        pedido_final = Pedido.objects.get(pk=self.pedido.id_pedidos)
        self.assertEqual(pedido_final.estado, 'Entregado')
