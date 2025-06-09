import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { CarritoComponent } from './pages/carrito/carrito.component';
import { CartaComponent } from './pages/carta/carta.component';
import { CheckoutComponent } from './shared/checkout/checkout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { RegistroComponent } from './pages/auth/registro/registro.component';
import { ExitoComponent } from './pages/exito/exito.component';
import { NosotrosDevsComponent } from './pages/nosotros-devs/nosotros-devs.component';
import { ContactoComponent } from './pages/contacto/contacto.component';
import { InicioSesionComponent } from './pages/auth/inicio-sesion/inicio-sesion.component';
import { authGuard } from './services/guards/auth.guard';
import { Pagina404Component } from './pages/pagina404/pagina404.component';
import { ExitoNuevoUsuarioComponent } from './pages/auth/exito-nuevo-usuario/exito-nuevo-usuario.component';
import { ServerErrorComponent } from './pages/server-error/server-error.component';
import { PerfilComponent } from './pages/perfil/perfil.component';
import { AdminProductosComponent } from './pages/admin-productos/admin-productos.component';

export const routes: Routes = [
    {path:"home", component:HomeComponent},
    {path:"", redirectTo:"/home", pathMatch:"full"},
    {path:"carta", component:CartaComponent},
    {path:"carrito", component:CarritoComponent},
    {path:"pagar", component:CheckoutComponent},
    {path:"dashboard", component:DashboardComponent, canActivate:[authGuard]},
    {path:"perfil", component:PerfilComponent, canActivate:[authGuard]},
    {path:"registro", component:RegistroComponent},
    {path:"exito", component:ExitoComponent},
    {path:"login", component:InicioSesionComponent},
    {path:'exitoNuevo', component:ExitoNuevoUsuarioComponent},
    {path:"quienes-somos", component:NosotrosDevsComponent},    
    {path:"contacto", component:ContactoComponent},    
    {path:"serverError", component:ServerErrorComponent},
    {path:"admin-productos", component:AdminProductosComponent, canActivate:[authGuard]},
    {path:'**', component:Pagina404Component},
];
