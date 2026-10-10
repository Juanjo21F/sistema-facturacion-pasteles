package com.pasteleria.facturacion.infrastructure.persistence.adapter;

import com.pasteleria.facturacion.domain.entity.DetalleFactura;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.exception.FacturaNoEncontradaException;
import com.pasteleria.facturacion.domain.port.output.FacturaRepositoryPort;
import com.pasteleria.facturacion.infrastructure.persistence.entity.DetalleFacturaJpaEntity;
import com.pasteleria.facturacion.infrastructure.persistence.entity.FacturaJpaEntity;
import com.pasteleria.facturacion.infrastructure.persistence.repository.ClienteJpaRepository;
import com.pasteleria.facturacion.infrastructure.persistence.repository.FacturaJpaRepository;
import com.pasteleria.facturacion.infrastructure.persistence.repository.ProductoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FacturaRepositoryAdapter implements FacturaRepositoryPort {

    private final FacturaJpaRepository jpa;
    private final ClienteJpaRepository clienteJpa;
    private final ProductoJpaRepository productoJpa;

    public FacturaRepositoryAdapter(FacturaJpaRepository jpa, ClienteJpaRepository clienteJpa,
                                    ProductoJpaRepository productoJpa) {
        this.jpa = jpa;
        this.clienteJpa = clienteJpa;
        this.productoJpa = productoJpa;
    }

    @Override
    public Factura guardar(Factura factura) {
        return factura.getIdFactura() == null ? insertar(factura) : actualizarEstado(factura);
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return jpa.findByIdConDetalles(id).map(PersistenceMapper::aDominio);
    }

    @Override
    public Optional<Factura> buscarPorIdConBloqueo(Long id) {
        if (jpa.findByIdParaActualizar(id).isEmpty()) {   // SELECT ... FOR UPDATE sobre la factura
            return Optional.empty();
        }
        return buscarPorId(id);                           // luego se cargan detalles y productos
    }

    @Override
    public long siguienteSecuencia() {
        return jpa.siguienteSecuencia();
    }

    private Factura insertar(Factura factura) {
        FacturaJpaEntity entidad = new FacturaJpaEntity();
        entidad.setNumeroFactura(factura.getNumeroFactura());
        entidad.setFechaEmision(factura.getFechaEmision());
        entidad.setCliente(clienteJpa.getReferenceById(factura.getCliente().getIdCliente()));
        entidad.setUsuarioAuthcore(factura.getUsuarioAuthcore());
        entidad.setTotal(factura.getTotal());
        entidad.setEstado(factura.getEstadoFactura());
        for (DetalleFactura d : factura.getDetalles()) {
            DetalleFacturaJpaEntity detalle = new DetalleFacturaJpaEntity();
            detalle.setFactura(entidad);
            detalle.setProducto(productoJpa.getReferenceById(d.getProducto().getIdProducto()));
            detalle.setCantidad(d.getCantidad());
            detalle.setPrecioUnitario(d.getPrecioUnitario());
            detalle.setSubtotal(d.getSubtotal());
            entidad.getDetalles().add(detalle);
        }
        FacturaJpaEntity guardada = jpa.saveAndFlush(entidad);

        List<DetalleFactura> conIds = new ArrayList<>();
        for (int i = 0; i < factura.getDetalles().size(); i++) {
            DetalleFactura original = factura.getDetalles().get(i);
            conIds.add(new DetalleFactura(guardada.getDetalles().get(i).getId(), original.getProducto(),
                    original.getCantidad(), original.getPrecioUnitario()));
        }
        return Factura.reconstruir(guardada.getId(), factura.getNumeroFactura(), factura.getFechaEmision(),
                factura.getCliente(), factura.getUsuarioAuthcore(), factura.getEstadoFactura(), conIds);
    }

    private Factura actualizarEstado(Factura factura) {
        FacturaJpaEntity entidad = jpa.findById(factura.getIdFactura())
                .orElseThrow(() -> new FacturaNoEncontradaException(factura.getIdFactura()));
        entidad.setEstado(factura.getEstadoFactura());
        jpa.saveAndFlush(entidad);
        return factura;
    }
}
