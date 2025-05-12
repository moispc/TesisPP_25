import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pagina404',
  standalone: true,
  imports: [],
  templateUrl: './pagina404.component.html',
  styleUrl: './pagina404.component.css'
})
export class Pagina404Component implements OnInit {
  contador=5;
constructor(private router:Router){}

ngOnInit() {
 this.startCountdown();
}

startCountdown(){
  const contdownInterval=setInterval(() => {this.contador--;
    if(this.contador==0){
      clearInterval(contdownInterval);
      this.router.navigate(['/']);
    }
  },1000);
    
}
}
