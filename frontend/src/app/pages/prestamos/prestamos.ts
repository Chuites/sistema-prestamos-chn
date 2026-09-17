import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { Modal } from '../../components/modal/modal';
import { Cliente } from '../../models/cliente';
import { Pago } from '../../models/pago';
import { Prestamo } from '../../models/prestamo';
import { ClienteService } from '../../services/cliente.service';
import { PagoService } from '../../services/pago.service';
import { PrestamoService } from '../../services/prestamo.service';
import { ToastrService } from '../../services/toastr.service';

@Component({
  selector: 'app-prestamos',
  imports: [CommonModule, ReactiveFormsModule, Modal],
  templateUrl: './prestamos.html',
  styleUrl: './prestamos.scss'
})
export class Prestamos implements OnInit {

  private readonly prestamoService = inject(PrestamoService);
  private readonly clienteService = inject(ClienteService);
  private readonly pagoService = inject(PagoService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly toastr = inject(ToastrService);

  prestamos: Prestamo[] = [];
  clientes: Cliente[] = [];
  prestamoSeleccionado: Prestamo | null = null;
  pagosPrestamo: Pago[] = [];
  cargando = false;
  cargandoPagos = false;

  readonly filtroCliente = this.formBuilder.control(0);

  get prestamosFiltrados(): Prestamo[] {
    const clienteId = Number(this.filtroCliente.value);

    if (!clienteId) {
      return this.prestamos;
    }

    return this.prestamos.filter(
      (prestamo) => prestamo.cliente?.id === clienteId
    );
  }

  ngOnInit(): void {
    this.cargarPrestamos();
  }

  cargarPrestamos(): void {
    this.cargando = true;

    forkJoin({
      prestamos: this.prestamoService.listar(),
      clientes: this.clienteService.listar()
    }).subscribe({
      next: (respuesta) => {
        this.prestamos = Array.isArray(respuesta.prestamos)
          ? respuesta.prestamos
          : Object.values(respuesta.prestamos);

        this.clientes = Array.isArray(respuesta.clientes)
          ? respuesta.clientes
          : Object.values(respuesta.clientes);

        this.cargando = false;
        this.changeDetector.detectChanges();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error('No fue posible cargar los préstamos.');
        this.cargando = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  verPagos(prestamo: Prestamo): void {
    this.prestamoSeleccionado = prestamo;
    this.pagosPrestamo = [];
    this.cargandoPagos = true;

    this.pagoService.listarPorPrestamo(prestamo.id).subscribe({
      next: (respuesta) => {
        this.pagosPrestamo = Array.isArray(respuesta)
          ? respuesta
          : Object.values(respuesta);

        this.cargandoPagos = false;
        this.changeDetector.detectChanges();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error(
          'No fue posible cargar los pagos del préstamo.'
        );
        this.cargandoPagos = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  cerrarPagos(): void {
    this.prestamoSeleccionado = null;
    this.pagosPrestamo = [];
  }

  get totalPagosPrestamo(): number {
    return this.pagosPrestamo.reduce(
      (total, pago) => total + Number(pago.monto || 0),
      0
    );
  }

  porcentajePagado(prestamo: Prestamo): number {
    if (prestamo.montoAprobado <= 0) {
      return 0;
    }

    return Math.round(
      (prestamo.montoPagado / prestamo.montoAprobado) * 100
    );
  }
}
