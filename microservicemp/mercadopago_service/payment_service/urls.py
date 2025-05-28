from django.urls import path

# Importar vistas desde views.py
from . import views
from .views import PaymentSuccessView

app_name = 'payment_service'

urlpatterns = [
    path('create-preference/', views.CreatePreferenceView.as_view(), name='create-preference'),
    path('webhook/', views.WebhookView.as_view(), name='webhook'),
    path('success/', PaymentSuccessView.as_view(), name='payment-success'),
]
