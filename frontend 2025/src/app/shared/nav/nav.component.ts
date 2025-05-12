import { CommonModule } from '@angular/common';
import { Component, OnInit, Input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './nav.component.html',
  styleUrl: './nav.component.css'
})
export class NavComponent implements OnInit {
  @Input() vistaUsuario = false;
  estaAutenticado = false;
  nombreUsuario = '';
  imagenPerfil: string | null = null;

  constructor(
    private authservice: AuthService, 
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authservice.isAuthenticated().subscribe({
      next: (respuesta) => {
        this.estaAutenticado = respuesta;
        if (respuesta) {
          this.obtenerNombreUsuario();
        }
      }
    });
    // Suscribirse a los cambios de la URL de la imagen de perfil
    this.authservice.imagenPerfil$.subscribe((url) => {
      this.imagenPerfil = url;
    });
  }

  obtenerNombreUsuario() {
    // Obtener el nombre del usuario del localStorage
    const nombreCompleto = localStorage.getItem('nameUser');
    const email = localStorage.getItem('emailUser');
    if (nombreCompleto) {
      // Extraer solo el primer nombre
      const primerNombre = nombreCompleto.split(' ')[0];
      // Asegurar que la primera letra sea mayúscula
      this.nombreUsuario = primerNombre.charAt(0).toUpperCase() + primerNombre.slice(1).toLowerCase();
    } else if (email === 'admin@admin.com') {
      this.nombreUsuario = 'Admin';
    } else {
      this.nombreUsuario = 'Usuario';
    }
  }

  isAdmin(): boolean {
    // Puedes cambiar esto por una lógica real de roles
    // Por ahora, si el email es admin@admin.com es admin
    const email = localStorage.getItem('emailUser');
    return email === 'admin@admin.com';
  }

  logout() {
    this.authservice.logout();
    this.toastr.info("Se cerró la sesión correctamente");
    // Redirigir al home después de logout
    this.router.navigate(['/home']);
  }

}
