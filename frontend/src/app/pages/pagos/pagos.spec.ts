import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Cliente } from '../../models/cliente';
import { Pago } from '../../models/pago';
import { Prestamo } from '../../models/prestamo';
import { SolicitudPrestamo } from '../../models/solicitud-prestamo';
import { Pagos } from './pagos';

const BASE = '/api';

const clienteUno: Cliente = {
  id: 1,
  nombre: 'Ana',
  apellido: 'Gómez',
  numeroIdentificacion: '1',
  fechaNacimiento: '1990-05-20',
  direccion: 'Dirección 1',
  correoElectronico: 'ana@example.com',
  telefono: '5555-5555'
};

const clienteDos: Cliente = {
  ...clienteUno,
  id: 2,
  nombre: 'Luis',
  numeroIdentificacion: '2'
};

const solicitud: SolicitudPrestamo = {
  id: 1,
  cliente: clienteUno,
  montoSolicitado: 1000,
  plazoMeses: 12,
  destinoPrestamo: 'Compra',
  observaciones: '',
  fechaSolicitud: '2024-01-01T10:00:00',
  estado: 'APROBADA',
  fechaResolucion: null,
  comentarioResolucion: null
};

function prestamo(id: number, cliente: Cliente): Prestamo {
  return {
    id,
    solicitud,
    cliente,
    montoAprobado: 1000,
    tasaInteresAnual: 12.5,
    plazoMeses: 12,
    montoPagado: 0,
    saldoPendiente: 1000,
    estado: 'PENDIENTE',
    fechaAprobacion: '2024-01-02T10:00:00'
  };
}

const prestamoUno = prestamo(1, clienteUno);
const prestamoDos = prestamo(2, clienteDos);

function pago(id: number, prestamoRef: Prestamo): Pago {
  return {
    id,
    prestamo: prestamoRef,
    monto: 100,
    fechaPago: '2024-02-01T10:00:00',
    metodoPago: 'EFECTIVO',
    numeroRecibo: `REC-${id}`,
    observaciones: ''
  };
}

const pagos = [pago(1, prestamoUno), pago(2, prestamoDos)];

describe('Pagos', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Pagos],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  function flushCarga(): void {
    http.expectOne(`${BASE}/prestamos`)
      .flush([prestamoUno, prestamoDos]);
    http.expectOne(`${BASE}/pagos`).flush(pagos);
    http.expectOne(`${BASE}/clientes`)
      .flush([clienteUno, clienteDos]);
  }

  function crearComponente(): Pagos {
    const fixture = TestBed.createComponent(Pagos);
    fixture.detectChanges();
    flushCarga();
    fixture.detectChanges();

    return fixture.componentInstance;
  }

  it('filtra los pagos por cliente', () => {
    const componente = crearComponente();

    expect(componente.pagosFiltrados).toHaveLength(2);

    componente.filtroCliente.setValue(1);

    expect(componente.pagosFiltrados).toHaveLength(1);
    expect(componente.pagosFiltrados[0].prestamo.cliente.id).toBe(1);
  });

  it('rechaza montos menores al mínimo', () => {
    const componente = crearComponente();
    const monto = componente.formulario.get('monto');

    monto?.setValue(0);
    monto?.markAsTouched();

    expect(monto?.hasError('min')).toBe(true);
  });

  it('rechaza números de recibo con caracteres inválidos', () => {
    const componente = crearComponente();
    const recibo = componente.formulario.get('numeroRecibo');

    recibo?.setValue('REC 0001');
    recibo?.markAsTouched();

    expect(recibo?.hasError('pattern')).toBe(true);
  });

  it('registra un pago válido', () => {
    const componente = crearComponente();

    componente.formulario.setValue({
      prestamoId: 1,
      monto: 100,
      numeroRecibo: 'REC-9',
      observaciones: 'Abono'
    });

    componente.registrar();

    const peticion = http.expectOne(`${BASE}/pagos/prestamo/1`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body.monto).toBe(100);
    peticion.flush(pago(9, prestamoUno));

    flushCarga();
  });

  it('muestra en la tabla solo los pagos del cliente filtrado', () => {
    const fixture = TestBed.createComponent(Pagos);
    fixture.detectChanges();
    flushCarga();
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelectorAll('tbody tr').length
    ).toBe(2);

    fixture.componentInstance.filtroCliente.setValue(1);
    fixture.detectChanges();

    const filas =
      fixture.nativeElement.querySelectorAll('tbody tr');

    expect(filas.length).toBe(1);
    expect(filas[0].textContent).toContain('REC-1');
  });
});
