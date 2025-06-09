import logging
import requests
import mercadopago
from django.conf import settings
from payment_service.models import PaymentRequest, PaymentNotification
from decimal import Decimal

logger = logging.getLogger('payment_service')

class CartService:
    """
    Servicio para interactuar con el carrito del usuario en el backend principal
    """
    @staticmethod
    def get_cart(user_token):
        """
        Obtiene el carrito del usuario desde el backend principal
        """
        try:
            # Validación básica del token
            if not user_token:
                logger.error("Token de usuario vacío o no proporcionado")
                return None
            
            # Modo de prueba para debugging
            if settings.DEBUG and "test" in user_token.lower():
                logger.info(f"Usando datos de prueba para el carrito con token: {user_token[:10]}...")
                return {
                    "productos": [
                        {
                            "id": 1,
                            "nombre": "Producto de Prueba 1",
                            "descripcion": "Descripción del producto 1",
                            "precio": 100.50,
                            "cantidad": 2
                        },
                        {
                            "id": 2,
                            "nombre": "Producto de Prueba 2",
                            "descripcion": "Descripción del producto 2",
                            "precio": 200.75,
                            "cantidad": 1
                        }
                    ],
                    "total": 401.75
                }
            
            url = f"{settings.MAIN_BACKEND_URL}/appCART/ver/"
            
            # Determinar el formato del token (JWT o Token clásico)
            if user_token.startswith('eyJ'):  # Formato típico de JWT
                headers = {"Authorization": f"Bearer {user_token}"}
                logger.info("Usando formato de token JWT (Bearer)")
            else:
                headers = {"Authorization": f"Token {user_token}"}
                logger.info("Usando formato de token clásico")
            logger.info(f"Consultando carrito en: {url} con token: {user_token[:5]}...")
            response = requests.get(url, headers=headers, timeout=10)
            
            if response.status_code == 200:
                response_data = response.json()
                
                # Si la respuesta es un array, convertirla al formato esperado
                if isinstance(response_data, list):
                    logger.info(f"Respuesta en formato de lista con {len(response_data)} productos")
                    cart_data = {
                        "productos": response_data,
                        "total": sum(float(item.get("precio", 0)) * int(item.get("cantidad", 0)) for item in response_data)
                    }
                    logger.info(f"Carrito convertido a formato interno con total: {cart_data['total']}")
                    return cart_data
                else:
                    logger.info(f"Respuesta en formato de objeto")
                    return response_data
            else:
                logger.error(f"Error al obtener el carrito: {response.status_code} - {response.text}")
                return None
        except Exception as e:
            logger.exception(f"Error al consultar el carrito: {str(e)}")
            return None

    @staticmethod
    def confirm_order(user_token, payment_id):
        """
        Confirma el pedido en el backend principal
        """
        try:
            url = f"{settings.MAIN_BACKEND_URL}/appCART/confirmar/"
            
            # Determinar el formato del token (JWT o Token clásico)
            if user_token.startswith('eyJ'):  # Formato típico de JWT
                headers = {"Authorization": f"Bearer {user_token}"}
                logger.info("Usando formato de token JWT (Bearer) para confirmar pedido")
            else:
                headers = {"Authorization": f"Token {user_token}"}
                logger.info("Usando formato de token clásico para confirmar pedido")
                
            data = {"payment_id": payment_id}
            
            logger.info(f"Confirmando pedido en: {url}")
            response = requests.post(url, json=data, headers=headers, timeout=10)
            
            if response.status_code in [200, 201]:
                return True, response.json()
            else:
                logger.error(f"Error al confirmar el pedido: {response.status_code} - {response.text}")
                return False, {"error": response.text}
        except Exception as e:
            logger.exception(f"Error al confirmar el pedido: {str(e)}")
            return False, {"error": str(e)}

