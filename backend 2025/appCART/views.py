from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from .models import DetallePedido, Pedido, Carrito
from appFOOD.models import Producto
from datetime import date, datetime
from .serializers import DetallePedidoSerializer, ModificarCantidadSerializer
from asgiref.sync import sync_to_async
from appUSERS.models import Usuario
from rest_framework import status
from asgiref.sync import sync_to_async
from appUSERS.models import Usuario
from rest_framework import status

class AgregarProductoAlCarrito(APIView):
    permission_classes = [IsAuthenticated]
    
    def post(self, request, producto_id):

        producto = Producto.objects.get(pk=producto_id)
        cantidad = int(request.data.get('cantidad'))
        id_usuario = request.user.id_usuario
        direccion = request.data.get('direccion')

        current_time = datetime.now().time()
        pedido, created  = Pedido.objects.get_or_create(id_usuario_id=id_usuario, estado="Pendiente")

        # Si el usuario envía una dirección, actualizarla en su perfil
        if direccion:
            request.user.direccion = direccion
            request.user.save()
            # Actualizar la dirección de entrega en todos los detalles del pedido pendiente
            if not created:
                DetallePedido.objects.filter(id_pedido=pedido).update(direccion_entrega=direccion)
        # Si no hay dirección en la petición, intentar usar la del perfil
        elif request.user.direccion:
            direccion = request.user.direccion
        # Si no hay dirección en ninguna parte, usar valor por defecto
        else:
            direccion = 'Sin especificar'

        if cantidad > producto.stock:
            return Response({'error': 'Stock insuficiente'}, status=400)

        # Actualizar dirección de entrega siempre
        pedido.direccion_entrega = direccion
        pedido.save()
        # Actualizar la dirección de entrega en todos los detalles del pedido (incluido el nuevo)
        DetallePedido.objects.filter(id_pedido=pedido).update(direccion_entrega=direccion)

        if created:
            pedido.hora_pedido = current_time
            pedido.fecha_pedido = date.today()
            pedido.save()

        carrito, carritoCreated = Carrito.objects.get_or_create(producto_id=producto.id_producto, usuario_id=id_usuario, id_pedido_id=pedido.id_pedidos)
        if carritoCreated:
            carrito.cantidad = cantidad
            carrito.save()
        else:
            carrito.cantidad += cantidad
            carrito.save()

        detallePedido, detalleCreated = DetallePedido.objects.get_or_create(
            precio_producto=producto.precio, id_pedido_id=pedido.id_pedidos, id_producto_id=producto.id_producto)

        if detalleCreated:
            detallePedido.cantidad_productos = cantidad
            detallePedido.subtotal = detallePedido.cantidad_productos * detallePedido.precio_producto
            detallePedido.save()
        else:
            detallePedido.cantidad_productos += cantidad
            detallePedido.subtotal = detallePedido.cantidad_productos * detallePedido.precio_producto
            detallePedido.save()

        # Ahora sí, actualizar la dirección en todos los detalles del pedido (incluido el nuevo)
        DetallePedido.objects.filter(id_pedido=pedido).update(direccion_entrega=pedido.direccion_entrega)

        producto.stock -= cantidad
        producto.save()
        return Response({'message': 'Producto agregado al carrito'})

