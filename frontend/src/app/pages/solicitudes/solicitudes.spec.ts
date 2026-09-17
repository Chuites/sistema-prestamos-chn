import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Cliente } from '../../models/cliente';
import { SolicitudPrestamo } from '../../models/solicitud-prestamo';
import { Solicitudes } from './solicitudes';

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

function solicitud(
  id: number,
  cliente: Cliente,
  estado: SolicitudPrestamo['estado']
): SolicitudPrestamo {
  return {
    id,
    cliente,
    montoSolicitado: 1000,
    plazoMeses: 12,
    destinoPrestamo: 'Compra',
    observaciones: '',
    fechaSolicitud: '2024-01-01T10:00:00',
    estado,
    fechaResolucion: null,
    comentarioResolucion: null
  };
}

const solicitudes = [
  solicitud(1, clienteUno, 'EN_PROCESO'),
  solicitud(2, clienteUno, 'APROBADA'),
  solicitud(3, clienteDos, 'EN_PROCESO')
];

describe('Solicitudes', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Solicitudes],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  function crearComponente(): Solicitudes {
    const fixture = TestBed.createComponent(Solicitudes);
    fixture.detectChanges();

    http.expectOne(`${BASE}/clientes`)
      .flush([clienteUno, clienteDos]);
    http.expectOne(`${BASE}/solicitudes`).flush(solicitudes);
    fixture.detectChanges();

    return fixture.componentInstance;
  }

  it('lista todas las solicitudes sin filtros', () => {
    const componente = crearComponente();

    expect(componente.solicitudesFiltradas).toHaveLength(3);
  });

  it('filtra por cliente', () => {
    const componente = crearComponente();

    componente.filtroCliente.setValue(1);

    expect(componente.solicitudesFiltradas).toHaveLength(2);
    expect(
      componente.solicitudesFiltradas.every(
        (item) => item.cliente.id === 1
      )
    ).toBe(true);
  });

  it('filtra por estado', () => {
    const componente = crearComponente();

    componente.filtroEstado.setValue('EN_PROCESO');

    expect(componente.solicitudesFiltradas).toHaveLength(2);
  });

  it('combina filtros de cliente y estado', () => {
    const componente = crearComponente();

    componente.filtroCliente.setValue(1);
    componente.filtroEstado.setValue('EN_PROCESO');

    expect(componente.solicitudesFiltradas).toHaveLength(1);
    expect(componente.solicitudesFiltradas[0].id).toBe(1);
  });

  it('cuenta solicitudes por estado', () => {
    const componente = crearComponente();

    expect(componente.totalPorEstado('EN_PROCESO')).toBe(2);
    expect(componente.totalPorEstado('APROBADA')).toBe(1);
    expect(componente.totalPorEstado('RECHAZADA')).toBe(0);
  });
});
