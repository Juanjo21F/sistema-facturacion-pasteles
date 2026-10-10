package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.application.service.AuditoriaService;
import com.pasteleria.facturacion.domain.entity.DetalleFactura;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.FacturaNoEncontradaException;
import com.pasteleria.facturacion.domain.port.input.AnularFacturaUseCase;
import com.pasteleria.facturacion.domain.port.output.FacturaRepositoryPort;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.service.FacturacionDomainService;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Anula la factura y restaura el stock en una sola transacción. */
@Service
@Transactional
public class AnularFacturaUseCaseImpl implements AnularFacturaUseCase {

    private final FacturaRepositoryPort facturas;
    private final ProductoRepositoryPort productos;
    private final FacturacionDomainService facturacion;
    private final AuditoriaService auditoria;

    public AnularFacturaUseCaseImpl(FacturaRepositoryPort facturas, ProductoRepositoryPort productos,
                                    FacturacionDomainService facturacion, AuditoriaService auditoria) {
        this.facturas = facturas;
        this.productos = productos;
        this.facturacion = facturacion;
        this.auditoria = auditoria;
    }

    @Override
    public Factura ejecutar(Long idFactura, UsuarioAutenticado usuario) {
        Factura factura = facturas.buscarPorIdConBloqueo(idFactura)
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura));

        List<Long> ids = factura.getDetalles().stream()
                .map(DetalleFactura::getProducto).map(Producto::getIdProducto).distinct().toList();
        Map<Long, Producto> productosBloqueados = productos.buscarPorIdsConBloqueo(ids).stream()
                .collect(Collectors.toMap(Producto::getIdProducto, Function.identity()));

        facturacion.anularFactura(factura, productosBloqueados);

        productosBloqueados.values().forEach(productos::guardar);
        Factura guardada = facturas.guardar(factura);

        auditoria.registrar(usuario, "ANULAR_FACTURA", "factura=" + guardada.getNumeroFactura());
        return guardada;
    }
}