class VerCarrito(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        usuario = request.user
        id_usuario = usuario.id_usuario

        detalles_carrito = Carrito.objects.select_related("id_pedido").all().filter(usuario_id=id_usuario)
        carrito_data = [
            {
                "id": detalle.id,
                'producto': detalle.producto.nombre_producto,
                'cantidad': detalle.cantidad,
                "precio": detalle.producto.precio,
                "imageURL": detalle.producto.imageURL
            } for detalle in detalles_carrito]

        return Response(carrito_data)

class ConfirmarPedido(APIView):
    permission_classes = [IsAuthenticated]
    
    def post(self, request):
        try:
            usuario = request.user
            id_usuario = usuario.id_usuario

            detalles_carrito = Carrito.objects.filter(usuario_id=id_usuario)
            pedido = Pedido.objects.get(id_usuario_id=id_usuario, estado="Pendiente")
            
            if not detalles_carrito.exists():
                return Response({'error': 'El carrito está vacío'}, status=400)
                
            # Verificar que el pedido tenga dirección de entrega
            if not pedido.direccion_entrega or pedido.direccion_entrega == 'Sin especificar':
                if usuario.direccion:
                    pedido.direccion_entrega = usuario.direccion
                    
            detalles_carrito.delete()
            pedido.estado = "Aprobado"
            pedido.save()
            
            return Response({'message': 'Pedido confirmado'})
        except Carrito.DoesNotExist:
            return Response({"error": "El carrito está vacio."}, status=status.HTTP_404_NOT_FOUND)
        except Pedido.DoesNotExist:
            return Response({"error": "El carrito está vacio."}, status=status.HTTP_404_NOT_FOUND)

class EliminarProductoDelCarrito(APIView):
    permission_classes = [IsAuthenticated]
    
    def delete(self, request, carrito_id):

        try:

            carrito_item = Carrito.objects.get(pk=carrito_id)

            detalle_item = DetallePedido.objects.get(id_producto_id=carrito_item.producto.id_producto, id_pedido_id = carrito_item.id_pedido)

            producto = carrito_item.producto
            producto.stock += carrito_item.cantidad
            producto.save()
            carrito_item.delete()
            detalle_item.delete()
            return Response({'message': 'Producto eliminado del carrito'})
        except Carrito.DoesNotExist:
            return Response({"error": "No existe un producto en el carrito con ese id de carrito."}, status=status.HTTP_404_NOT_FOUND)

class VerDashboard(APIView):
    permission_classes = [IsAuthenticated]
    
    def get(self, request):
        usuario = request.user
        id_usuario = usuario.id_usuario
        vistaPedidos = Pedido.objects.prefetch_related('detalles').all().filter(id_usuario_id=id_usuario)
        # Eliminar print innecesario
        
        carrito_data = []
        for pedido in vistaPedidos:
            # Obtener dirección del pedido, o usar la del usuario, o usar valor por defecto
            direccion_entrega = pedido.direccion_entrega
            if not direccion_entrega or direccion_entrega == 'Sin especificar':
                direccion_entrega = usuario.direccion if usuario.direccion else 'Sin especificar'
                
                # Si estamos usando la dirección del usuario, actualizar también el pedido
                if direccion_entrega != 'Sin especificar' and direccion_entrega != pedido.direccion_entrega:
                    pedido.direccion_entrega = direccion_entrega
                    pedido.save()
                
            carrito_data.append({
                "id_pedidos": pedido.id_pedidos,
                "fecha_pedido": pedido.fecha_pedido,
                "direccion_entrega": direccion_entrega,
                "estado": pedido.estado,
                "detalles": DetallePedidoSerializer(pedido.detalles.all(), many=True).data
            })

        return Response({"results": carrito_data})

class ModificarCantidadProductoCarrito(APIView):
    permission_classes = [IsAuthenticated]

    def put(self, request, carrito_id):
        serializer = ModificarCantidadSerializer(data=request.data)
        if serializer.is_valid():
            nueva_cantidad = serializer.validated_data['cantidad']
            try:
                carrito_item = Carrito.objects.get(pk=carrito_id)
                producto = carrito_item.producto

                if nueva_cantidad > producto.stock + carrito_item.cantidad:
                    return Response({'error': 'Stock insuficiente'}, status=400)

                diferencia_cantidad = nueva_cantidad - carrito_item.cantidad
                carrito_item.cantidad = nueva_cantidad
                carrito_item.save()

                producto.stock -= diferencia_cantidad
                producto.save()

                detalle_item = DetallePedido.objects.get(id_producto_id=carrito_item.producto.id_producto, id_pedido_id=carrito_item.id_pedido)
                detalle_item.cantidad_productos = nueva_cantidad
                detalle_item.subtotal = detalle_item.cantidad_productos * detalle_item.precio_producto
                detalle_item.save()

                return Response({'message': 'Cantidad de producto actualizada en el carrito'})
            except Carrito.DoesNotExist:
                return Response({"error": "No existe un producto en el carrito con ese id de carrito."}, status=status.HTTP_404_NOT_FOUND)
            except DetallePedido.DoesNotExist:
                return Response({"error": "No existe un detalle de pedido para este producto en el carrito."}, status=status.HTTP_404_NOT_FOUND)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class VerDetallePedido(APIView):
    permission_classes = [IsAuthenticated]
    
    def get(self, request, pedido_id):
        try:
            usuario = request.user
            pedido = Pedido.objects.prefetch_related('detalles').get(
                id_pedidos=pedido_id, id_usuario=usuario.id_usuario
            )
            
            # Obtener los detalles del pedido
            detalles = DetallePedido.objects.filter(id_pedido=pedido)
            
            # Calcular monto total
            monto_total = sum(detalle.subtotal for detalle in detalles)
            
            # Verificar la dirección de entrega
            direccion_entrega = pedido.direccion_entrega
            if not direccion_entrega or direccion_entrega == 'Sin especificar':
                direccion_entrega = usuario.direccion if usuario.direccion else 'Sin especificar'
            
            # Crear respuesta con información completa
            respuesta = {
                'pedido': {
                    'id_pedidos': pedido.id_pedidos,
                    'fecha_pedido': pedido.fecha_pedido,
                    'hora_pedido': pedido.hora_pedido,
                    'direccion_entrega': direccion_entrega,
                    'estado': pedido.estado,
                    'monto_total': monto_total
                },
                'detalles': [
                    {
                        'producto': detalle.id_producto.nombre_producto,
                        'cantidad': detalle.cantidad_productos,
                        'precio_unitario': detalle.precio_producto,
                        'subtotal': detalle.subtotal,
                        'imagen': detalle.id_producto.imageURL if hasattr(detalle.id_producto, 'imageURL') else None
                    } for detalle in detalles
                ]
            }
            
            return Response(respuesta)
        except Pedido.DoesNotExist:
            return Response({"error": "Pedido no encontrado"}, status=status.HTTP_404_NOT_FOUND)

class EntregarPedido(APIView):
    permission_classes = [IsAuthenticated]

    def put(self, request):
        id_pedidos = request.data.get('id_pedidos')
        if not id_pedidos:
            return Response({'error': 'Falta el id del pedido'}, status=400)
        try:
            pedido = Pedido.objects.get(id_pedidos=id_pedidos, id_usuario=request.user.id_usuario)
            if pedido.estado != 'Aprobado':
                return Response({'error': 'Solo se pueden entregar pedidos aprobados'}, status=400)
            pedido.estado = 'Entregado'
            pedido.save()
            return Response({'message': 'Pedido entregado correctamente'})
        except Pedido.DoesNotExist:
            return Response({'error': 'Pedido no encontrado'}, status=404)

