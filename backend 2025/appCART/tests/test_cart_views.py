from django.test import TestCase
from django.urls import reverse
from rest_framework.test import APIClient
from rest_framework import status
from appUSERS.models import Usuario
from appCART.models import Pedido, Carrito, DetallePedido
from appFOOD.models import Producto, CategoriaProducto
from datetime import date
import json

class CartViewTests(TestCase):
    """
    Pruebas unitarias para las vistas de carrito
    """
    
    def setUp(self):
        # Crear cliente API
        self.client = APIClient()
        
        # Crear usuario para autenticación
        self.usuario = Usuario.objects.create_user(
            email='cliente@example.com',
            password='password123',
            nombre='Cliente',
            apellido='Test',
            telefono='1234567890',
            direccion='Calle Falsa 123'
        )
        
        # Autenticar usuario
        self.client.force_authenticate(user=self.usuario)
        
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
        
        # URLs para las vistas
        self.add_to_cart_url = reverse('appCART:agregar-producto', args=[self.producto.id_producto])
        self.view_cart_url = reverse('appCART:ver-carrito')
        self.confirm_order_url = reverse('appCART:confirmar-pedido')
        
    def test_agregar_producto_al_carrito(self):
        """Prueba que se puede agregar un producto al carrito"""
        data = {
            'cantidad': 2,
            'direccion': 'Calle Falsa 123'
        }
        
        response = self.client.post(self.add_to_cart_url, data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('message', response.data)
        self.assertEqual(response.data['message'], 'Producto agregado al carrito')
        
        # Verificar que se creó el detalle de pedido y carrito
        self.assertTrue(DetallePedido.objects.filter(id_pedido=self.pedido, id_producto=self.producto).exists())
        self.assertTrue(Carrito.objects.filter(producto=self.producto, usuario=self.usuario).exists())
    
    def test_ver_carrito(self):
        """Prueba que se puede ver el carrito del usuario"""
        # Crear un ítem en el carrito primero
        Carrito.objects.create(
            producto=self.producto,
            cantidad=2,
            usuario=self.usuario,
            comprado=False,
            id_pedido=self.pedido
        )
        
        response = self.client.get(self.view_cart_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(isinstance(response.data, list))
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['producto'], 'Hamburguesa clásica')
        self.assertEqual(response.data[0]['cantidad'], 2)
    
    def test_confirmar_pedido_vacio(self):
        """Prueba que no se puede confirmar un pedido vacío"""
        # Asegurarse que el carrito está vacío
        Carrito.objects.filter(usuario=self.usuario).delete()
        
        response = self.client.post(self.confirm_order_url)
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('error', response.data)
