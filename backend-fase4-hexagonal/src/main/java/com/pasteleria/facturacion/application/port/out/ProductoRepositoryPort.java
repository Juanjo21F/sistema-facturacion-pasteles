package com.pasteleria.facturacion.application.port.out;

import com.pasteleria.facturacion.domain.model.Producto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: persistencia de productos. */
public interface ProductoRepositoryPort {

    Producto guardar(Producto producto);

    void guardarTodos(Collection<Producto> productos);

    Optional<Producto> buscarPorId(Long id);

    Optional<Producto> buscarPorCodigo(String codigoUnico);

    List<Producto> listar(boolean soloActivos);

    /** Debe bloquear las filas (lectura para actualizar), ordenadas por id para evitar deadlocks entre ventas concurrentes. */
    List<Producto> buscarPorIdsParaActualizar(Collection<Long> ids);
}
