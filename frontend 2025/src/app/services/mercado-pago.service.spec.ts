import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MercadoPagoService } from './mercado-pago.service';
import { ToastrService, ToastrModule } from 'ngx-toastr';
import { RouterTestingModule } from '@angular/router/testing';

describe('MercadoPagoService', () => {
  let service: MercadoPagoService;
  let httpMock: HttpTestingController;
  let toastrSpy: jasmine.SpyObj<ToastrService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ToastrService', ['success', 'error', 'info', 'warning']);

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        ToastrModule.forRoot()
      ],
      providers: [
        MercadoPagoService,
        { provide: ToastrService, useValue: spy }
      ]
    });
    
    service = TestBed.inject(MercadoPagoService);
    httpMock = TestBed.inject(HttpTestingController);
    toastrSpy = TestBed.inject(ToastrService) as jasmine.SpyObj<ToastrService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a preference', () => {
    const mockResponse = {
      payment_request_id: '123',
      preference_id: 'pref123',
      init_point: 'https://mercadopago.com/init_point'
    };
    
    service.crearPreferencia().subscribe(response => {
      expect(response).toEqual(mockResponse);
      expect(response.init_point).toBe('https://mercadopago.com/init_point');
    });

    const req = httpMock.expectOne('https://backmp.onrender.com/payment/create-preference/');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should verify payment status', () => {
    const paymentId = 'payment123';
    const mockResponse = {
      status: 'approved',
      payment_method_id: 'credit_card',
      payment_type_id: 'visa',
      external_reference: 'ref123'
    };
    
    service.verificarPago(paymentId).subscribe(response => {
      expect(response).toEqual(mockResponse);
      expect(response.status).toBe('approved');
    });

    const req = httpMock.expectOne('https://api.mercadopago.com/v1/payments/' + paymentId);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should handle error when creating preference', () => {
    service.crearPreferencia().subscribe(
      () => fail('should have failed with an error'),
      (error) => {
        expect(error).toBeTruthy();
        expect(toastrSpy.error).toHaveBeenCalled();
      }
    );

    const req = httpMock.expectOne('https://backmp.onrender.com/payment/create-preference/');
    expect(req.request.method).toBe('POST');
    req.error(new ErrorEvent('Network error'), { status: 500 });

    expect(toastrSpy.error).toHaveBeenCalled();
  });
});
