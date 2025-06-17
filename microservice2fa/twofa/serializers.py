from rest_framework import serializers
from .models import User2FA

class Setup2FASerializer(serializers.ModelSerializer):
    class Meta:
        model = User2FA
        fields = ['email', 'secret', 'enabled']
        read_only_fields = ['secret', 'enabled']

class Verify2FASerializer(serializers.Serializer):
    email = serializers.EmailField()
    code = serializers.CharField()

class AuthorizePurchaseSerializer(serializers.Serializer):
    email = serializers.EmailField()
    monto = serializers.FloatField()
    titular = serializers.CharField()
    code = serializers.CharField(required=False)
