from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .models import User2FA
from .serializers import Setup2FASerializer, Verify2FASerializer, AuthorizePurchaseSerializer
import pyotp
import qrcode
import base64
from io import BytesIO
import logging

logger = logging.getLogger(__name__)

# Añadimos una clase para comprobar el estado del servicio
class HealthCheckView(APIView):
    def get(self, request):
        return Response({"status": "ok", "service": "2FA Backend", "version": "1.0"})
    
    def post(self, request):
        return Response({
            "status": "ok",
            "service": "2FA Backend", 
            "version": "1.0",
            "received_data": request.data
        })

class Setup2FAView(APIView):
    def post(self, request):
        email = request.data.get('email')
        if not email:
            return Response({'error': 'Email requerido'}, status=400)
        user2fa, created = User2FA.objects.get_or_create(email=email)
        if not user2fa.secret:
            user2fa.secret = pyotp.random_base32()
            user2fa.save()
        totp = pyotp.TOTP(user2fa.secret)
        uri = totp.provisioning_uri(name=email, issuer_name="Back2FA")
        qr = qrcode.make(uri)
        buffer = BytesIO()
        qr.save(buffer, format="PNG")
        img_str = base64.b64encode(buffer.getvalue()).decode()
        return Response({
            'email': email,
            'secret': user2fa.secret,
            'qr': f"data:image/png;base64,{img_str}"
        })

class Verify2FAView(APIView):
    def post(self, request):
        serializer = Verify2FASerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        email = serializer.validated_data['email']
        code = serializer.validated_data['code']
        try:
            user2fa = User2FA.objects.get(email=email)
        except User2FA.DoesNotExist:
            return Response({'error': 'Usuario no tiene 2FA configurado'}, status=404)
        totp = pyotp.TOTP(user2fa.secret)
        if totp.verify(code):
            user2fa.enabled = True
            user2fa.save()
            return Response({'verified': True})
        return Response({'verified': False}, status=400)

class AuthorizePurchaseView(APIView):
    def post(self, request):
        logger.info(f"POST /api/2fa/authorize/ - data: {request.data}")
        try:
            serializer = AuthorizePurchaseSerializer(data=request.data)
            
            # Validación más detallada para capturar errores específicos
            if not serializer.is_valid():
                logger.error(f"Error de validación: {serializer.errors}")
                return Response({'error': 'Datos inválidos', 'detalles': serializer.errors}, status=400)
            
            email = serializer.validated_data['email']
            monto = serializer.validated_data['monto']
            titular = serializer.validated_data['titular']
            code = serializer.validated_data.get('code')
            
            logger.info(f"AuthorizePurchase: email={email}, monto={monto}, titular={titular}, code={code}")
            
            # Verificar si el usuario existe o crear automáticamente
            user2fa, created = User2FA.objects.get_or_create(email=email)
            if created:
                logger.info(f"Se creó un nuevo usuario 2FA: {email}")
                user2fa.secret = pyotp.random_base32()
                user2fa.save()
            
            requiere_2fa = False
            motivo = []
            
            if monto > 50000:
                requiere_2fa = True
                motivo.append('monto')
            
            # Suponiendo que el nombre del usuario está en el email (ajustar según tu lógica real)
            if titular.strip().lower() not in email.strip().lower():
                requiere_2fa = True
                motivo.append('titular')
            
            if requiere_2fa:
                if not user2fa.enabled:
                    logger.info(f"2FA requerido pero no configurado para {email}")
                    return Response({'requiere_2fa': True, 'motivo': motivo, 'configurado': False}, status=200)
                
                if not code:
                    logger.info(f"2FA requerido, esperando código para {email}")
                    return Response({'requiere_2fa': True, 'motivo': motivo, 'configurado': True}, status=200)
                
                totp = pyotp.TOTP(user2fa.secret)
                if not totp.verify(code):
                    logger.warning(f"Código 2FA inválido para {email}")
                    return Response({'error': 'Código 2FA inválido'}, status=400)
                
                logger.info(f"Compra autorizada con 2FA para {email}")
                return Response({'autorizado': True, 'motivo': motivo})
            
            logger.info(f"Compra autorizada sin 2FA para {email}")
            return Response({'autorizado': True, 'motivo': motivo})
        
        except Exception as e:
            logger.error(f"Error inesperado: {str(e)}", exc_info=True)
            return Response({'error': 'Error interno del servidor'}, status=500)
