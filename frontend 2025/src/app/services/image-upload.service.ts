import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ImageUploadService {
  private cloudName = 'djp80kwaj';
  private uploadPreset = 'ml_default'; // Puedes cambiarlo si tienes un preset propio
  private apiUrl = `https://api.cloudinary.com/v1_1/${this.cloudName}/image/upload`;

  constructor(private http: HttpClient) {}

  uploadImage(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.uploadPreset);
    // Si necesitas firmar la petición, deberías hacerlo desde el backend por seguridad
    return this.http.post(this.apiUrl, formData);
  }
}
