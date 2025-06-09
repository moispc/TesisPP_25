from rest_framework import generics, authentication, permissions, status
from appCART.models import Carrito, Pedido
from appUSERS.serializers import UsuarioSerializer, AuthTokenSerializer 
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework_simplejwt.tokens import RefreshToken


class CreateUsuarioView(generics.CreateAPIView):
    serializer_class = UsuarioSerializer
    
    def create(self, request, *args, **kwargs):
        # Obtener el correo electrónico del nuevo registro
        email = request.data.get('email')
        
        if email:            # Buscar si existe un usuario inactivo con este mismo correo como último correo
            from appUSERS.models import Usuario
            try:
                # Usar filter en vez de get para manejar el caso de múltiples usuarios con el mismo email
                inactive_users = Usuario.objects.filter(last_email=email, is_active=False)
                
                if inactive_users.exists():
                    # Tomar el primer usuario inactivo encontrado (o podríamos usar el más reciente)
                    inactive_user = inactive_users.first()
                    
                    # Si lo encontramos, vamos a reactivar al usuario y actualizar sus datos
                    inactive_user.is_active = True
                    inactive_user.email = email  # Restauramos el email original
                
                # Actualizamos los datos con la nueva información
                if 'nombre' in request.data:
                    inactive_user.nombre = request.data['nombre']
                if 'apellido' in request.data:
                    inactive_user.apellido = request.data['apellido']
                if 'telefono' in request.data:
                    inactive_user.telefono = request.data['telefono']
                if 'direccion' in request.data:
                    inactive_user.direccion = request.data['direccion']
                if 'imagen_perfil_url' in request.data:
                    inactive_user.imagen_perfil_url = request.data['imagen_perfil_url']
                
                # Si hay contraseña, la actualizamos
                if 'password' in request.data:
                    inactive_user.set_password(request.data['password'])
                
                # Guardamos los cambios
                inactive_user.save()
                
                # Serializamos el usuario reactivado para devolverlo
                serializer = self.get_serializer(inactive_user)
                return Response(serializer.data, status=status.HTTP_201_CREATED)
                
            except Usuario.DoesNotExist:
                # Si no existe un usuario inactivo con ese email, continuamos normal
                pass
        
        # Proceso normal si no hay reactivación
        return super().create(request, *args, **kwargs)


class RetrieveUpdateUsuarioView(generics.RetrieveUpdateAPIView):
    serializer_class = UsuarioSerializer
    authentication_classes = [authentication.TokenAuthentication]
    permission_classes = [permissions.IsAuthenticated]
    
    def get_object(self):
        return self.request.user



class CreateTokenView(APIView):
    serializer_class = AuthTokenSerializer

    def post(self, request, *args, **kwargs):
        serializer = self.serializer_class(data=request.data, context={'request': request})
        serializer.is_valid(raise_exception=True)
        user = serializer.validated_data['user']
        
        refresh = RefreshToken.for_user(user)
        access_token = refresh.access_token
        return Response({
            'email': user.email,
            'user_id': user.pk, 
            'refresh': str(refresh),
            'access': str(access_token),
            'nombre': user.nombre, 
            'apellido': user.apellido,
            'telefono': user.telefono,
            'admin': user.is_superuser,
            'imagen_perfil_url': user.imagen_perfil_url
        }, status=status.HTTP_200_OK)

class LogoutView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):       
        try:           
            return Response({"detalle": "Logout Satisfactorio."}, status=status.HTTP_200_OK)
        except Exception as e:
            
            return Response({"detalle": "Error inesperado."}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class UpdateProfileView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def put(self, request):
        user = request.user
        serializer = UsuarioSerializer(user, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class DeleteProfileView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def delete(self, request):
        user = request.user
        
        # Guardamos el email original para referencia futura
        original_email = user.email
        
        # En lugar de eliminar físicamente, desactivamos la cuenta (borrado lógico)
        user.is_active = False
        
        # Guardar el email original en un campo adicional para una posible reactivación
        user.last_email = original_email
        
        # Cambiamos el email actual para permitir nuevos registros con ese mismo email
        user.email = f"deleted_{user.id_usuario}@example.com"  # Evitar conflictos al registrarse de nuevo
        
        # Opcional: podemos limpiar algunos datos sensibles pero manteniendo el registro
        # user.nombre = "Usuario"
        # user.apellido = "Eliminado"
        # user.telefono = ""
        # user.direccion = ""
        # user.imagen_perfil_url = ""
        
        # Guardamos los cambios
        user.save()

        return Response({
            "detalle": "Cuenta desactivada correctamente. Puedes volver a registrarte en cualquier momento.",
            "email_original": original_email
        }, status=status.HTTP_200_OK)

class UpdateProfileImageView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        user = request.user
        
        # Verificar si se proporcionó una URL de imagen
        if 'imagen_perfil_url' not in request.data:
            return Response({"error": "No se proporcionó ninguna URL de imagen"}, status=status.HTTP_400_BAD_REQUEST)
        
        # Actualizar solo el campo de imagen_perfil_url
        serializer = UsuarioSerializer(user, data={'imagen_perfil_url': request.data['imagen_perfil_url']}, partial=True)
        
        if serializer.is_valid():
            serializer.save()
            
            # Devolvemos los datos actualizados incluida la URL de la imagen
            return Response({
                'mensaje': 'URL de imagen de perfil actualizada correctamente',
                'imagen_perfil_url': user.imagen_perfil_url
            }, status=status.HTTP_200_OK)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
