package com.pasteleria.facturacion.infrastructure.persistence.repository;

import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;
import com.pasteleria.facturacion.infrastructure.persistence.entity.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {

    Optional<ClienteJpaEntity> findByDocumentoIdentidad(String documentoIdentidad);

    List<ClienteJpaEntity> findByEstadoOrderByNombreCompleto(EstadoRegistro estado);

    List<ClienteJpaEntity> findAllByOrderByNombreCompleto();
}
