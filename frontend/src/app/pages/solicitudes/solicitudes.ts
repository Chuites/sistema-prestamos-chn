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
import { forkJoin } from 'rxjs';

import { Modal } from '../../components/modal/modal';
import { Cliente } from '../../models/cliente';
import {
  CrearSolicitud,
  EstadoSolicitud,
  SolicitudPrestamo
} from '../../models/solicitud-prestamo';
import { ClienteService } from '../../services/cliente.service';
import { SolicitudService } from '../../services/solicitud.service';
import { ToastrService } from '../../services/toastr.service';

const PATRON_DESTINO = /^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü .,#\-/]+$/;
const MONTO_MAXIMO = 100000000;
const TASA_MAXIMA = 999.99;

const MENSAJES_PATRON: Record<string, string> = {
  destinoPrestamo:
    'El destino solo admite letras, números y signos como # . , - /'
};

type FiltroEstado = 'TODOS' | EstadoSolicitud;

@Component({
  selector: 'app-solicitudes',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Modal
  ],
  templateUrl: './solicitudes.html',
  styleUrl: './solicitudes.scss'
})
export class Solicitudes implements OnInit {

  private readonly solicitudService = inject(SolicitudService);
  private readonly clienteService = inject(ClienteService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly toastr = inject(ToastrService);

  clientes: Cliente[] = [];
  solicitudes: SolicitudPrestamo[] = [];
  solicitudAResolver: SolicitudPrestamo | null = null;
  estadoResolucion: 'APROBADA' | 'RECHAZADA' | null = null;
  cargando = false;
  guardando = false;
  resolviendo = false;

  readonly formulario = this.formBuilder.nonNullable.group({
    clienteId: [0, [Validators.required, Validators.min(1)]],
    montoSolicitado: [
      0,
      [
        Validators.required,
        Validators.min(1),
        Validators.max(MONTO_MAXIMO)
      ]
    ],
    plazoMeses: [
      12,
      [
        Validators.required,
        Validators.min(1),
        Validators.max(360)
      ]
    ],
    destinoPrestamo: [
      '',
      [
        Validators.required,
        Validators.maxLength(200),
        Validators.pattern(PATRON_DESTINO)
      ]
    ],
    observaciones: ['', Validators.maxLength(500)]
  });

  readonly formularioResolucion = this.formBuilder.nonNullable.group({
    comentario: ['', [Validators.required, Validators.maxLength(500)]],
    tasaInteresAnual: [
      12.5,
      [Validators.min(0), Validators.max(TASA_MAXIMA)]
    ]
  });

  readonly filtroCliente = this.formBuilder.control(0);

  readonly filtroEstado = this.formBuilder.control<FiltroEstado>('TODOS');

  get solicitudesFiltradas(): SolicitudPrestamo[] {
    const clienteId = Number(this.filtroCliente.value);
    const estado = this.filtroEstado.value;

    return this.solicitudes.filter((solicitud) => {
      const coincideCliente =
        !clienteId || solicitud.cliente?.id === clienteId;
      const coincideEstado =
        estado === 'TODOS' || solicitud.estado === estado;

      return coincideCliente && coincideEstado;
    });
  }

  totalPorEstado(estado: EstadoSolicitud): number {
    return this.solicitudes.filter(
      (solicitud) => solicitud.estado === estado
    ).length;
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;

    forkJoin({
      clientes: this.clienteService.listar(),
      solicitudes: this.solicitudService.listar()
    }).subscribe({
      next: (respuesta) => {
        this.clientes = Array.isArray(respuesta.clientes)
          ? respuesta.clientes
          : Object.values(respuesta.clientes);

        this.solicitudes = Array.isArray(respuesta.solicitudes)
          ? respuesta.solicitudes
          : Object.values(respuesta.solicitudes);

        this.cargando = false;
        this.changeDetector.detectChanges();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error('No fue posible cargar las solicitudes.');
        this.cargando = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando = true;

    const solicitud: CrearSolicitud =
      this.formulario.getRawValue();

    this.solicitudService.crear(solicitud).subscribe({
      next: () => {
        this.toastr.exito('Solicitud registrada correctamente.');
        this.formulario.reset({
          clienteId: 0,
          montoSolicitado: 0,
          plazoMeses: 12,
          destinoPrestamo: '',
          observaciones: ''
        });
        this.guardando = false;
        this.cargarDatos();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error(
          error?.error?.mensaje
          ?? 'No fue posible registrar la solicitud.'
        );
        this.guardando = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  aprobar(solicitud: SolicitudPrestamo): void {
    this.abrirResolucion(solicitud, 'APROBADA');
  }

  rechazar(solicitud: SolicitudPrestamo): void {
    this.abrirResolucion(solicitud, 'RECHAZADA');
  }

  cerrarResolucion(): void {
    this.solicitudAResolver = null;
    this.estadoResolucion = null;
  }

  confirmarResolucion(): void {
    const solicitud = this.solicitudAResolver;
    const estado = this.estadoResolucion;

    if (!solicitud || !estado) {
      return;
    }

    this.formularioResolucion.markAllAsTouched();

    const { comentario, tasaInteresAnual } =
      this.formularioResolucion.getRawValue();

    if (!comentario.trim()) {
      return;
    }

    if (!this.formularioResolucion.valid) {
      return;
    }

    this.resolviendo = true;

    this.solicitudService.resolver(solicitud.id, {
      estado,
      comentario: comentario.trim(),
      tasaInteresAnual: estado === 'APROBADA'
        ? tasaInteresAnual
        : null
    }).subscribe({
      next: () => {
        this.toastr.exito(
          `Solicitud ${estado === 'APROBADA' ? 'aprobada' : 'rechazada'} correctamente.`
        );
        this.resolviendo = false;
        this.cerrarResolucion();
        this.cargarDatos();
      },
      error: (error) => {
        console.error(error);
        this.toastr.error(
          error?.error?.mensaje
          ?? 'No fue posible resolver la solicitud.'
        );
        this.resolviendo = false;
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

    if (campo === 'clienteId' && control.hasError('min')) {
      return 'Selecciona un cliente.';
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

  mensajeErrorResolucion(campo: string): string {
    const control = this.formularioResolucion.get(campo);

    if (!control || control.valid || !(control.touched || control.dirty)) {
      return '';
    }

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
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

    return 'El valor ingresado no es válido.';
  }

  private abrirResolucion(
    solicitud: SolicitudPrestamo,
    estado: 'APROBADA' | 'RECHAZADA'
  ): void {
    this.solicitudAResolver = solicitud;
    this.estadoResolucion = estado;

    this.formularioResolucion.reset({
      comentario: '',
      tasaInteresAnual: 12.5
    });
  }
}
