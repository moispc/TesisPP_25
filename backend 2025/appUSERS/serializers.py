from django.contrib.auth import get_user_model,authenticate
from rest_framework import serializers

class UsuarioSerializer(serializers.ModelSerializer):
    imagen_perfil_url = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    direccion = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    
    class Meta:
        model = get_user_model()

        fields = ['email','password','nombre','apellido','telefono','direccion','imagen_perfil_url']

        extra_kwargs = {'password': {'write_only': True}}
        
    def create(self, validated_data):
        return get_user_model().objects.create_user(**validated_data)
        
    def update(self, instance, validated_data):
        password = validated_data.pop('password', None)
        imagen_perfil_url = validated_data.pop('imagen_perfil_url', None)
        direccion = validated_data.pop('direccion', None)
        user = super().update(instance, validated_data)

        if imagen_perfil_url is not None:
            user.imagen_perfil_url = imagen_perfil_url
            user.save()
            
        if direccion is not None:
            user.direccion = direccion
            user.save()

        if password:
            user.set_password(password)
            user.save()

        return user

class AuthTokenSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password =serializers.CharField(style ={'input_type' : 'password'}, write_only=True)

    def validate(self,data):
        email = data.get('email')
        password = data.get('password')
        user = authenticate(
            request = self.context.get('request'),
            username = email,
            password = password
        )

        if not user:
            raise serializers.ValidationError('No se pudo Autenticar', code = 'autorizacion')
        
        data['user'] = user
        return data