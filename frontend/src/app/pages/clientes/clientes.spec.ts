import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Cliente } from '../../models/cliente';
import { Clientes } from './clientes';

const BASE = '/api/clientes';

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

describe('Clientes', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Clientes],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  function crearComponente(clientes: Cliente[] = []): Clientes {
    const fixture = TestBed.createComponent(Clientes);
    fixture.detectChanges();
    http.expectOne(BASE).flush(clientes);
    fixture.detectChanges();

    return fixture.componentInstance;
  }

  it('rechaza nombres con números', () => {
    const componente = crearComponente();
    const nombre = componente.formulario.get('nombre');

    nombre?.setValue('Ana1');
    nombre?.markAsTouched();

    expect(nombre?.hasError('pattern')).toBe(true);
    expect(componente.mensajeError('nombre')).toContain('letras');
  });

  it('rechaza identificaciones con letras o guiones', () => {
    const componente = crearComponente();
    const identificacion =
      componente.formulario.get('numeroIdentificacion');

    identificacion?.setValue('123-ABC');
    identificacion?.markAsTouched();

    expect(identificacion?.hasError('pattern')).toBe(true);
  });

  it('acepta un cliente con datos válidos', () => {
    const componente = crearComponente();

    componente.formulario.setValue({
      nombre: 'Ana María',
      apellido: 'Gómez',
      numeroIdentificacion: '1234567890101',
      fechaNacimiento: '1990-05-20',
      direccion: '5ta avenida 1-23 zona 1',
      correoElectronico: 'ana@example.com',
      telefono: '5555-5555'
    });

    expect(componente.formulario.valid).toBe(true);
  });

  it('marca el cliente a eliminar para confirmar en modal', () => {
    const componente = crearComponente([cliente]);

    componente.eliminar(cliente);

    expect(componente.clienteAEliminar).toEqual(cliente);
  });
});
