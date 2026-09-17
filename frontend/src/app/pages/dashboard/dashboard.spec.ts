import {
  provideHttpClient
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Cliente } from '../../models/cliente';
import { Pago } from '../../models/pago';
import { Prestamo } from '../../models/prestamo';
import { SolicitudPrestamo } from '../../models/solicitud-prestamo';
import { Dashboard } from './dashboard';

const BASE = '/api';

const cliente: Cliente = {
  id: 1,
  nombre: 'Ana',
  apellido: 'Gómez',
  numeroIdentificacion: '1234567890101',
  fechaNacimiento: '1990-05-20',
  direccion: '5ta avenida 1-23',
  correoElectronico: 'ana@example.com',
  telefono: '5555-5555'
};

const solicitudPendiente: SolicitudPrestamo = {
  id: 1,
  cliente,
  montoSolicitado: 1000,
  plazoMeses: 12,
  destinoPrestamo: 'Compra',
  observaciones: '',
  fechaSolicitud: '2024-01-01T10:00:00',
  estado: 'EN_PROCESO',
  fechaResolucion: null,
  comentarioResolucion: null
};

const solicitudAprobada: SolicitudPrestamo = {
  ...solicitudPendiente,
  id: 2,
  estado: 'APROBADA',
  comentarioResolucion: 'Aprobado'
};

const prestamo: Prestamo = {
  id: 1,
  solicitud: solicitudAprobada,
  cliente,
  montoAprobado: 1000,
  tasaInteresAnual: 12.5,
  plazoMeses: 12,
  montoPagado: 400,
  saldoPendiente: 600,
  estado: 'PARCIAL',
  fechaAprobacion: '2024-01-02T10:00:00'
};

const pago: Pago = {
  id: 1,
  prestamo,
  monto: 400,
  fechaPago: '2024-02-01T10:00:00',
  metodoPago: 'EFECTIVO',
  numeroRecibo: 'REC-1',
  observaciones: ''
};

describe('Dashboard', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  function crearConRespuestas(): Dashboard {
    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();

    http.expectOne(`${BASE}/clientes`).flush([cliente]);
    http.expectOne(`${BASE}/solicitudes`)
      .flush([solicitudPendiente, solicitudAprobada]);
    http.expectOne(`${BASE}/prestamos`).flush([prestamo]);
    http.expectOne(`${BASE}/pagos`).flush([pago]);
    fixture.detectChanges();

    return fixture.componentInstance;
  }

  it('calcula los totales generales', () => {
    const dashboard = crearConRespuestas();

    expect(dashboard.totalClientes).toBe(1);
    expect(dashboard.totalSolicitudes).toBe(2);
    expect(dashboard.solicitudesPendientes).toBe(1);
    expect(dashboard.totalPrestamos).toBe(1);
    expect(dashboard.totalPagos).toBe(1);
  });

  it('calcula montos y saldo pendiente', () => {
    const dashboard = crearConRespuestas();

    expect(dashboard.montoPrestado).toBe(1000);
    expect(dashboard.montoPagado).toBe(400);
    expect(dashboard.saldoPendiente).toBe(600);
  });

  it('agrupa los pagos por cliente', () => {
    const dashboard = crearConRespuestas();

    expect(dashboard.resumenPorCliente).toHaveLength(1);
    expect(dashboard.resumenPorCliente[0].pagos).toBe(1);
    expect(dashboard.resumenPorCliente[0].montoPagado).toBe(400);
    expect(dashboard.resumenPorCliente[0].solicitudes).toBe(2);
    expect(dashboard.resumenPorCliente[0].prestamos).toBe(1);
  });
});
