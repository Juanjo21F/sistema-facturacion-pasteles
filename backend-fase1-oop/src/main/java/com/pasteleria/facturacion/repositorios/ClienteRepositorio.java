package com.pasteleria.facturacion.repositorios;

import com.pasteleria.facturacion.modelo.entidades.Cliente;
import com.pasteleria.facturacion.modelo.enumeraciones.EstadoRegistro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDocumentoIdentidad(String documentoIdentidad);

    List<Cliente> findByEstadoOrderByNombreCompleto(EstadoRegistro estado);

    List<Cliente> findAllByOrderByNombreCompleto();
}
