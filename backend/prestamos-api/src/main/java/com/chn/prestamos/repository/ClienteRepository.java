package com.chn.prestamos.repository;

import com.chn.prestamos.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByNumeroIdentificacionAndIdNot(
        String numeroIdentificacion,
        Long id
    );
}