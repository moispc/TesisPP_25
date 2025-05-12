import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule,Validators } from '@angular/forms';

@Component({
  selector: 'app-contacto',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './contacto.component.html',
  styleUrl: './contacto.component.css',
})
export class ContactoComponent {
  form!: FormGroup;
  constructor(private formBuilder: FormBuilder) {
    this.form = this.formBuilder.group({
      nombre: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email], []],
      asunto: ['', [Validators.required],[]],
      mensaje: ['',[Validators.required], []],
    });
  }


  onEnviar(event: Event): void {
    event.preventDefault;
    if (this.form.valid) {
      console.log('Formulario válido');
    } else {
      this.form.markAllAsTouched();
      console.log('error en formulario');
    }
  }
  enviarMensaje() {
    console.log('enviando ');
  }

  get Nombre()
  {
  return this.form.get("nombre");
  }

  get Email()
  {
  return this.form.get("email");
  }

  get Asunto()
  {
  return this.form.get("asunto");
  }

  get Mensaje()
  {
  return this.form.get("mensaje");
  }




}
