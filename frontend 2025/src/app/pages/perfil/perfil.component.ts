import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { Usuario } from '../../model/usuario.model';
import { ImageUploadService } from '../../services/image-upload.service';
import { SharedDataService } from '../../services/shared-data.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.css']
})
export class PerfilComponent implements OnInit {
  perfilForm: FormGroup;
  usuario: any = {};
  cargando = false;
  refrescando = false; // Para el spinner de refresco
  
  // Variables para la imagen de perfil
  imagenPerfil: string | null = null;
  imagenError: string | null = null;
  imagenArchivo: File | null = null;    constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private toastr: ToastrService,
    private imageUploadService: ImageUploadService, // Inyectar el servicio
    private sharedDataService: SharedDataService,
    private router: Router
  ) {
    this.perfilForm = this.fb.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      direccion: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.imagenPerfil = localStorage.getItem('imagenPerfil') || null;
    this.cargando = true;
    // Usar el nuevo endpoint /appUSERS/me/ para obtener los datos actualizados
    this.authService.getUserProfileMe().subscribe({
      next: (user) => {
        this.usuario = user;
        this.perfilForm.patchValue({
          nombre: user.nombre || '',
          apellido: user.apellido || '',
          email: user.email || '',
          telefono: user.telefono || '',
          direccion: user.direccion || ''
        });
        if (user.imagen_perfil_url) {
          this.imagenPerfil = user.imagen_perfil_url;
          localStorage.setItem('imagenPerfil', user.imagen_perfil_url);
        }
        localStorage.setItem('nameUser', `${user.nombre} ${user.apellido}`);
        localStorage.setItem('emailUser', user.email || '');
        localStorage.setItem('telefono', user.telefono || '');
        localStorage.setItem('direccion', user.direccion || '');
        this.cargando = false;
      },
      error: () => {
        // Si falla, usar los datos locales como respaldo
        const nombre = localStorage.getItem('nameUser') || '';
        const [nombreUsuario, apellidoUsuario] = nombre.split(' ');
        const email = localStorage.getItem('emailUser') || 'usuario@ejemplo.com';
        const direccion = localStorage.getItem('direccion') || '';
        const telefono = localStorage.getItem('telefono') || '123456789';
        this.perfilForm.patchValue({
          nombre: nombreUsuario,
          apellido: apellidoUsuario || '',
          email: email,
          telefono: telefono,
          direccion: direccion
        });
        this.cargando = false;
      }
    });
    const fecha = localStorage.getItem('fechaActualizacion');
    if (fecha) {
      this.usuario.fechaActualizacion = fecha;
    }
  }

  // Método para manejar la selección de una nueva imagen
  onImagenSeleccionada(event: any): void {
    this.imagenError = null;
    const file = event.target.files[0];
    if (file) {
      // Validar tamaño (máximo 2MB)
      if (file.size > 2 * 1024 * 1024) {
        this.imagenError = 'La imagen no debe superar los 2MB';
        return;
      }
      // Validar tipo de archivo
      if (!file.type.match(/image\/(jpeg|jpg|png|gif|webp)/)) {
        this.imagenError = 'Formato de imagen no válido. Use JPG, PNG, GIF o WEBP.';
        return;
      }
      this.imagenArchivo = file;
      this.cargando = true;
      // Subir a Cloudinary
      this.imageUploadService.uploadImage(file).subscribe({
        next: (res) => {
          const nuevaUrl = res.secure_url;
          // Guardar la URL en la base de datos
          const userId = localStorage.getItem('idUser');
          this.authService.updateUser(userId!, { id_usuario: userId, imagen_perfil_url: nuevaUrl }).subscribe({
            next: () => {
              this.imagenPerfil = nuevaUrl;
              localStorage.setItem('imagenPerfil', nuevaUrl);
              this.toastr.success('Imagen subida y guardada correctamente');
              this.cargando = false;
            },
            error: () => {
              this.imagenError = 'Error al guardar la imagen en el perfil';
              this.cargando = false;
            }
          });
        },
        error: (err) => {
          this.imagenError = 'Error al subir la imagen';
          this.cargando = false;
        }
      });
    }
  }

  cargarDatosUsuario(): void {
    const userId = localStorage.getItem('idUser');
    if (userId) {
      this.cargando = true;
      // Aquí normalmente harías una petición para obtener los datos del usuario
      // por ahora simularemos con datos del localStorage
      const nombre = localStorage.getItem('nameUser') || '';
      const [nombreUsuario, apellidoUsuario] = nombre.split(' ');
      const email = localStorage.getItem('emailUser') || 'usuario@ejemplo.com';
      
      this.perfilForm.patchValue({
        nombre: nombreUsuario,
        apellido: apellidoUsuario || '',
        email: email, // Ahora usamos el email del localStorage
        telefono: '123456789', // Esto debería venir del backend
        direccion: 'Dirección de ejemplo' // Esto debería venir del backend
      });
      
      this.cargando = false;
    }
  }

  actualizarPerfil(): void {
    if (this.perfilForm.invalid) {
      this.toastr.error('Por favor, complete todos los campos requeridos');
      return;
    }

    const userId = localStorage.getItem('idUser');
    if (!userId) {
      this.toastr.error('No se pudo identificar al usuario');
      return;
    }

    this.cargando = true;
    const fechaActualizacion = new Date();
    const fechaActualizacionStr = fechaActualizacion.toLocaleString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
    // Obtener la URL de la imagen de perfil más reciente
    const imagenPerfilUrl = this.imagenPerfil || localStorage.getItem('imagenPerfil') || null;
    const datosUsuario = {
      ...this.perfilForm.value,
      id_usuario: userId,
      imagen_perfil_url: imagenPerfilUrl, // Siempre enviar la URL actual
      fechaActualizacion: fechaActualizacionStr,
      direccion: this.perfilForm.value.direccion // Asegura que la dirección se envía
    };

    this.authService.updateUser(userId, datosUsuario).subscribe({
      next: (response) => {
        this.toastr.success('Perfil actualizado con éxito');        localStorage.setItem('nameUser', `${datosUsuario.nombre} ${datosUsuario.apellido}`);
        localStorage.setItem('fechaActualizacion', fechaActualizacionStr);
        
        // Utilizar el servicio compartido para actualizar la dirección
        if (datosUsuario.direccion && datosUsuario.direccion.trim() !== '') {
          this.sharedDataService.actualizarDireccion(datosUsuario.direccion);
        }
        
        localStorage.setItem('telefono', datosUsuario.telefono || '');
        this.usuario.fechaActualizacion = fechaActualizacionStr;
        this.cargando = false;
      },
      error: (error) => {
        this.toastr.error('Error al actualizar el perfil');
        console.error('Error:', error);
        this.cargando = false;
      }
    });
  }
  eliminarCuenta(): void {
    const userId = localStorage.getItem('idUser');
    if (!userId) {
      this.toastr.error('No se pudo identificar al usuario');
      return;
    }
    
    if (confirm('¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.')) {
      this.authService.deleteUser(userId).subscribe({
        next: () => {
          // El método logout ya está siendo llamado dentro de deleteUser
          this.toastr.success('Cuenta eliminada correctamente');
          // Redirigir al usuario a la página de login
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.toastr.error('Error al eliminar la cuenta');
          console.error('Error:', error);
        }
      });
    }
  }

  refrescarPerfil(): void {
    this.refrescando = true;
    this.authService.getUserProfileMe().subscribe({
      next: (user) => {
        this.usuario = user;
        this.perfilForm.patchValue({
          nombre: user.nombre || '',
          apellido: user.apellido || '',
          email: user.email || '',
          telefono: user.telefono || '',
          direccion: user.direccion || ''
        });
        if (user.imagen_perfil_url) {
          this.imagenPerfil = user.imagen_perfil_url;
          localStorage.setItem('imagenPerfil', user.imagen_perfil_url);
        }
        localStorage.setItem('nameUser', `${user.nombre} ${user.apellido}`);
        localStorage.setItem('emailUser', user.email || '');
        localStorage.setItem('telefono', user.telefono || '');
        localStorage.setItem('direccion', user.direccion || '');
        this.refrescando = false;
        this.toastr.success('Perfil actualizado');
      },
      error: () => {
        this.toastr.error('No se pudo refrescar el perfil');
        this.refrescando = false;
      }
    });
  }
}