package com.pasteleria.facturacion.support;

import com.pasteleria.facturacion.domain.entity.DetalleFactura;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.port.output.FacturaRepositoryPort;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryFacturaRepository implements FacturaRepositoryPort {

    private final Map<Long, Factura> datos = new LinkedHashMap<>();
    private final AtomicLong id = new AtomicLong(0);
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Factura guardar(Factura f) {
        if (f.getIdFactura() == null) {
            List<DetalleFactura> detalles = f.getDetalles().stream()
                    .map(d -> new DetalleFactura(id.incrementAndGet(), d.getProducto(), d.getCantidad(), d.getPrecioUnitario()))
                    .toList();
            Factura nueva = Factura.reconstruir(id.incrementAndGet(), f.getNumeroFactura(), f.getFechaEmision(),
                    f.getCliente(), f.getUsuarioAuthcore(), f.getEstadoFactura(), detalles);
            datos.put(nueva.getIdFactura(), nueva);
            return nueva;
        }
        datos.put(f.getIdFactura(), f);
        return f;
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Optional<Factura> buscarPorIdConBloqueo(Long id) {
        return buscarPorId(id);
    }

    @Override
    public long siguienteSecuencia() {
        return secuencia.incrementAndGet();
    }

    public int cantidadGuardada() {
        return datos.size();
    }
}
