package com.pasteleria.facturacion.domain.model;

import com.pasteleria.facturacion.domain.exception.FacturaYaAnuladaException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Dominio: factura. El total lo calcula el dominio y la anulación solo es posible una vez. */
public class Factura {

    private Long id;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private Cliente cliente;
    private String usuarioAuthcore;
    private BigDecimal total;
    private EstadoFactura estado;
    private List<DetalleFactura> detalles;

    public Factura(Long id, String numeroFactura, LocalDateTime fechaEmision, Cliente cliente, String usuarioAuthcore,
                   BigDecimal total, EstadoFactura estado, List<DetalleFactura> detalles) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.cliente = cliente;
        this.usuarioAuthcore = usuarioAuthcore;
        this.total = total;
        this.estado = estado;
        this.detalles = List.copyOf(detalles);
    }

    public static Factura emitir(String numeroFactura, LocalDateTime fecha, Cliente cliente, String usuarioAuthcore,
                                 List<DetalleFactura> detalles) {
        BigDecimal total = detalles.stream().map(DetalleFactura::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Factura(null, numeroFactura, fecha, cliente, usuarioAuthcore, total, EstadoFactura.EMITIDA, detalles);
    }

    public void anular() {
        if (estado == EstadoFactura.ANULADA) {
            throw new FacturaYaAnuladaException(numeroFactura);
        }
        estado = EstadoFactura.ANULADA;
    }

    public Long getId() { return id; }
    public String getNumeroFactura() { return numeroFactura; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public Cliente getCliente() { return cliente; }
    public String getUsuarioAuthcore() { return usuarioAuthcore; }
    public BigDecimal getTotal() { return total; }
    public EstadoFactura getEstado() { return estado; }
    public List<DetalleFactura> getDetalles() { return detalles; }
}
