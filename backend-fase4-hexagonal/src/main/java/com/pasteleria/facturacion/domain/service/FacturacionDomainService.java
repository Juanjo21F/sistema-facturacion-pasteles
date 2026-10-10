package com.pasteleria.facturacion.domain.service;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.entity.DetalleFactura;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.valueobject.LineaVenta;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Coordina las reglas de una venta que involucran varias entidades. No conoce bases de datos ni HTTP. */
public class FacturacionDomainService {

    private final InventarioDomainService inventario;

    public FacturacionDomainService(InventarioDomainService inventario) {
        this.inventario = inventario;
    }

    public Factura emitirFactura(Cliente cliente, List<LineaVenta> lineas, UsuarioAutenticado usuario,
                                 long secuencia, LocalDateTime fecha) {
        validarLineas(lineas);
        List<DetalleFactura> detalles = lineas.stream().map(DetalleFactura::desde).toList();
        Factura factura = Factura.emitir(generarNumero(secuencia, fecha), fecha, cliente, usuario.id(), detalles);
        inventario.descontarStock(lineas);
        return factura;
    }

    /** Cambia el estado a ANULADA y restaura el stock. Si ya estaba anulada lanza excepción ANTES de tocar stock. */
    public void anularFactura(Factura factura, Map<Long, Producto> productosPorId) {
        factura.anular();
        inventario.restaurarStock(factura, productosPorId);
    }

    public String generarNumero(long secuencia, LocalDateTime fecha) {
        return String.format("FAC-%d-%06d", fecha.getYear(), secuencia);
    }

    private void validarLineas(List<LineaVenta> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un producto");
        }
        Set<Long> vistos = new HashSet<>();
        for (LineaVenta linea : lineas) {
            linea.producto().validarDisponibleParaVenta();
            if (!vistos.add(linea.producto().getIdProducto())) {
                throw new ReglaDeNegocioException(
                        "El producto \"" + linea.producto().getNombre() + "\" está repetido en la factura");
            }
        }
    }
}
