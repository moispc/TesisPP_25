from rest_framework import serializers
from .models import PaymentRequest, PaymentNotification

class PaymentRequestSerializer(serializers.ModelSerializer):
    class Meta:
        model = PaymentRequest
        fields = ['id', 'preference_id', 'init_point', 'status', 'total_amount', 'created_at']
        read_only_fields = ['id', 'preference_id', 'init_point', 'status', 'total_amount', 'created_at']

class CreatePreferenceSerializer(serializers.Serializer):
    user_token = serializers.CharField(max_length=255, required=True,
                                      help_text="Token de autenticación del usuario")
    email = serializers.EmailField(required=False, allow_blank=True,
                                  help_text="Email del usuario (opcional)")

class WebhookNotificationSerializer(serializers.Serializer):
    topic = serializers.CharField(required=False, allow_blank=True)
    id = serializers.CharField(required=False, allow_blank=True)
    data = serializers.JSONField(required=False)
    
    # Campos específicos para IPN V1
    resource = serializers.CharField(required=False, allow_blank=True)
    action = serializers.CharField(required=False, allow_blank=True)
    
    # Campo para manejar las notificaciones directas
    type = serializers.CharField(required=False, allow_blank=True)
