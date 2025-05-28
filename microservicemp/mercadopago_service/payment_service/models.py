import uuid
from django.db import models
from django.utils import timezone

class PaymentRequest(models.Model):
    """
    Modelo para almacenar las solicitudes de pago y seguimiento
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user_token = models.CharField(max_length=255, help_text="Token de autenticación del usuario")
    preference_id = models.CharField(max_length=255, null=True, blank=True, help_text="ID de la preferencia en Mercado Pago")
    init_point = models.URLField(null=True, blank=True, help_text="URL para redirigir al usuario para el pago")
    status = models.CharField(max_length=50, default="pending", help_text="Estado del pago")
    total_amount = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True, help_text="Monto total del pago")
    cart_data = models.JSONField(null=True, blank=True, help_text="Datos del carrito del usuario")
    created_at = models.DateTimeField(default=timezone.now, help_text="Fecha de creación")
    updated_at = models.DateTimeField(auto_now=True, help_text="Fecha de actualización")
    
    def __str__(self):
        return f"Pago {self.id} - Estado: {self.status}"
    
    class Meta:
        verbose_name = "Solicitud de pago"
        verbose_name_plural = "Solicitudes de pago"
        ordering = ['-created_at']

class PaymentNotification(models.Model):
    """
    Modelo para almacenar las notificaciones recibidas de Mercado Pago
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    payment_request = models.ForeignKey(PaymentRequest, on_delete=models.CASCADE, null=True, blank=True, related_name="notifications")
    payment_id = models.CharField(max_length=255, null=True, blank=True, help_text="ID del pago en Mercado Pago")
    topic = models.CharField(max_length=50, help_text="Tipo de notificación")
    raw_data = models.JSONField(help_text="Datos completos de la notificación")
    processed = models.BooleanField(default=False, help_text="Indica si la notificación ya fue procesada")
    created_at = models.DateTimeField(default=timezone.now, help_text="Fecha de recepción")
    
    def __str__(self):
        return f"Notificación {self.id} - Tema: {self.topic}"
    
    class Meta:
        verbose_name = "Notificación de pago"
        verbose_name_plural = "Notificaciones de pago"
        ordering = ['-created_at']
