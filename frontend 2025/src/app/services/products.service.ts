import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Producto } from '../model/producto.model';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class ProductsService {

  private apiUrl = 'api/producto/';
  constructor(private http:HttpClient) { }

  getProducts():Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  public addProduct(product:Producto):Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, product);
  }

  public updateProduct(product:Producto):Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}${product.id_producto}/`, product);
  }

  public deleteProduct(product:Producto):Observable<Producto> {
    return this.http.delete<Producto>(`${this.apiUrl}${product.id_producto}/`);
  }

}
