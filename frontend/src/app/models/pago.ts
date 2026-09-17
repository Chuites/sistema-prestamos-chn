import { Prestamo } from './prestamo';

export interface RegistrarPago {
  monto: number;
  numeroRecibo: string;
  observaciones: string;
}

export interface Pago {
  id: number;
  prestamo: Prestamo;
  monto: number;
  fechaPago: string;
  metodoPago: string;
  numeroRecibo: string;
  observaciones: string;
}