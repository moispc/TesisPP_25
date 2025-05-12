import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Usuario } from '../model/usuario.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  url:string="appUSERS/";
  constructor(private http:HttpClient) { }

  getUsers(){
    return this.http.get(this.url+"/users");
  }


  addUser(user:Usuario): Observable<Usuario>
  {
    return this.http.post<Usuario>(this.url+'register/', user);

  }

  public updateUser(user:Usuario): Observable <Usuario>{
    return this.http.put<Usuario>(this.url+'/users/'+user.id_usuario, user);
  }


}
