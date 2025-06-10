from django.test import TestCase, RequestFactory
from django.urls import reverse
from unittest.mock import patch, MagicMock
from payment_service.views import CreatePreferenceView
from payment_service.models import PaymentRequest
from rest_framework.test import APIClient
from rest_framework import status
import json

class PaymentViewsTests(TestCase):
    """
    Pruebas para las vistas relacionadas con pagos
    """
    
    def setUp(self):
        self.client = APIClient()
        self.factory = RequestFactory()
        self.create_preference_url = reverse('payment_service:create-preference')
        self.cart_data = {
            "productos": [
                {
                    "id": 1,
                    "nombre": "Hamburguesa Clásica",
                    "descripcion": "Hamburguesa con queso y lechuga",
                    "precio": 500.0,
                    "cantidad": 2
                }
            ],
            "total": 1000.0
        }
        self.preference_data = {
            "id": "test_preference_id",
            "init_point": "https://www.mercadopago.com/init_point",
            "sandbox_init_point": "https://sandbox.mercadopago.com/init_point",
            "items": [
                {
                    "id": "1",
                    "title": "Hamburguesa Clásica",
                    "description": "Hamburguesa con queso y lechuga",
                    "quantity": 2,
                    "unit_price": 500.0,
                    "currency_id": "ARS"
                }
            ]
        }
    
    @patch('payment_service.views.CartService')
    @patch('payment_service.views.MercadoPagoService')
    def test_create_preference_success(self, mock_mp_service, mock_cart_service):
        """Prueba la creación exitosa de una preferencia de pago"""
        # Configurar mocks
        mock_cart_service.get_cart.return_value = self.cart_data
        
        # Configurar el mock de MercadoPagoService
        mock_mp_service_instance = MagicMock()
        mock_mp_service.return_value = mock_mp_service_instance
        
        # Configurar el proceso de items y la creación de preferencia
        mock_mp_service_instance.process_cart_to_items.return_value = self.preference_data["items"]
        mock_mp_service_instance.create_preference.return_value = self.preference_data
        
        # Realizar la solicitud
        token = "test_token_123"
        response = self.client.post(
            self.create_preference_url,
            data=json.dumps({"user_token": token, "email": "cliente@example.com"}),
            content_type="application/json"
        )
        
        # Verificar respuesta
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data["preference_id"], "test_preference_id")
        self.assertEqual(response.data["init_point"], "https://www.mercadopago.com/init_point")
        
        # Verificar que se llamó a los servicios correctamente
        mock_cart_service.get_cart.assert_called_once_with(token)
        mock_mp_service_instance.process_cart_to_items.assert_called_once_with(self.cart_data)
        
        # Verificar que se guardó el PaymentRequest
        self.assertTrue(PaymentRequest.objects.filter(preference_id="test_preference_id").exists())
    
    @patch('payment_service.views.CartService')
    def test_create_preference_empty_cart(self, mock_cart_service):
        """Prueba que se maneja correctamente un carrito vacío"""
        # Configurar el carrito vacío
        mock_cart_service.get_cart.return_value = {"productos": [], "total": 0}
        
        # Realizar la solicitud
        token = "test_token_123"
        response = self.client.post(
            self.create_preference_url,
            data=json.dumps({"user_token": token}),
            content_type="application/json"
        )
        
        # Verificar respuesta de error
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "El carrito está vacío")
    
    @patch('payment_service.views.CartService')
    def test_create_preference_cart_service_error(self, mock_cart_service):
        """Prueba que se maneja correctamente un error al obtener el carrito"""
        # Configurar error en el servicio de carrito
        mock_cart_service.get_cart.return_value = None
        
        # Realizar la solicitud
        token = "test_token_123"
        response = self.client.post(
            self.create_preference_url,
            data=json.dumps({"user_token": token}),
            content_type="application/json"
        )
        
        # Verificar respuesta de error
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("error", response.data)
