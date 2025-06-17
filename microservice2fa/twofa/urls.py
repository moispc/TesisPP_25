from django.urls import path
from . import views

urlpatterns = [
    path('setup/', views.Setup2FAView.as_view(), name='setup-2fa'),
    path('verify/', views.Verify2FAView.as_view(), name='verify-2fa'),
    path('authorize/', views.AuthorizePurchaseView.as_view(), name='authorize-purchase'),
    path('health/', views.HealthCheckView.as_view(), name='health'),
]
