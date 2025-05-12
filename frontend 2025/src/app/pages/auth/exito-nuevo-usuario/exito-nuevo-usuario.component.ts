import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-exito-nuevo-usuario',
  standalone: true,
  imports: [],
  templateUrl: './exito-nuevo-usuario.component.html',
  styleUrl: './exito-nuevo-usuario.component.css',
})
export class ExitoNuevoUsuarioComponent implements OnInit {
  contador = 5;
  constructor(private router: Router) {}
  ngOnInit() {
    this.startCountdown();
  }

  startCountdown() {
    const contdownInterval = setInterval(() => {
      this.contador--;
      if (this.contador == 0) {
        clearInterval(contdownInterval);
        this.router.navigate(['login']);
      }
    }, 1000);
  }

  abrirConfirmacion(){
    this.router.navigate(['login']);
  }
}
