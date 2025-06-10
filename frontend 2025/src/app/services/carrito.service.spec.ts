import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CarritoService } from './carrito.service';
import { ToastrService, ToastrModule } from 'ngx-toastr';

describe('CarritoService', () => {
  let service: CarritoService;
  let httpMock: HttpTestingController;
  let toastrSpy: jasmine.SpyObj<ToastrService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ToastrService', ['success', 'error', 'info']);

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ToastrModule.forRoot()
      ],
      providers: [
        CarritoService,
        { provide: ToastrService, useValue: spy }
      ]
    });
    
    service = TestBed.inject(CarritoService);
    httpMock = TestBed.inject(HttpTestingController);
    toastrSpy = TestBed.inject(ToastrService) as jasmine.SpyObj<ToastrService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
  it('should get cart items', () => {
    const mockResponse = [
      {
        id: 1,
        producto: 'Hamburguesa',
        cantidad: 2,
        precio: 500,
        imageURL: 'hamburguesa.jpg'
      }
    ];

    service.obtenerCarrito().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('appCART/ver/');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should add a product to cart', () => {
    const mockResponse = { message: 'Producto agregado al carrito' };
    const productId = '1';
    const cantidad = 2;

    service.agregarProducto(productId, cantidad).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`appCART/agregar/${productId}/`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ cantidad });
    req.flush(mockResponse);
  });
  it('should remove a product from cart', () => {
    const mockResponse = { message: 'Producto eliminado del carrito' };
    const carritoId = '1';

    service.eliminarItemCarrito(carritoId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`appCART/eliminar/${carritoId}/`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);

    // No podemos verificar directamente si un Observable ha sido observado
    // Esto se puede probar registrando un observador y verificando si las notificaciones llegan
    let notificationReceived = false;
    const subscription = service.actualizarCarrito$.subscribe(() => {
      notificationReceived = true;
    });
    
    service.tiggerActualizarCarrito();
    expect(notificationReceived).toBe(true);
    subscription.unsubscribe();
  });
});
