import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedDataService {
  private direccionSubject = new BehaviorSubject<string>(localStorage.getItem('direccion') || '');
  public direccion$ = this.direccionSubject.asObservable();

  constructor() { 
    // Escuchar los cambios en localStorage para la dirección
    window.addEventListener('storage', this.storageEventListener.bind(this));
  }

  private storageEventListener(event: StorageEvent): void {
    if (event.key === 'direccion') {
      this.direccionSubject.next(event.newValue || '');
    }
  }

  public actualizarDireccion(direccion: string): void {
    if (direccion && direccion.trim() !== '') {
      localStorage.setItem('direccion', direccion);
      this.direccionSubject.next(direccion);
    }
  }

  public obtenerDireccion(): string {
    return localStorage.getItem('direccion') || '';
  }
}
