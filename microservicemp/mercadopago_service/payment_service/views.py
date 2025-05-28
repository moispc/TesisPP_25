import logging
from decimal import Decimal
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from django.conf import settings
from django.http import HttpResponse
from django.shortcuts import redirect
from django.views import View
from django.http import HttpRequest
import requests

from .models import PaymentRequest, PaymentNotification
from .serializers import (
    PaymentRequestSerializer, 
    CreatePreferenceSerializer,
    WebhookNotificationSerializer
)
from .services import CartService, MercadoPagoService

logger = logging.getLogger('payment_service')

class CreatePreferenceView(APIView):
    """
    Endpoint para crear una preferencia de pago en Mercado Pago
    """
    
    def post(self, request, *args, **kwargs):
        # Log de datos recibidos
        logger.info(f"Datos recibidos en create-preference: {request.data}")
        
        # Validar datos de entrada
        serializer = CreatePreferenceSerializer(data=request.data)
        if not serializer.is_valid():
            logger.error(f"Datos inválidos: {serializer.errors}")
            return Response({"error": serializer.errors}, status=status.HTTP_400_BAD_REQUEST)
        
        # Extraer token del usuario
        user_token = serializer.validated_data['user_token']
        email = serializer.validated_data.get('email')
        
        logger.info(f"Token recibido: {user_token[:10]}... (truncado por seguridad)")
          # Obtener datos del carrito
        cart_service = CartService()
        cart_data = cart_service.get_cart(user_token)
        
        if not cart_data:
            logger.error(f"No se pudo obtener el carrito con el token proporcionado: {user_token[:10]}...")
            
            # Proporcionar un mensaje más específico para ayudar en la resolución del problema
            error_message = "No se pudo obtener el carrito o está vacío"
            if settings.DEBUG:
                error_message += ". Para pruebas, utiliza un token que contenga 'test' para generar un carrito de prueba, o proporciona un token válido de un usuario con sesión iniciada."
            
            return Response(
                {"error": error_message}, 
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Verificar que el carrito tenga productos
        if not cart_data.get("productos", []):
            logger.error(f"El carrito está vacío: {cart_data}")
            return Response(
                {"error": "El carrito está vacío"}, 
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Crear solicitud de pago en la base de datos
        total_amount = Decimal(str(cart_data.get("total", "0")))
        payment_request = PaymentRequest.objects.create(
            user_token=user_token,
            cart_data=cart_data,
            total_amount=total_amount,
            status="pending"
        )
        
        # Convertir carrito a formato de Mercado Pago
        mp_service = MercadoPagoService()
        items = mp_service.process_cart_to_items(cart_data)
        
        if not items:
            payment_request.status = "error"
            payment_request.save()
            return Response(
                {"error": "No se pudieron procesar los ítems del carrito"}, 
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Crear preferencia en Mercado Pago
        notification_url = f"{request.build_absolute_uri('/').rstrip('/')}/payment/webhook/"
        preference = mp_service.create_preference(
            items=items,
            external_reference=str(payment_request.id),
            payer_email=email,
            notification_url=notification_url
        )
        
        if not preference:
            payment_request.status = "error"
            payment_request.save()
            return Response(
                {"error": "Error al crear la preferencia de pago"}, 
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )
        
        # Actualizar solicitud de pago con datos de la preferencia
        payment_request.preference_id = preference.get("id")
        payment_request.init_point = preference.get("init_point")
        payment_request.save()
        
        # Devolver datos al cliente
        return Response({
            "init_point": preference.get("init_point"),
            "preference_id": preference.get("id"),
            "payment_request_id": str(payment_request.id)
        }, status=status.HTTP_201_CREATED)


class WebhookView(APIView):
    """
    Endpoint para recibir notificaciones (webhooks) de Mercado Pago
    """
    
    def post(self, request, *args, **kwargs):
        logger.info(f"Webhook recibido: {request.data}")
        
        # Validar datos de entrada
        serializer = WebhookNotificationSerializer(data=request.data)
        if not serializer.is_valid():
            logger.error(f"Datos inválidos en webhook: {serializer.errors}")
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Determinar el tipo de notificación
        if 'topic' in data and data['topic'] == 'merchant_order':
            # Notificación IPNv1 de Merchant Order
            notification_type = 'merchant_order'
            resource_id = data.get('id')
        elif 'topic' in data and data['topic'] == 'payment':
            # Notificación IPNv1 de Payment
            notification_type = 'payment'
            resource_id = data.get('id')
        elif 'resource' in data and 'topic' in data:
            # Notificación IPNv2
            notification_type = data.get('topic')
            resource_id = data.get('resource', '').split('/')[-1]
        elif 'type' in data:
            # Notificación directa de Webhook
            notification_type = data.get('type')
            resource_id = data.get('data', {}).get('id')
        else:
            logger.error(f"Tipo de notificación no reconocido: {data}")
            return Response({"error": "Tipo de notificación no reconocido"}, status=status.HTTP_400_BAD_REQUEST)
          # Guardar la notificación en la base de datos
        notification = PaymentNotification.objects.create(
            topic=notification_type,
            payment_id=resource_id if notification_type == 'payment' else None,
            raw_data=data
        )
        
        # Si es una notificación de pago, procesarla
        if notification_type == 'payment':
            self._process_payment_notification(notification)
        
        # Siempre responder con éxito para que Mercado Pago no reintente
        return HttpResponse(status=200)
    
    def _process_payment_notification(self, notification):
        """
        Procesa una notificación de pago
        """
        try:
            mp_service = MercadoPagoService()
            payment_data = mp_service.get_payment(notification.payment_id)
            
            if not payment_data:
                logger.error(f"No se pudo obtener información del pago {notification.payment_id}")
                return
            
            # Obtener la referencia externa (ID de la solicitud de pago)
            external_reference = payment_data.get('external_reference')
            if not external_reference:
                logger.error(f"Pago sin referencia externa: {notification.payment_id}")
                return
            
            # Buscar la solicitud de pago
            try:
                payment_request = PaymentRequest.objects.get(id=external_reference)
            except PaymentRequest.DoesNotExist:
                logger.error(f"No se encontró la solicitud de pago con ID {external_reference}")
                return
            
            # Vincular la notificación a la solicitud de pago
            notification.payment_request = payment_request
            notification.save()
            
            # Actualizar el estado de la solicitud de pago
            payment_status = payment_data.get('status')
            payment_request.status = payment_status
            payment_request.save()
            
            # Si el pago fue aprobado, confirmar el pedido
            if payment_status == 'approved':
                logger.info(f"Pago aprobado, confirmando pedido: {payment_request.id}")
                
                cart_service = CartService()
                success, response = cart_service.confirm_order(
                    payment_request.user_token, 
                    notification.payment_id
                )
                
                if success:
                    notification.processed = True
                    notification.save()
                    logger.info(f"Pedido confirmado correctamente: {payment_request.id}")
                else:
                    logger.error(f"Error al confirmar pedido: {response}")
            
        except Exception as e:
            logger.exception(f"Error al procesar notificación de pago: {str(e)}")

class PaymentSuccessView(View):
    """
    Vista para manejar la URL de éxito de Mercado Pago, consultar el backend principal y redirigir al frontend con el ticket
    """
    def get(self, request: HttpRequest):
        external_reference = request.GET.get('external_reference')
        payment_id = request.GET.get('payment_id')
        if external_reference:
            # Consultar al backend principal para obtener los datos del ticket
            try:
                url = f"{settings.MAIN_BACKEND_URL}/appCART/ticket/{external_reference}/"
                resp = requests.get(url, timeout=10)
                if resp.status_code == 200:
                    # Redirigir al frontend local solo a /exito
                    return redirect("http://localhost:4200/exito")
                else:
                    logger.error(f"No se pudo obtener el ticket del backend principal: {resp.status_code} - {resp.text}")
                    return redirect("http://localhost:4200/error")
            except Exception as e:
                logger.exception(f"Error al consultar el backend principal para el ticket: {str(e)}")
                return redirect("http://localhost:4200/error")
        else:
            logger.error("No se encontró external_reference en la URL de éxito")
            return redirect("http://localhost:4200/error")
