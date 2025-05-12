import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-nosotros-devs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nosotros-devs.component.html',
  styleUrl: './nosotros-devs.component.css'
})
export class NosotrosDevsComponent {

  profesionalList: {
    id: number;
    name: string;
    perfil: string;
    photo: string;
    githubLink: string;
    linkedinLink: string;
  }[] = [
    {
      id: 1,
      name: 'Veronica Ludemann',
      perfil: 'Desarrollador web',
      photo: 'assets/devs/vero.webp',
      githubLink: 'https://github.com/veroludemann',
      linkedinLink:"https://www.linkedin.com/in/veronica-ludemann/",
    },
    {
      id: 2,
      name: 'Diego Guzman',
      perfil: 'Desarrollador web',
      photo: 'assets/devs/die.webp',
      githubLink: 'https://github.com/cerveux',
      linkedinLink:"https://www.linkedin.com/in/diego-guzm%C3%A1n-cerveux/",
    },
    {
      id: 3,
      name: 'Nicolas Luna',
      perfil: 'Desarrollador web',
      photo: 'assets/devs/nico.webp',
      githubLink: 'https://github.com/NicolasLuna12',
      linkedinLink:"https://www.linkedin.com/in/nicolas-luna-632612106/",
    },
    {
      id: 4,
      name: 'Lucas Gelpi',
      perfil: 'Desarrollador web',
      photo: 'assets/devs/lucas.webp',
      githubLink: 'https://github.com/lucasGelpi',
      linkedinLink:"https://www.linkedin.com/in/lucas-gelpi/",
    },
  ];

}
