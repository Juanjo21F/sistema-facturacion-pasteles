package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.dto.DatosProductoCommand;
import com.pasteleria.facturacion.business.dto.ProductoResponse;
import com.pasteleria.facturacion.business.exception.CategoriaNoEncontradaException;
import com.pasteleria.facturacion.business.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.business.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.business.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.business.mapper.EntityMapper;
import com.pasteleria.facturacion.business.security.UsuarioAutenticado;
import com.pasteleria.facturacion.business.validation.Validaciones;
import com.pasteleria.facturacion.dataaccess.entity.CategoriaEntity;
import com.pasteleria.facturacion.dataaccess.entity.EstadoRegistro;
import com.pasteleria.facturacion.dataaccess.entity.ProductoEntity;
import com.pasteleria.facturacion.dataaccess.repository.CategoriaRepository;
import com.pasteleria.facturacion.dataaccess.repository.ProductoRepository;

import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capa de negocio: reglas de productos. Trabaja con entidades de datos y entrega DTOs. */
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

    public ProductoResponse crear(DatosProductoCommand datos) {
        validar(datos);
        productos.findByCodigoConCategoria(datos.codigoUnico().trim()).ifPresent(existente -> {
            throw new CodigoProductoDuplicadoException(datos.codigoUnico());
        });
        ProductoEntity producto = new ProductoEntity();
        producto.setEstado(EstadoRegistro.ACTIVO);
        aplicar(producto, datos, buscarCategoria(datos.idCategoria()));
        return EntityMapper.aRespuesta(productos.saveAndFlush(producto));
    }

    public ProductoResponse actualizar(Long idProducto, DatosProductoCommand datos) {
        ProductoEntity producto = buscar(idProducto);
        validar(datos);
        productos.findByCodigoConCategoria(datos.codigoUnico().trim())
                .filter(otro -> !otro.getIdProducto().equals(idProducto))
                .ifPresent(otro -> {
                    throw new CodigoProductoDuplicadoException(datos.codigoUnico());
                });
        aplicar(producto, datos, buscarCategoria(datos.idCategoria()));
        return EntityMapper.aRespuesta(productos.saveAndFlush(producto));
    }

    public void desactivar(Long idProducto, UsuarioAutenticado usuario) {
        ProductoEntity producto = buscar(idProducto);
        producto.setEstado(EstadoRegistro.INACTIVO);
        productos.saveAndFlush(producto);
        auditoria.registrar(usuario, "DESACTIVAR_PRODUCTO", "producto=" + producto.getCodigoUnico());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean soloDisponibles) {
        List<ProductoEntity> lista = soloDisponibles
                ? productos.findByEstadoConCategoria(EstadoRegistro.ACTIVO)
                : productos.findAllConCategoria();
        return lista.stream().map(EntityMapper::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long idProducto) {
        return EntityMapper.aRespuesta(buscar(idProducto));
    }

    // ---- reglas ----

    private void validar(DatosProductoCommand datos) {
        Validaciones.textoObligatorio(datos.codigoUnico(), "codigoUnico");
        Validaciones.textoObligatorio(datos.nombre(), "nombre");
        if (datos.idCategoria() == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        Validaciones.mayorQueCero(datos.precio(), "precio");
        if (datos.stock() < 0) {
            throw new ReglaDeNegocioException("El stock no puede ser negativo");
        }
    }

    private void aplicar(ProductoEntity producto, DatosProductoCommand datos, CategoriaEntity categoria) {
        producto.setCodigoUnico(datos.codigoUnico().trim());
        producto.setNombre(datos.nombre().trim());
        producto.setCategoria(categoria);
        producto.setPrecio(datos.precio().setScale(2, RoundingMode.HALF_UP));
        producto.setStock(datos.stock());
    }

    private CategoriaEntity buscarCategoria(Long idCategoria) {
        return categorias.findById(idCategoria).orElseThrow(() -> new CategoriaNoEncontradaException(idCategoria));
    }

    private ProductoEntity buscar(Long idProducto) {
        return productos.findByIdConCategoria(idProducto).orElseThrow(() -> new ProductoNoEncontradoException(idProducto));
    }
}
