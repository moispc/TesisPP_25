from django.test import TestCase
from unittest.mock import patch, MagicMock
from payment_service.services import MercadoPagoService
from decimal import Decimal

class MercadoPagoServiceTests(TestCase):
    """
    Pruebas para el servicio MercadoPagoService que interactúa con la API de Mercado Pago
    """
    
    def setUp(self):
        self.mp_service = MercadoPagoService()
        self.test_items = [
            {
                "id": "1",
                "title": "Hamburguesa Clásica",
                "description": "Hamburguesa con queso y lechuga",
                "quantity": 2,
                "unit_price": 500.0,
                "currency_id": "ARS"
            }
        ]
        self.external_reference = "test_reference_123"
        self.test_payer_email = "cliente@example.com"
    
    def test_process_cart_to_items_list_format(self):
        """Prueba la conversión de carrito en formato de lista a formato de items de Mercado Pago"""
        cart_data = [
            {
                "id": 1,
                "producto": "Hamburguesa Clásica",
                "imageURL": "hamburguesa.jpg",
                "cantidad": 2,
                "precio": 500.0
            }
        ]
        
        result = self.mp_service.process_cart_to_items(cart_data)
        
        self.assertEqual(len(result), 1)
        self.assertEqual(result[0]["id"], "1")
        self.assertEqual(result[0]["title"], "Hamburguesa Clásica")
        self.assertEqual(result[0]["description"], "hamburguesa.jpg")
        self.assertEqual(result[0]["quantity"], 2)
        self.assertEqual(result[0]["unit_price"], 500.0)
        self.assertEqual(result[0]["currency_id"], "ARS")
    
    def test_process_cart_to_items_dict_format(self):
        """Prueba la conversión de carrito en formato de diccionario a formato de items de Mercado Pago"""
        cart_data = {
            "productos": [
                {
                    "id": 2,
                    "nombre": "Pizza Muzarella",
                    "descripcion": "Pizza con queso muzarella",
                    "cantidad": 1,
                    "precio": 800.0
                }
            ],
            "total": 800.0
        }
        
        result = self.mp_service.process_cart_to_items(cart_data)
        
        self.assertEqual(len(result), 1)
        self.assertEqual(result[0]["id"], "2")
        self.assertEqual(result[0]["title"], "Pizza Muzarella")
        self.assertEqual(result[0]["description"], "Pizza con queso muzarella")
        self.assertEqual(result[0]["quantity"], 1)
        self.assertEqual(result[0]["unit_price"], 800.0)
        self.assertEqual(result[0]["currency_id"], "ARS")
    
    @patch('mercadopago.SDK')
    def test_create_preference_success(self, mock_sdk):
        """Prueba la creación exitosa de una preferencia de pago"""
        # Configurar el mock para el SDK
        mock_preference = MagicMock()
        mock_sdk.return_value.preference.return_value = mock_preference
        
        # Configurar respuesta de creación de preferencia
        mock_preference_response = {
            "status": 201,
            "response": {
                "id": "test_preference_id",
                "init_point": "https://www.mercadopago.com/checkout/v1/redirect?pref_id=test_preference_id",
                "collector_id": 12345,
                "items": self.test_items
            }
        }
        mock_preference.create.return_value = mock_preference_response
        
        # Configurar respuesta para obtener preferencia
        mock_preference.get.return_value = {
            "response": {
                "status": "active"
            }
        }
        
        # Llamar al método que queremos probar
        result = self.mp_service.create_preference(
            self.test_items,
            self.external_reference,
            self.test_payer_email
        )
        
        # Verificar que se llamó al SDK correctamente
        mock_preference.create.assert_called_once()
        
        # Verificar que se devolvió la preferencia correctamente
        self.assertEqual(result["id"], "test_preference_id")
        self.assertEqual(result["init_point"], "https://www.mercadopago.com/checkout/v1/redirect?pref_id=test_preference_id")
