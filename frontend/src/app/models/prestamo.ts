import { Cliente } from './cliente';
import { SolicitudPrestamo } from './solicitud-prestamo';

export type EstadoPrestamo =
  | 'PENDIENTE'
  | 'PARCIAL'
  | 'PAGADO';

export interface Prestamo {
  id: number;
  solicitud: SolicitudPrestamo;
  cliente: Cliente;
  montoAprobado: number;
  tasaInteresAnual: number;
  plazoMeses: number;
  montoPagado: number;
  saldoPendiente: number;
  estado: EstadoPrestamo;
  fechaAprobacion: string;
}