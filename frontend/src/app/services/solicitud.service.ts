import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CrearSolicitud,
  ResolverSolicitud,
  SolicitudPrestamo
} from '../models/solicitud-prestamo';
import { API_BASE_URL } from './api-base';

@Injectable({
  providedIn: 'root'
})
export class SolicitudService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_BASE_URL}/solicitudes`;

  listar(): Observable<SolicitudPrestamo[]> {
    return this.http.get<SolicitudPrestamo[]>(this.apiUrl);
  }

  listarPorCliente(
    clienteId: number
  ): Observable<SolicitudPrestamo[]> {
    return this.http.get<SolicitudPrestamo[]>(
      `${this.apiUrl}/cliente/${clienteId}`
    );
  }

  crear(
    solicitud: CrearSolicitud
  ): Observable<SolicitudPrestamo> {
    return this.http.post<SolicitudPrestamo>(
      this.apiUrl,
      solicitud
    );
  }

  resolver(
    id: number,
    resolucion: ResolverSolicitud
  ): Observable<SolicitudPrestamo> {
    return this.http.put<SolicitudPrestamo>(
      `${this.apiUrl}/${id}/resolver`,
      resolucion
    );
  }
}