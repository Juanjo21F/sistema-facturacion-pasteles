package com.pasteleria.facturacion.domain.port.output;

import com.pasteleria.facturacion.domain.entity.Producto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(Long id);

    Optional<Producto> buscarPorCodigo(String codigoUnico);

    List<Producto> buscarTodos(boolean soloActivos);

    /** Carga los productos bloqueándolos para evitar que dos ventas simultáneas descuenten el mismo stock. */
    List<Producto> buscarPorIdsConBloqueo(Collection<Long> ids);
}
