package com.pasteleria.facturacion.model.entity;

import com.pasteleria.facturacion.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.exception.StockInsuficienteException;
import com.pasteleria.facturacion.model.enums.EstadoFactura;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Modelo: factura (raíz del agregado). Contiene las reglas de una venta: valida líneas, descuenta
 * stock, calcula el total y se anula restaurando el inventario.
 */
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

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
    private EstadoFactura estadoFactura;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleFactura> detalles = new ArrayList<>();

    protected Factura() {
    }

    private Factura(String numero, LocalDateTime fecha, Cliente cliente, String usuario, List<DetalleFactura> detalles) {
        this.numeroFactura = Validaciones.textoObligatorio(numero, "numeroFactura");
        this.fechaEmision = Objects.requireNonNull(fecha);
        this.cliente = Objects.requireNonNull(cliente);
        this.usuarioAuthcore = Validaciones.textoObligatorio(usuario, "usuarioAuthcore");
        this.estadoFactura = EstadoFactura.EMITIDA;
        detalles.forEach(d -> {
            d.asignarFactura(this);
            this.detalles.add(d);
        });
        this.total = this.detalles.stream().map(DetalleFactura::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Emite una factura: valida, calcula el total y descuenta el stock (todo o nada). */
    public static Factura emitir(Cliente cliente, List<LineaVenta> lineas, String usuario, long secuencia, LocalDateTime fecha) {
        validarLineas(lineas);
        cliente.validarActivo();
        lineas.forEach(l -> {
            if (!l.producto().tieneStockPara(l.cantidad())) {
                throw new StockInsuficienteException(l.producto().getNombre(), l.cantidad(), l.producto().getStock());
            }
        });
        List<DetalleFactura> detalles = lineas.stream().map(DetalleFactura::desde).toList();
        Factura factura = new Factura(generarNumero(secuencia, fecha), fecha, cliente, usuario, detalles);
        lineas.forEach(l -> l.producto().descontarStock(l.cantidad()));
        return factura;
    }

    /** Cambia a ANULADA y restaura el stock. Si ya estaba anulada falla ANTES de tocar el inventario. */
    public void anular(Map<Long, Producto> productosPorId) {
        if (estaAnulada()) {
            throw new FacturaYaAnuladaException(numeroFactura);
        }
        for (DetalleFactura d : detalles) {
            Producto producto = productosPorId.get(d.getProducto().getIdProducto());
            if (producto == null) {
                throw new ReglaDeNegocioException("No se pudo restaurar el stock: producto no disponible");
            }
            producto.restaurarStock(d.getCantidad());
        }
        this.estadoFactura = EstadoFactura.ANULADA;
    }

    public boolean estaAnulada() {
        return estadoFactura == EstadoFactura.ANULADA;
    }

    public static String generarNumero(long secuencia, LocalDateTime fecha) {
        return String.format("FAC-%d-%06d", fecha.getYear(), secuencia);
    }

    private static void validarLineas(List<LineaVenta> lineas) {
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

    public Long getIdFactura() { return idFactura; }
    public String getNumeroFactura() { return numeroFactura; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public Cliente getCliente() { return cliente; }
    public String getUsuarioAuthcore() { return usuarioAuthcore; }
    public BigDecimal getTotal() { return total; }
    public EstadoFactura getEstadoFactura() { return estadoFactura; }
    public List<DetalleFactura> getDetalles() { return Collections.unmodifiableList(detalles); }
}
