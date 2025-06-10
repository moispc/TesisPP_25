from django.test import TestCase, Client
from django.urls import reverse
from rest_framework.test import APIClient
from rest_framework import status
from appUSERS.models import Usuario
import json

class RegistroLoginTests(TestCase):
    """
    Pruebas para las vistas de registro y login
    """
    
    def setUp(self):
        self.client = APIClient()
        self.register_url = reverse('appUSERS:register')
        self.login_url = reverse('appUSERS:login')
        
        # Datos para registro y login
        self.valid_user_data = {
            'email': 'test@example.com',
            'password': 'testpassword123',
            'nombre': 'Usuario',
            'apellido': 'Test',
            'telefono': '1234567890'
        }
        
        # Usuario pre-creado para pruebas de login
        self.test_user = Usuario.objects.create_user(
            email='existing@example.com',
            password='existingpassword123',
            nombre='Existing',
            apellido='User',
            telefono='9876543210'
        )
    
    def test_registro_usuario_exitoso(self):
        """Verifica que el registro de usuario funciona correctamente"""
        response = self.client.post(
            self.register_url,
            data=json.dumps(self.valid_user_data),
            content_type='application/json'
        )
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(Usuario.objects.filter(email='test@example.com').exists())
    
    def test_login_exitoso(self):
        """Verifica que el login funciona correctamente con credenciales válidas"""
        login_data = {
            'email': 'existing@example.com',
            'password': 'existingpassword123'
        }
        
        response = self.client.post(
            self.login_url,
            data=json.dumps(login_data),
            content_type='application/json'
        )
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('access', response.data)
        self.assertIn('refresh', response.data)
        self.assertEqual(response.data['email'], 'existing@example.com')
    
    def test_login_credenciales_invalidas(self):
        """Verifica que el login falla con credenciales inválidas"""
        login_data = {
            'email': 'existing@example.com',
            'password': 'wrongpassword'
        }
        
        response = self.client.post(
            self.login_url,
            data=json.dumps(login_data),
            content_type='application/json'
        )
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
