import { Cliente } from './cliente';

export type EstadoSolicitud =
  | 'EN_PROCESO'
  | 'APROBADA'
  | 'RECHAZADA';

export interface CrearSolicitud {
  clienteId: number;
  montoSolicitado: number;
  plazoMeses: number;
  destinoPrestamo: string;
  observaciones: string;
}

export interface ResolverSolicitud {
  estado: 'APROBADA' | 'RECHAZADA';
  comentario: string;
  tasaInteresAnual: number | null;
}

export interface SolicitudPrestamo {
  id: number;
  cliente: Cliente;
  montoSolicitado: number;
  plazoMeses: number;
  destinoPrestamo: string;
  observaciones: string;
  fechaSolicitud: string;
  estado: EstadoSolicitud;
  fechaResolucion: string | null;
  comentarioResolucion: string | null;
}