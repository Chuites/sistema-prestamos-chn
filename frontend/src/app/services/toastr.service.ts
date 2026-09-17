import { Injectable, signal } from '@angular/core';

export type ToastrTipo = 'exito' | 'error' | 'info' | 'advertencia';

export interface ToastrMensaje {
  id: number;
  tipo: ToastrTipo;
  titulo: string;
  texto: string;
}

const DURACION_MS = 5000;

@Injectable({
  providedIn: 'root'
})
export class ToastrService {

  private contador = 0;

  readonly mensajes = signal<ToastrMensaje[]>([]);

  exito(texto: string, titulo = 'Éxito'): void {
    this.mostrar('exito', titulo, texto);
  }

  error(texto: string, titulo = 'Error'): void {
    this.mostrar('error', titulo, texto);
  }

  info(texto: string, titulo = 'Información'): void {
    this.mostrar('info', titulo, texto);
  }

  advertencia(texto: string, titulo = 'Advertencia'): void {
    this.mostrar('advertencia', titulo, texto);
  }

  cerrar(id: number): void {
    this.mensajes.update((lista) =>
      lista.filter((mensaje) => mensaje.id !== id)
    );
  }

  private mostrar(
    tipo: ToastrTipo,
    titulo: string,
    texto: string
  ): void {
    const id = ++this.contador;

    this.mensajes.update((lista) => [
      ...lista,
      { id, tipo, titulo, texto }
    ]);

    setTimeout(() => this.cerrar(id), DURACION_MS);
  }
}
