package com.pasteleria.facturacion.servicios;

import com.pasteleria.facturacion.modelo.entidades.Categoria;
import com.pasteleria.facturacion.modelo.entidades.Producto;
import com.pasteleria.facturacion.modelo.enumeraciones.EstadoRegistro;
import com.pasteleria.facturacion.modelo.excepciones.CategoriaNoEncontradaException;
import com.pasteleria.facturacion.modelo.excepciones.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.modelo.excepciones.ProductoNoEncontradoException;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;
import com.pasteleria.facturacion.repositorios.CategoriaRepositorio;
import com.pasteleria.facturacion.repositorios.ProductoRepositorio;
import com.pasteleria.facturacion.servicios.comandos.DatosProductoCommand;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioProductos extends ServicioActivable<Producto> {

    private final ProductoRepositorio productos;
    private final CategoriaRepositorio categorias;

    public ServicioProductos(ProductoRepositorio productos, CategoriaRepositorio categorias, ServicioAuditoria auditoria) {
        super(auditoria);
        this.productos = productos;
        this.categorias = categorias;
    }

    public Producto crear(DatosProductoCommand datos) {
        productos.findByCodigoConCategoria(datos.codigoUnico()).ifPresent(existente -> {
            throw new CodigoProductoDuplicadoException(datos.codigoUnico());
        });
        Categoria categoria = buscarCategoria(datos.idCategoria());
        return guardar(Producto.crear(datos.codigoUnico(), datos.nombre(), categoria, datos.precio(), datos.stock()));
    }

    public Producto actualizar(Long idProducto, DatosProductoCommand datos) {
        Producto producto = obtenerPorId(idProducto);
        productos.findByCodigoConCategoria(datos.codigoUnico())
                .filter(otro -> !otro.getId().equals(idProducto))
                .ifPresent(otro -> {
                    throw new CodigoProductoDuplicadoException(datos.codigoUnico());
                });
        producto.actualizar(datos.codigoUnico(), datos.nombre(), buscarCategoria(datos.idCategoria()),
                datos.precio(), datos.stock());
        return guardar(producto);
    }

    @Transactional(readOnly = true)
    public List<Producto> listar(boolean soloDisponibles) {
        return soloDisponibles
                ? productos.findByEstadoConCategoria(EstadoRegistro.ACTIVO)
                : productos.findAllConCategoria();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return super.obtenerPorId(id);
    }

    @Override
    protected Optional<Producto> buscar(Long id) {
        return productos.findByIdConCategoria(id);
    }

    @Override
    protected RuntimeException noEncontrado(Long id) {
        return new ProductoNoEncontradoException(id);
    }

    @Override
    protected Producto guardar(Producto producto) {
        return productos.saveAndFlush(producto);
    }

    @Override
    protected String operacionDesactivar() {
        return "DESACTIVAR_PRODUCTO";
    }

    @Override
    protected String detalleAuditoria(Producto producto) {
        return "producto=" + producto.getCodigoUnico();
    }

    private Categoria buscarCategoria(Long idCategoria) {
        if (idCategoria == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        return categorias.findById(idCategoria).orElseThrow(() -> new CategoriaNoEncontradaException(idCategoria));
    }
}
