import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExitoNuevoUsuarioComponent } from './exito-nuevo-usuario.component';

describe('ExitoNuevoUsuarioComponent', () => {
  let component: ExitoNuevoUsuarioComponent;
  let fixture: ComponentFixture<ExitoNuevoUsuarioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExitoNuevoUsuarioComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExitoNuevoUsuarioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
