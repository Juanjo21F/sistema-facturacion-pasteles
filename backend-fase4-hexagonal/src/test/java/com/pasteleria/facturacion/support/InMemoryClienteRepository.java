package com.pasteleria.facturacion.support;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.port.output.ClienteRepositoryPort;
import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryClienteRepository implements ClienteRepositoryPort {

    private final Map<Long, Cliente> datos = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Cliente guardar(Cliente c) {
        if (c.getIdCliente() == null) {
            Cliente nuevo = Cliente.reconstruir(secuencia.incrementAndGet(), c.getDocumentoIdentidad(),
                    c.getNombreCompleto(), c.getTelefono(), c.getCorreo(), c.getEstado());
            datos.put(nuevo.getIdCliente(), nuevo);
            return nuevo;
        }
        datos.put(c.getIdCliente(), c);
        return c;
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return datos.values().stream().filter(c -> c.getDocumentoIdentidad().equals(documento)).findFirst();
    }

    @Override
    public List<Cliente> buscarTodos(boolean soloActivos) {
        return datos.values().stream().filter(c -> !soloActivos || c.getEstado() == EstadoRegistro.ACTIVO).toList();
    }
}
