package com.pasteleria.facturacion.domain.entity;

import com.pasteleria.facturacion.domain.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.valueobject.EstadoFactura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Agregado raíz: la factura es dueña de sus detalles y calcula su propio total. */
public class Factura {

    private final Long idFactura;
    private final String numeroFactura;
    private final LocalDateTime fechaEmision;
    private final Cliente cliente;
    private final String usuarioAuthcore;
    private final BigDecimal total;
    private EstadoFactura estadoFactura;
    private final List<DetalleFactura> detalles;

    private Factura(Long id, String numero, LocalDateTime fecha, Cliente cliente, String usuario,
                    EstadoFactura estado, List<DetalleFactura> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un detalle");
        }
        this.idFactura = id;
        this.numeroFactura = Validaciones.textoObligatorio(numero, "numeroFactura");
        this.fechaEmision = Objects.requireNonNull(fecha);
        this.cliente = Objects.requireNonNull(cliente);
        this.usuarioAuthcore = Validaciones.textoObligatorio(usuario, "usuarioAuthcore");
        this.estadoFactura = estado;
        this.detalles = List.copyOf(detalles);
        this.total = this.detalles.stream()
                .map(DetalleFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Emite una factura nueva. El total se calcula siempre en el dominio. */
    public static Factura emitir(String numero, LocalDateTime fecha, Cliente cliente,
                                 String usuario, List<DetalleFactura> detalles) {
        cliente.validarActivo();
        return new Factura(null, numero, fecha, cliente, usuario, EstadoFactura.EMITIDA, detalles);
    }

    /** Reconstruye una factura existente (sin revalidar el estado actual del cliente). */
    public static Factura reconstruir(Long id, String numero, LocalDateTime fecha, Cliente cliente,
                                      String usuario, EstadoFactura estado, List<DetalleFactura> detalles) {
        return new Factura(id, numero, fecha, cliente, usuario, estado, detalles);
    }

    public void anular() {
        if (estadoFactura == EstadoFactura.ANULADA) {
            throw new FacturaYaAnuladaException(numeroFactura);
        }
        this.estadoFactura = EstadoFactura.ANULADA;
    }

    public boolean estaAnulada() {
        return estadoFactura == EstadoFactura.ANULADA;
    }

    public Long getIdFactura() { return idFactura; }
    public String getNumeroFactura() { return numeroFactura; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public Cliente getCliente() { return cliente; }
    public String getUsuarioAuthcore() { return usuarioAuthcore; }
    public BigDecimal getTotal() { return total; }
    public EstadoFactura getEstadoFactura() { return estadoFactura; }
    public List<DetalleFactura> getDetalles() { return detalles; }
}
