package com.pasteleria.facturacion.application.service;

import com.pasteleria.facturacion.application.port.in.DatosProductoCommand;
import com.pasteleria.facturacion.application.port.in.GestionarProductosUseCase;
import com.pasteleria.facturacion.application.port.out.AuditoriaPort;
import com.pasteleria.facturacion.application.port.out.CategoriaRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.exception.CategoriaNoEncontradaException;
import com.pasteleria.facturacion.domain.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.domain.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.model.Categoria;
import com.pasteleria.facturacion.domain.model.Producto;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;
import com.pasteleria.facturacion.domain.validation.Validaciones;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/** Aplicación: orquesta el dominio de productos a través de puertos. */
@Transactional
public class ProductoService implements GestionarProductosUseCase {

    private final ProductoRepositoryPort productos;
    private final CategoriaRepositoryPort categorias;
    private final AuditoriaPort auditoria;

    public ProductoService(ProductoRepositoryPort productos, CategoriaRepositoryPort categorias, AuditoriaPort auditoria) {
        this.productos = productos;
        this.categorias = categorias;
        this.auditoria = auditoria;
    }

    @Override
    public Producto crear(DatosProductoCommand datos) {
        Validaciones.textoObligatorio(datos.codigoUnico(), "codigoUnico");
        productos.buscarPorCodigo(datos.codigoUnico().trim()).ifPresent(existente -> {
            throw new CodigoProductoDuplicadoException(datos.codigoUnico());
        });
        Producto producto = Producto.crear(datos.codigoUnico(), datos.nombre(), buscarCategoria(datos.idCategoria()),
                datos.precio(), datos.stock());
        return productos.guardar(producto);
    }

    @Override
    public Producto actualizar(Long idProducto, DatosProductoCommand datos) {
        Producto producto = buscar(idProducto);
        Validaciones.textoObligatorio(datos.codigoUnico(), "codigoUnico");
        productos.buscarPorCodigo(datos.codigoUnico().trim())
                .filter(otro -> !otro.getId().equals(idProducto))
                .ifPresent(otro -> {
                    throw new CodigoProductoDuplicadoException(datos.codigoUnico());
                });
        producto.actualizarDatos(datos.codigoUnico(), datos.nombre(), buscarCategoria(datos.idCategoria()),
                datos.precio(), datos.stock());
        return productos.guardar(producto);
    }

    @Override
    public void desactivar(Long idProducto, UsuarioAutenticado usuario) {
        Producto producto = buscar(idProducto);
        producto.desactivar();
        productos.guardar(producto);
        auditoria.registrar(usuario, "DESACTIVAR_PRODUCTO", "producto=" + producto.getCodigoUnico());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listar(boolean soloDisponibles) {
        return productos.listar(soloDisponibles);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long idProducto) {
        return buscar(idProducto);
    }

    private Categoria buscarCategoria(Long idCategoria) {
        if (idCategoria == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        return categorias.buscarPorId(idCategoria).orElseThrow(() -> new CategoriaNoEncontradaException(idCategoria));
    }

    private Producto buscar(Long idProducto) {
        return productos.buscarPorId(idProducto).orElseThrow(() -> new ProductoNoEncontradoException(idProducto));
    }
}
