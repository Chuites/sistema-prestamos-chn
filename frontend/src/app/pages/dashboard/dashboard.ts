import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import { forkJoin } from 'rxjs';

import { Cliente } from '../../models/cliente';
import { Pago } from '../../models/pago';
import { Prestamo } from '../../models/prestamo';
import { SolicitudPrestamo } from '../../models/solicitud-prestamo';
import { ClienteService } from '../../services/cliente.service';
import { PagoService } from '../../services/pago.service';
import { PrestamoService } from '../../services/prestamo.service';
import { SolicitudService } from '../../services/solicitud.service';
import { ToastrService } from '../../services/toastr.service';

interface ResumenCliente {
  cliente: Cliente;
  solicitudes: number;
  prestamos: number;
  pagos: number;
  montoPagado: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {

  private readonly clienteService = inject(ClienteService);
  private readonly solicitudService = inject(SolicitudService);
  private readonly prestamoService = inject(PrestamoService);
  private readonly pagoService = inject(PagoService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly toastr = inject(ToastrService);

  totalClientes = 0;
  totalSolicitudes = 0;
  solicitudesPendientes = 0;
  totalPrestamos = 0;
  totalPagos = 0;
  montoPrestado = 0;
  montoPagado = 0;
  saldoPendiente = 0;
  resumenPorCliente: ResumenCliente[] = [];
  cargando = true;

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;

    forkJoin({
      clientes: this.clienteService.listar(),
      solicitudes: this.solicitudService.listar(),
      prestamos: this.prestamoService.listar(),
      pagos: this.pagoService.listar()
    }).subscribe({
      next: (respuesta) => {
        const clientes = this.normalizar<Cliente>(respuesta.clientes);
        const solicitudes =
          this.normalizar<SolicitudPrestamo>(respuesta.solicitudes);
        const prestamos = this.normalizar<Prestamo>(respuesta.prestamos);
        const pagos = this.normalizar<Pago>(respuesta.pagos);

        this.calcularResumen(clientes, solicitudes, prestamos, pagos);

        this.cargando = false;
        this.changeDetector.detectChanges();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error('No fue posible cargar el panel general.');
        this.cargando = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  private calcularResumen(
    clientes: Cliente[],
    solicitudes: SolicitudPrestamo[],
    prestamos: Prestamo[],
    pagos: Pago[]
  ): void {
    this.totalClientes = clientes.length;
    this.totalSolicitudes = solicitudes.length;
    this.solicitudesPendientes = solicitudes.filter(
      (solicitud) => solicitud.estado === 'EN_PROCESO'
    ).length;
    this.totalPrestamos = prestamos.length;
    this.totalPagos = pagos.length;

    this.montoPrestado = this.sumar(
      prestamos.map((prestamo) => Number(prestamo.montoAprobado))
    );

    this.montoPagado = this.sumar(
      prestamos.map((prestamo) => Number(prestamo.montoPagado))
    );

    this.saldoPendiente = this.sumar(
      prestamos.map((prestamo) => Number(prestamo.saldoPendiente))
    );

    this.resumenPorCliente = clientes
      .map((cliente) => {
        const pagosCliente = pagos.filter(
          (pago) => pago.prestamo?.cliente?.id === cliente.id
        );

        return {
          cliente,
          solicitudes: solicitudes.filter(
            (solicitud) => solicitud.cliente?.id === cliente.id
          ).length,
          prestamos: prestamos.filter(
            (prestamo) => prestamo.cliente?.id === cliente.id
          ).length,
          pagos: pagosCliente.length,
          montoPagado: this.sumar(
            pagosCliente.map((pago) => Number(pago.monto))
          )
        };
      })
      .sort((a, b) => b.pagos - a.pagos
        || b.solicitudes - a.solicitudes);
  }

  private normalizar<T>(valor: T[]): T[] {
    return Array.isArray(valor) ? valor : Object.values(valor);
  }

  private sumar(valores: number[]): number {
    return valores.reduce(
      (total, valor) => total + (Number.isFinite(valor) ? valor : 0),
      0
    );
  }
}
