package com.pasteleria.facturacion.model.service;

import com.pasteleria.facturacion.exception.CategoriaNoEncontradaException;
import com.pasteleria.facturacion.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.model.entity.Categoria;
import com.pasteleria.facturacion.model.entity.Producto;
import com.pasteleria.facturacion.model.enums.EstadoRegistro;
import com.pasteleria.facturacion.model.repository.CategoriaRepository;
import com.pasteleria.facturacion.model.repository.ProductoRepository;
import com.pasteleria.facturacion.model.security.UsuarioAutenticado;
import com.pasteleria.facturacion.model.service.command.DatosProductoCommand;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Modelo: casos de uso de productos. La transacción se define aquí. */
@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final AuditoriaService auditoria;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias, AuditoriaService auditoria) {
        this.productos = productos;
        this.categorias = categorias;
        this.auditoria = auditoria;
    }

    public Producto crear(DatosProductoCommand datos) {
        productos.findByCodigoConCategoria(datos.codigoUnico()).ifPresent(existente -> {
            throw new CodigoProductoDuplicadoException(datos.codigoUnico());
        });
        Categoria categoria = buscarCategoria(datos.idCategoria());
        Producto producto = Producto.crear(datos.codigoUnico(), datos.nombre(), categoria, datos.precio(), datos.stock());
        return productos.saveAndFlush(producto);
    }

    public Producto actualizar(Long idProducto, DatosProductoCommand datos) {
        Producto producto = obtenerPorId(idProducto);
        productos.findByCodigoConCategoria(datos.codigoUnico())
                .filter(otro -> !otro.getIdProducto().equals(idProducto))
                .ifPresent(otro -> {
                    throw new CodigoProductoDuplicadoException(datos.codigoUnico());
                });
        Categoria categoria = buscarCategoria(datos.idCategoria());
        producto.actualizar(datos.codigoUnico(), datos.nombre(), categoria, datos.precio(), datos.stock());
        return productos.saveAndFlush(producto);
    }

    public void desactivar(Long idProducto, UsuarioAutenticado usuario) {
        Producto producto = obtenerPorId(idProducto);
        producto.desactivar();
        productos.saveAndFlush(producto);
        auditoria.registrar(usuario, "DESACTIVAR_PRODUCTO", "producto=" + producto.getCodigoUnico());
    }

    @Transactional(readOnly = true)
    public List<Producto> listar(boolean soloDisponibles) {
        return soloDisponibles
                ? productos.findByEstadoConCategoria(EstadoRegistro.ACTIVO)
                : productos.findAllConCategoria();
    }

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long idProducto) {
        return productos.findByIdConCategoria(idProducto)
                .orElseThrow(() -> new ProductoNoEncontradoException(idProducto));
    }

    private Categoria buscarCategoria(Long idCategoria) {
        if (idCategoria == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        return categorias.findById(idCategoria).orElseThrow(() -> new CategoriaNoEncontradaException(idCategoria));
    }
}
