import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Modal } from '../../components/modal/modal';
import { Cliente } from '../../models/cliente';
import { ClienteService } from '../../services/cliente.service';
import { ToastrService } from '../../services/toastr.service';

const PATRON_NOMBRE =
  /^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+(?:[ '-][A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$/;
const PATRON_IDENTIFICACION = /^[0-9]+$/;
const PATRON_DIRECCION = /^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü #.,\-/]+$/;
const PATRON_TELEFONO = /^(?=.*[0-9])[0-9+()\- ]+$/;

const MENSAJES_PATRON: Record<string, string> = {
  nombre: 'El nombre solo puede contener letras y espacios.',
  apellido: 'El apellido solo puede contener letras y espacios.',
  numeroIdentificacion: 'La identificación solo puede contener números.',
  direccion:
    'La dirección solo admite letras, números y signos como # . , - /',
  telefono: 'El teléfono solo puede contener números y los signos + ( ) -'
};

@Component({
  selector: 'app-clientes',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Modal
  ],
  templateUrl: './clientes.html',
  styleUrl: './clientes.scss'
})
export class Clientes implements OnInit {

  private readonly clienteService = inject(ClienteService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly toastr = inject(ToastrService);

  clientes: Cliente[] = [];
  editandoId: number | null = null;
  clienteAEliminar: Cliente | null = null;
  cargando = false;
  guardando = false;
  eliminando = false;

  readonly fechaMaxima = this.construirFechaMaxima();

  readonly formulario = this.formBuilder.nonNullable.group({
    nombre: [
      '',
      [
        Validators.required,
        Validators.maxLength(100),
        Validators.pattern(PATRON_NOMBRE)
      ]
    ],
    apellido: [
      '',
      [
        Validators.required,
        Validators.maxLength(100),
        Validators.pattern(PATRON_NOMBRE)
      ]
    ],
    numeroIdentificacion: [
      '',
      [
        Validators.required,
        Validators.maxLength(25),
        Validators.pattern(PATRON_IDENTIFICACION)
      ]
    ],
    fechaNacimiento: [
      '',
      [
        Validators.required,
        (control) => this.validarFechaNacimiento(control)
      ]
    ],
    direccion: [
      '',
      [
        Validators.required,
        Validators.maxLength(250),
        Validators.pattern(PATRON_DIRECCION)
      ]
    ],
    correoElectronico: [
      '',
      [
        Validators.required,
        Validators.email,
        Validators.maxLength(150)
      ]
    ],
    telefono: [
      '',
      [
        Validators.required,
        Validators.maxLength(20),
        Validators.pattern(PATRON_TELEFONO)
      ]
    ]
  });

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.cargando = true;

    this.clienteService.listar().subscribe({
      next: (respuesta) => {
        this.clientes = Array.isArray(respuesta)
          ? respuesta
          : Object.values(respuesta);

        this.cargando = false;
        this.changeDetector.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar clientes:', error);
        this.toastr.error('No fue posible cargar los clientes.');
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

    const cliente: Cliente = this.formulario.getRawValue();

    const operacion = this.editandoId === null
      ? this.clienteService.crear(cliente)
      : this.clienteService.actualizar(
          this.editandoId,
          cliente
        );

    operacion.subscribe({
      next: () => {
        this.toastr.exito(
          this.editandoId === null
            ? 'Cliente registrado correctamente.'
            : 'Cliente actualizado correctamente.'
        );

        this.cancelarEdicion();
        this.cargarClientes();
        this.guardando = false;
      },
      error: (error) => {
        this.toastr.error(
          error?.error?.mensaje
          ?? 'No fue posible guardar la información del cliente.'
        );
        this.guardando = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  editar(cliente: Cliente): void {
    if (cliente.id === undefined) {
      return;
    }

    this.editandoId = cliente.id;

    this.formulario.setValue({
      nombre: cliente.nombre,
      apellido: cliente.apellido,
      numeroIdentificacion: cliente.numeroIdentificacion,
      fechaNacimiento: cliente.fechaNacimiento,
      direccion: cliente.direccion,
      correoElectronico: cliente.correoElectronico,
      telefono: cliente.telefono
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  eliminar(cliente: Cliente): void {
    if (cliente.id === undefined) {
      return;
    }

    this.clienteAEliminar = cliente;
    this.changeDetector.detectChanges();
  }

  cancelarEliminacion(): void {
    this.clienteAEliminar = null;
    this.changeDetector.detectChanges();
  }

  confirmarEliminacion(): void {
    const cliente = this.clienteAEliminar;

    if (!cliente?.id) {
      return;
    }

    this.eliminando = true;

    this.clienteService.eliminar(cliente.id).subscribe({
      next: () => {
        this.toastr.exito('Cliente eliminado correctamente.');
        this.clienteAEliminar = null;
        this.eliminando = false;
        this.cargarClientes();
      },
      error: (error) => {
        this.toastr.error(
          error?.error?.mensaje
          ?? 'No fue posible eliminar el cliente.'
        );
        this.eliminando = false;
      }
    });
  }

  cancelarEdicion(): void {
    this.editandoId = null;
    this.formulario.reset({
      nombre: '',
      apellido: '',
      numeroIdentificacion: '',
      fechaNacimiento: '',
      direccion: '',
      correoElectronico: '',
      telefono: ''
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

    if (control.hasError('email')) {
      return 'Ingresa un correo electrónico válido.';
    }

    if (control.hasError('maxlength')) {
      const { requiredLength } = control.getError('maxlength');
      return `No puede superar ${requiredLength} caracteres.`;
    }

    if (control.hasError('fechaInvalida')) {
      return 'Ingresa una fecha válida.';
    }

    if (control.hasError('fechaFutura')) {
      return 'La fecha no puede ser hoy ni futura.';
    }

    if (control.hasError('pattern')) {
      return MENSAJES_PATRON[campo] ?? 'El formato no es válido.';
    }

    return 'El valor ingresado no es válido.';
  }

  private construirFechaMaxima(): string {
    const hoy = new Date();
    const mes = `${hoy.getMonth() + 1}`.padStart(2, '0');
    const dia = `${hoy.getDate()}`.padStart(2, '0');

    return `${hoy.getFullYear()}-${mes}-${dia}`;
  }

  private validarFechaNacimiento(
    control: AbstractControl
  ): ValidationErrors | null {
    const valor = control.value as string;

    if (!valor) {
      return null;
    }

    const partes = valor.split('-').map(Number);

    if (partes.length !== 3 || partes.some((parte) => Number.isNaN(parte))) {
      return { fechaInvalida: true };
    }

    const [anio, mes, dia] = partes;
    const fecha = new Date(anio, mes - 1, dia);

    if (
      fecha.getFullYear() !== anio
      || fecha.getMonth() !== mes - 1
      || fecha.getDate() !== dia
    ) {
      return { fechaInvalida: true };
    }

    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    if (fecha >= hoy) {
      return { fechaFutura: true };
    }

    return null;
  }
}