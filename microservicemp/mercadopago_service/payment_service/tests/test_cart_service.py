from django.test import TestCase
from unittest.mock import patch, MagicMock
from payment_service.services import CartService
from django.conf import settings

class CartServiceTests(TestCase):
    """
    Pruebas para el servicio CartService que interactúa con el backend principal
    """
    
    def setUp(self):
        self.test_token = "test_token_1234567890"
        self.test_cart_data = {
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
    
    @patch('payment_service.services.requests.get')
    def test_get_cart_success(self, mock_get):
        # Configurar el mock para simular una respuesta exitosa
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = self.test_cart_data
        mock_get.return_value = mock_response
        
        # Llamar al método que queremos probar
        result = CartService.get_cart(self.test_token)
        
        # Verificar que se llamó a requests.get con los parámetros correctos
        expected_url = f"{settings.MAIN_BACKEND_URL}/appCART/ver/"
        mock_get.assert_called_once()
        args, kwargs = mock_get.call_args
        self.assertEqual(args[0], expected_url)
        
        # Verificar el formato del token en el header
        self.assertIn('Authorization', kwargs['headers'])
        self.assertTrue(kwargs['headers']['Authorization'].startswith('Token '))
        
        # Verificar que se devolvió el carrito correctamente
        self.assertEqual(result, self.test_cart_data)
    
    @patch('payment_service.services.requests.get')
    def test_get_cart_with_jwt_token(self, mock_get):
        # Configurar un token JWT (comienza con "ey")
        jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"
        
        # Configurar el mock para simular una respuesta exitosa
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = self.test_cart_data
        mock_get.return_value = mock_response
        
        # Llamar al método que queremos probar
        result = CartService.get_cart(jwt_token)
        
        # Verificar que se usó el formato correcto de encabezado para JWT
        args, kwargs = mock_get.call_args
        self.assertIn('Authorization', kwargs['headers'])
        self.assertTrue(kwargs['headers']['Authorization'].startswith('Bearer '))
        
        # Verificar que se devolvió el carrito correctamente
        self.assertEqual(result, self.test_cart_data)
    
    @patch('payment_service.services.requests.get')
    def test_get_cart_error(self, mock_get):
        # Configurar el mock para simular un error
        mock_response = MagicMock()
        mock_response.status_code = 404
        mock_response.text = "Carrito no encontrado"
        mock_get.return_value = mock_response
        
        # Llamar al método que queremos probar
        result = CartService.get_cart(self.test_token)
        
        # Verificar que se devuelve None cuando hay un error
        self.assertIsNone(result)
