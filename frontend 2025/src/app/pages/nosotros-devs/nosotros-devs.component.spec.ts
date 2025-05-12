import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NosotrosDevsComponent } from './nosotros-devs.component';

describe('NosotrosDevsComponent', () => {
  let component: NosotrosDevsComponent;
  let fixture: ComponentFixture<NosotrosDevsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NosotrosDevsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NosotrosDevsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
