package com.pasteleria.facturacion.modelo.entidades;

import com.pasteleria.facturacion.modelo.base.Anulable;
import com.pasteleria.facturacion.modelo.base.EntidadBase;
import com.pasteleria.facturacion.modelo.base.Validaciones;
import com.pasteleria.facturacion.modelo.enumeraciones.EstadoFactura;
import com.pasteleria.facturacion.modelo.excepciones.FacturaYaAnuladaException;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;

import jakarta.persistence.AttributeOverride;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Raíz del agregado. Es una EntidadBase y un documento Anulable; es dueña de sus detalles. */
@Entity
@Table(name = "facturas")
@AttributeOverride(name = "id", column = @Column(name = "id_factura"))
public class Factura extends EntidadBase implements Anulable {

    @Column(name = "numero_factura", nullable = false, unique = true, length = 30)
    private String numeroFactura;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "usuario_authcore", nullable = false, length = 100)
    private String usuarioAuthcore;

    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_factura", nullable = false, length = 10)
    private EstadoFactura estado;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleFactura> detalles = new ArrayList<>();

    protected Factura() {
    }

    private Factura(String numero, LocalDateTime fecha, Cliente cliente, String usuario, List<LineaVenta> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un detalle");
        }
        this.numeroFactura = Validaciones.textoObligatorio(numero, "numeroFactura");
        this.fechaEmision = Objects.requireNonNull(fecha);
        this.cliente = Objects.requireNonNull(cliente);
        this.usuarioAuthcore = Validaciones.textoObligatorio(usuario, "usuarioAuthcore");
        this.estado = EstadoFactura.EMITIDA;
        for (LineaVenta linea : lineas) {
            DetalleFactura detalle = new DetalleFactura(linea);
            detalle.asignarFactura(this);
            this.detalles.add(detalle);
        }
        this.total = detalles.stream().map(DetalleFactura::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** El total SIEMPRE lo calcula la factura a partir de sus detalles. */
    public static Factura emitir(String numero, LocalDateTime fecha, Cliente cliente, String usuario,
                                 List<LineaVenta> lineas) {
        return new Factura(numero, fecha, cliente, usuario, lineas);
    }

    public static String generarNumero(long secuencia, LocalDateTime fecha) {
        return String.format("FAC-%d-%06d", fecha.getYear(), secuencia);
    }

    @Override
    public void anular() {
        if (estaAnulada()) {
            throw new FacturaYaAnuladaException(numeroFactura);
        }
        this.estado = EstadoFactura.ANULADA;
    }

    @Override
    public boolean estaAnulada() {
        return estado == EstadoFactura.ANULADA;
    }

    public String getNumeroFactura() { return numeroFactura; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public Cliente getCliente() { return cliente; }
    public String getUsuarioAuthcore() { return usuarioAuthcore; }
    public BigDecimal getTotal() { return total; }
    public EstadoFactura getEstadoFactura() { return estado; }
    public List<DetalleFactura> getDetalles() { return Collections.unmodifiableList(detalles); }
}
