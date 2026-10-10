package com.pasteleria.facturacion.dataaccess.repository;

import com.pasteleria.facturacion.dataaccess.entity.ClienteEntity;
import com.pasteleria.facturacion.dataaccess.entity.EstadoRegistro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByDocumentoIdentidad(String documentoIdentidad);

    List<ClienteEntity> findByEstadoOrderByNombreCompleto(EstadoRegistro estado);

    List<ClienteEntity> findAllByOrderByNombreCompleto();
}
