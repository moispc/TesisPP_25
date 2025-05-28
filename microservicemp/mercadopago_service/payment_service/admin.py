from django.contrib import admin
from .models import PaymentRequest, PaymentNotification

@admin.register(PaymentRequest)
class PaymentRequestAdmin(admin.ModelAdmin):
    list_display = ('id', 'preference_id', 'status', 'total_amount', 'created_at')
    list_filter = ('status', 'created_at')
    search_fields = ('id', 'preference_id', 'user_token')
    readonly_fields = ('id', 'created_at', 'updated_at', 'cart_data')
    ordering = ('-created_at',)

@admin.register(PaymentNotification)
class PaymentNotificationAdmin(admin.ModelAdmin):
    list_display = ('id', 'payment_id', 'topic', 'processed', 'created_at')
    list_filter = ('topic', 'processed', 'created_at')
    search_fields = ('id', 'payment_id')
    readonly_fields = ('id', 'created_at', 'raw_data')
    ordering = ('-created_at',)
