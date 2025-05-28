"""
WSGI config for mp_integration project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/5.2/howto/deployment/wsgi/
"""

import os
import sys

# Añadir carpeta padre al PYTHONPATH para importar mp_integration
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

# Añadir carpeta 'mercadopago_service' al PYTHONPATH para que se encuentre mp_integration
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from django.core.wsgi import get_wsgi_application

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'mp_integration.settings')

application = get_wsgi_application()
