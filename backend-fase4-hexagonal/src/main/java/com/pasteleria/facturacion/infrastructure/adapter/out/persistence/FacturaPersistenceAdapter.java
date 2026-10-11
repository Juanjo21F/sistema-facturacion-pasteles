package com.pasteleria.facturacion.infrastructure.adapter.out.persistence;

import com.pasteleria.facturacion.application.port.out.FacturaRepositoryPort;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.FacturaEntity;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.ClienteRepository;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.FacturaRepository;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.ProductoRepository;
import com.pasteleria.facturacion.domain.model.Factura;

import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adaptador de salida: implementa el puerto de facturas con Spring Data JPA. */
@Component
public class FacturaPersistenceAdapter implements FacturaRepositoryPort {

    private final FacturaRepository facturas;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;

    public FacturaPersistenceAdapter(FacturaRepository facturas, ClienteRepository clientes, ProductoRepository productos) {
        this.facturas = facturas;
        this.clientes = clientes;
        this.productos = productos;
    }

    @Override
    public Factura guardar(Factura factura) {
        FacturaEntity guardada;
        if (factura.getId() == null) {
            guardada = facturas.saveAndFlush(PersistenceMapper.aEntidad(factura,
                    clientes.getReferenceById(factura.getCliente().getId()), productos::getReferenceById));
        } else {
            // una factura emitida solo cambia de estado (EMITIDA -> ANULADA)
            guardada = facturas.findByIdParaActualizar(factura.getId()).orElseThrow();
            guardada.setEstadoFactura(factura.getEstado());
            facturas.saveAndFlush(guardada);
        }
        return facturas.findByIdConDetalles(guardada.getIdFactura()).map(PersistenceMapper::aDominio).orElseThrow();
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return facturas.findByIdConDetalles(id).map(PersistenceMapper::aDominio);
    }

    @Override
    public Optional<Factura> buscarPorIdParaActualizar(Long id) {
        return facturas.findByIdParaActualizar(id).map(PersistenceMapper::aDominio);
    }

    @Override
    public long siguienteSecuencia() {
        return facturas.siguienteSecuencia();
    }
}
