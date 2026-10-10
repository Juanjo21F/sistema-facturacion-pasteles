package com.pasteleria.facturacion.model.repository;

import com.pasteleria.facturacion.model.entity.Cliente;
import com.pasteleria.facturacion.model.enums.EstadoRegistro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDocumentoIdentidad(String documentoIdentidad);

    List<Cliente> findByEstadoOrderByNombreCompleto(EstadoRegistro estado);

    List<Cliente> findAllByOrderByNombreCompleto();
}
