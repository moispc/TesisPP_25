import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { CarritoComponent } from '../carrito/carrito.component';


const fetchComentarios = async () => {
  const response = await fetch('assets/data/comentarios.json'); 
  return await response.json();
};
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, NgFor],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  comentarios:any[]=[];

  constructor() {}

  ngOnInit(): void {

    fetchComentarios().then((comentarios: any[]) => {
      this.comentarios = comentarios;
      // console.log(this.comentarios);
    });
    
  }
  
}
