from django.test import TestCase
from appUSERS.models import Usuario

class UsuarioModelTests(TestCase):
    """
    Pruebas unitarias para el modelo Usuario en appUSERS
    """
    
    def setUp(self):
        # Crear un usuario para las pruebas
        self.usuario = Usuario.objects.create_user(
            email='test@example.com',
            password='testpassword123',
            nombre='Usuario',
            apellido='Test',
            telefono='1234567890'
        )
    
    def test_create_usuario(self):
        """Verifica que un usuario se crea correctamente con todos sus campos"""
        self.assertEqual(self.usuario.email, 'test@example.com')
        self.assertEqual(self.usuario.nombre, 'Usuario')
        self.assertEqual(self.usuario.apellido, 'Test')
        self.assertEqual(self.usuario.telefono, '1234567890')
        self.assertTrue(self.usuario.is_active)
        self.assertFalse(self.usuario.is_staff)
        self.assertFalse(self.usuario.is_superuser)
    
    def test_usuario_str_representation(self):
        """Verifica que el método __str__ del modelo devuelve el nombre del usuario"""
        self.assertEqual(str(self.usuario), 'Usuario')
    
    def test_desactivar_usuario(self):
        """Verifica que se puede desactivar un usuario y guardar su email anterior"""
        email_original = self.usuario.email
        self.usuario.is_active = False
        self.usuario.last_email = email_original
        self.usuario.email = f"deleted_{self.usuario.id_usuario}@example.com"
        self.usuario.save()
        
        # Verificar que el usuario está desactivado y tiene el email anterior guardado
        self.assertFalse(self.usuario.is_active)
        self.assertEqual(self.usuario.last_email, email_original)
        self.assertEqual(self.usuario.email, f"deleted_{self.usuario.id_usuario}@example.com")
