package com.chn.prestamos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(
    name = "clientes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cliente_identificacion",
            columnNames = "numero_identificacion"
        )
    }
)
public class Cliente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+(?:[ '-][A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$",
        message = "El nombre solo puede contener letras y espacios"
    )
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+(?:[ '-][A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$",
        message = "El apellido solo puede contener letras y espacios"
    )
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank(message = "El número de identificación es obligatorio")
    @Size(max = 25)
    @Pattern(
        regexp = "^[0-9]+$",
        message = "El número de identificación solo puede contener dígitos"
    )
    @Column(name = "numero_identificacion", nullable = false, length = 25)
    private String numeroIdentificacion;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 250)
    @Pattern(
        regexp = "^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü #.,\\-/]+$",
        message = "La dirección solo puede contener letras, números y signos como # . , - /"
    )
    @Column(nullable = false, length = 250)
    private String direccion;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String correoElectronico;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20)
    @Pattern(
        regexp = "^(?=.*[0-9])[0-9+()\\- ]+$",
        message = "El teléfono solo puede contener dígitos y los signos + ( ) -"
    )
    @Column(nullable = false, length = 20)
    private String telefono;

    public Cliente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}