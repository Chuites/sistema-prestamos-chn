import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Prestamo } from '../models/prestamo';
import { API_BASE_URL } from './api-base';

@Injectable({
  providedIn: 'root'
})
export class PrestamoService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_BASE_URL}/prestamos`;

  listar(): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Prestamo> {
    return this.http.get<Prestamo>(`${this.apiUrl}/${id}`);
  }

  listarPorCliente(clienteId: number): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(
      `${this.apiUrl}/cliente/${clienteId}`
    );
  }
}