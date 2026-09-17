import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  NgOptionComponent,
  NgSelectComponent
} from '@ng-select/ng-select';
import { forkJoin } from 'rxjs';

import { Cliente } from '../../models/cliente';
import { Pago, RegistrarPago } from '../../models/pago';
import { Prestamo } from '../../models/prestamo';
import { ClienteService } from '../../services/cliente.service';
import { PagoService } from '../../services/pago.service';
import { PrestamoService } from '../../services/prestamo.service';
import { ToastrService } from '../../services/toastr.service';

const PATRON_RECIBO = /^(?=.*[A-Za-z0-9])[A-Za-z0-9_-]+$/;
const MONTO_MAXIMO = 100000000;

const MENSAJES_PATRON: Record<string, string> = {
  numeroRecibo:
    'El número de recibo solo admite letras, números, guiones y guiones bajos.'
};

@Component({
  selector: 'app-pagos',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgSelectComponent,
    NgOptionComponent
  ],
  templateUrl: './pagos.html',
  styleUrl: './pagos.scss'
})
export class Pagos implements OnInit {

  private readonly pagoService = inject(PagoService);
  private readonly prestamoService = inject(PrestamoService);
  private readonly clienteService = inject(ClienteService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly toastr = inject(ToastrService);

  prestamos: Prestamo[] = [];
  pagos: Pago[] = [];
  clientes: Cliente[] = [];
  cargando = false;
  guardando = false;

  readonly formulario = this.formBuilder.nonNullable.group({
    prestamoId: [0, [Validators.required, Validators.min(1)]],
    monto: [
      0,
      [
        Validators.required,
        Validators.min(0.01),
        Validators.max(MONTO_MAXIMO)
      ]
    ],
    numeroRecibo: [
      '',
      [
        Validators.required,
        Validators.maxLength(50),
        Validators.pattern(PATRON_RECIBO)
      ]
    ],
    observaciones: ['', Validators.maxLength(250)]
  });

  readonly filtroCliente = this.formBuilder.control(0);

  get pagosFiltrados(): Pago[] {
    const clienteId = Number(this.filtroCliente.value);

    if (!clienteId) {
      return this.pagos;
    }

    return this.pagos.filter(
      (pago) => pago.prestamo?.cliente?.id === clienteId
    );
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;

    forkJoin({
      prestamos: this.prestamoService.listar(),
      pagos: this.pagoService.listar(),
      clientes: this.clienteService.listar()
    }).subscribe({
      next: (respuesta) => {
        this.prestamos = Array.isArray(respuesta.prestamos)
          ? respuesta.prestamos
          : Object.values(respuesta.prestamos);

        this.pagos = Array.isArray(respuesta.pagos)
          ? respuesta.pagos
          : Object.values(respuesta.pagos);

        this.clientes = Array.isArray(respuesta.clientes)
          ? respuesta.clientes
          : Object.values(respuesta.clientes);

        this.cargando = false;
        this.changeDetector.detectChanges();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error('No fue posible cargar los pagos.');
        this.cargando = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  registrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando = true;

    const datos = this.formulario.getRawValue();

    const pago: RegistrarPago = {
      monto: datos.monto,
      numeroRecibo: datos.numeroRecibo,
      observaciones: datos.observaciones
    };

    this.pagoService.registrar(
      datos.prestamoId,
      pago
    ).subscribe({
      next: () => {
        this.toastr.exito('Pago registrado correctamente.');
        this.formulario.reset({
          prestamoId: 0,
          monto: 0,
          numeroRecibo: '',
          observaciones: ''
        });
        this.guardando = false;
        this.cargarDatos();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error(
          error?.error?.mensaje
          ?? 'No fue posible registrar el pago. Verifica el saldo.'
        );
        this.guardando = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  mensajeError(campo: string): string {
    const control = this.formulario.get(campo);

    if (!control || control.valid || !(control.touched || control.dirty)) {
      return '';
    }

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }

    if (campo === 'prestamoId' && control.hasError('min')) {
      return 'Selecciona un préstamo.';
    }

    if (control.hasError('min')) {
      const { min } = control.getError('min');
      return `El valor mínimo es ${min}.`;
    }

    if (control.hasError('max')) {
      const { max } = control.getError('max');
      return `El valor máximo es ${max}.`;
    }

    if (control.hasError('maxlength')) {
      const { requiredLength } = control.getError('maxlength');
      return `No puede superar ${requiredLength} caracteres.`;
    }

    if (control.hasError('pattern')) {
      return MENSAJES_PATRON[campo] ?? 'El formato no es válido.';
    }

    return 'El valor ingresado no es válido.';
  }
}
