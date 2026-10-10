package com.pasteleria.facturacion.support;

import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryProductoRepository implements ProductoRepositoryPort {

    private final Map<Long, Producto> datos = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Producto guardar(Producto p) {
        if (p.getIdProducto() == null) {
            Producto nuevo = Producto.reconstruir(secuencia.incrementAndGet(), p.getCodigoUnico(), p.getNombre(),
                    p.getCategoria(), p.getPrecio(), p.getStock(), p.getEstado());
            datos.put(nuevo.getIdProducto(), nuevo);
            return nuevo;
        }
        datos.put(p.getIdProducto(), p);
        return p;
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return datos.values().stream().filter(p -> p.getCodigoUnico().equals(codigo)).findFirst();
    }

    @Override
    public List<Producto> buscarTodos(boolean soloActivos) {
        return datos.values().stream().filter(p -> !soloActivos || p.getEstado() == EstadoRegistro.ACTIVO).toList();
    }

    @Override
    public List<Producto> buscarPorIdsConBloqueo(Collection<Long> ids) {
        return ids.stream().map(datos::get).filter(Objects::nonNull).toList();
    }
}
