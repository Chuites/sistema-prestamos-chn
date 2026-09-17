import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Pago, RegistrarPago } from '../models/pago';
import { API_BASE_URL } from './api-base';

@Injectable({
  providedIn: 'root'
})
export class PagoService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_BASE_URL}/pagos`;

  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.apiUrl);
  }

  listarPorPrestamo(prestamoId: number): Observable<Pago[]> {
    return this.http.get<Pago[]>(
      `${this.apiUrl}/prestamo/${prestamoId}`
    );
  }

  registrar(
    prestamoId: number,
    pago: RegistrarPago
  ): Observable<Pago> {
    return this.http.post<Pago>(
      `${this.apiUrl}/prestamo/${prestamoId}`,
      pago
    );
  }
}