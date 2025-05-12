from rest_framework import viewsets
from .models import Producto
from .serializers import ProductoSerializer

# Create your views here.


class ProductoViewSet(viewsets.ModelViewSet):
    serializer_class = ProductoSerializer
    queryset = Producto.objects.all()
