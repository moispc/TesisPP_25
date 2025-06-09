import { Component, OnInit } from '@angular/core';
import { Usuario } from '../../../../app/model/usuario.model';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { UserService } from '../../../services/user.service';
import { Router } from '@angular/router';
import { HttpErrorResponse, HttpHeaderResponse } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css',
})
export class RegistroComponent implements OnInit {
  usuario: Usuario = new Usuario();
  form: FormGroup;
  emailExistente: boolean = false;
  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private router: Router,
    private toastr:ToastrService
  ) {
    this.form = this.formBuilder.group({
      nombre: ['', [Validators.required]],
      apellido: ['', [Validators.required]],
      telefono: [
        '',
        [
          Validators.required,
          Validators.minLength(7),
          Validators.maxLength(12),
          Validators.pattern(/^([0-9])*$/),
        ],
      ],
      email: ['', [Validators.email, Validators.required]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      checkPassword: ['', [Validators.required]],
    });
  }
  ngOnInit(): void {
    this.form.reset();
  }
  onEnviar(event: Event): void {
    event.preventDefault;
    if (this.form.valid) {
      this.usuario = this.form.value;
      this.userService.addUser(this.usuario).subscribe({
        next: (data) => {
          if (data && data.re_registro && data.nombre) {
            this.toastr.success('Gracias por volver a elegirnos, ' + data.nombre + '!');
          } else {
            this.toastr.success('Usuario registrado correctamente. Inicie sesión con sus nuevas credenciales.');
          }
          this.router.navigate(['exitoNuevo']);
        },
        error: (err: HttpErrorResponse) => {
          if (err.error && err.error.email) {
            this.emailExistente = true;
          }
          if (err.status === 0) this.router.navigate(['serverError']);
        },
      });
    } else {
      this.form.markAllAsTouched();
    }
  }


  get Password() {
    return this.form.get('password');
  }
  get Email() {
    return this.form.get('email');
  }
  get Apellido() {
    return this.form.get('apellido');
  }
  get Telefono() {
    return this.form.get('telefono');
  }
  get Nombre() {
    return this.form.get('nombre');
  }
  get CheckPassword() {
    return this.form.get('checkPassword');
  }
}
