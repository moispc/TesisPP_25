import { Component } from '@angular/core';
import {ReactiveFormsModule, FormGroup, FormBuilder, Validators} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

// AuthService
// Router

@Component({
  selector: 'app-inicio-sesion',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './inicio-sesion.component.html',
  styleUrl: './inicio-sesion.component.css'
})
export class InicioSesionComponent {
  form!: FormGroup;
  errorMensaje: string="";
  constructor(private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private toastr:ToastrService
  ) {
    this.form = this.formBuilder.group({
      email: ['', [Validators.required], []],
      password: ['', [Validators.required, Validators.minLength(8)], []],
    });
  }
  get Password() {
    return this.form.get('password');
  }
  get Email() {
    return this.form.get('email');
  }



  onEnviar(event: Event) {
    event.preventDefault();

    if (this.form.valid) {
      // Llamar al servicio de autenticación
      this.authService.login(this.Email?.value, this.Password?.value).subscribe({
        next:(success)=> {
          // Guardar la URL de la imagen de perfil si viene en la respuesta (campo correcto)
          if (success.imagen_perfil_url) {
            localStorage.setItem('imagenPerfil', success.imagen_perfil_url);
          }
          // Redirigir al dashboard después de una autenticación exitosa
          this.router.navigate(['/home']);
        },
        error:(error)=> {
          // Mostrar un mensaje de error o manejar el fallo de autenticación

          this.errorMensaje="El usuario o contraseña es incorrecto";
          this.form.markAllAsTouched();
        }
      });
    } else {
      this.form.markAllAsTouched();
    }
  }
}