class MercadoPagoService:
    """
    Servicio para interactuar con la API de Mercado Pago
    """
    def __init__(self):
        self.sdk = mercadopago.SDK(settings.MERCADOPAGO_ACCESS_TOKEN)
    
    def create_preference(self, items, external_reference, payer_email=None, notification_url=None, back_urls=None):
        """
        Crea una preferencia de pago en Mercado Pago y verifica que esté activa antes de devolver la URL
        """
        try:
            # Construir los datos de la preferencia
            preference_data = {
                "items": items,
                "external_reference": str(external_reference),
                "back_urls": back_urls if back_urls else {
                    "success": "https://ispcfood.netlify.app/exito",
                    "failure": "https://ispcfood.netlify.app/error",
                    "pending": "https://ispcfood.netlify.app/pendiente"
                },
                "auto_return": "approved"
            }
            # Solo agregar notification_url si es una URL pública válida (no localhost ni 127.0.0.1)
            if notification_url and not (notification_url.startswith('http://127.0.0.1') or notification_url.startswith('http://localhost')):
                preference_data["notification_url"] = notification_url
            else:
                logger.info(f"No se envía notification_url a Mercado Pago por ser local: {notification_url}")
            # Agregar datos del comprador si están disponibles
            if payer_email:
                preference_data["payer"] = {
                    "email": payer_email
                }
            logger.info(f"Creando preferencia de pago: {preference_data}")
            preference_response = self.sdk.preference().create(preference_data)
            logger.info(f"Respuesta cruda de Mercado Pago al crear preferencia: {preference_response}")
            # Si hay error explícito, loguear y devolver None
            if preference_response.get('status') != 201:
                logger.error(f"Error al crear preferencia: status {preference_response.get('status')}, detalle: {preference_response.get('response')}")
                return None
            pref = preference_response["response"]
            # Verificar estado de la preferencia SOLO si el campo status existe
            pref_id = pref.get("id")
            if pref_id:
                status_resp = self.sdk.preference().get(pref_id)
                status_value = status_resp["response"].get("status")
                if status_value is not None and status_value != "active":
                    logger.error(f"Preferencia {pref_id} no está activa: {status_value}")
                    return None
            return pref
        except Exception as e:
            logger.exception(f"Error al crear preferencia de pago: {str(e)}")
            return None

    def process_cart_to_items(self, cart_data):
        """
        Procesa los datos del carrito y los convierte al formato requerido por Mercado Pago
        """
        try:
            if not cart_data:
                logger.error("Datos del carrito inválidos o vacíos")
                return []
            
            # Si es una lista (formato de array del backend principal)
            if isinstance(cart_data, list):
                items = []
                for producto in cart_data:
                    item = {
                        "id": str(producto.get("id", "")),
                        "title": producto.get("producto", "Producto"),
                        "description": producto.get("imageURL", ""),
                        "quantity": int(producto.get("cantidad", 1)),
                        "unit_price": float(producto.get("precio", 0)),
                        "currency_id": "ARS"  # Ajustar según el país
                    }
                    items.append(item)
                return items
            
            # Si es un diccionario con "productos" (formato actual esperado)
            elif isinstance(cart_data, dict) and "productos" in cart_data:
                items = []
                for producto in cart_data.get("productos", []):
                    item = {
                        "id": str(producto.get("id")),
                        "title": producto.get("nombre", "Producto"),
                        "description": producto.get("descripcion", ""),
                        "quantity": int(producto.get("cantidad", 1)),
                        "unit_price": float(producto.get("precio", 0)),
                        "currency_id": "ARS"  # Ajustar según el país
                    }
                    items.append(item)
                return items
            else:
                logger.error("Formato de carrito no reconocido")
                return []
        except Exception as e:
            logger.exception(f"Error al procesar carrito a items: {str(e)}")
            return []

    def get_payment(self, payment_id):
        """
        Consulta el estado de un pago en Mercado Pago
        """
        try:
            logger.info(f"Consultando pago {payment_id}")
            payment_response = self.sdk.payment().get(payment_id)
            
            if payment_response["status"] == 200:
                return payment_response["response"]
            else:
                logger.error(f"Error al consultar pago: {payment_response['status']}")
                return None
        except Exception as e:
            logger.exception(f"Error al consultar pago: {str(e)}")
            return None
