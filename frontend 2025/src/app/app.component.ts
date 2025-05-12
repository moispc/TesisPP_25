import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { CheckoutComponent } from './shared/checkout/checkout.component';
import { CarritoComponent } from './pages/carrito/carrito.component';
import { ExitoComponent } from './pages/exito/exito.component';
import { HomeComponent } from './pages/home/home.component';
import { CartaComponent } from './pages/carta/carta.component';
import { NavComponent } from './shared/nav/nav.component';
import { RegistroComponent } from './pages/auth/registro/registro.component';
import { NosotrosDevsComponent} from './pages/nosotros-devs/nosotros-devs.component';
import { FooterComponent } from './shared/footer/footer.component';
import { InicioSesionComponent } from './pages/auth/inicio-sesion/inicio-sesion.component';
import { ContactoComponent } from './pages/contacto/contacto.component';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    NavComponent,
    RouterOutlet,
    CarritoComponent,
    CommonModule,
    DashboardComponent,
    CheckoutComponent,
    HomeComponent,
    ExitoComponent,
    CartaComponent,
    RegistroComponent,
    NosotrosDevsComponent,
    FooterComponent,
    ContactoComponent,
    InicioSesionComponent
  ],

  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'ISPC Food';
  vistaUsuario = false;
  estaAutenticado = false;
  esAdmin = false;

  constructor() {
    const email = localStorage.getItem('emailUser');
    this.estaAutenticado = !!localStorage.getItem('authToken');
    this.esAdmin = email === 'admin@admin.com';
  }

  setVistaUsuario(valor: boolean) {
    this.vistaUsuario = valor;
  }
}
