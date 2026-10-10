package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Categoria;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.CategoriaNoEncontradaException;
import com.pasteleria.facturacion.domain.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.domain.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.port.input.ActualizarProductoUseCase;
import com.pasteleria.facturacion.domain.port.input.command.DatosProductoCommand;
import com.pasteleria.facturacion.domain.port.output.CategoriaRepositoryPort;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActualizarProductoUseCaseImpl implements ActualizarProductoUseCase {

    private final ProductoRepositoryPort productos;
    private final CategoriaRepositoryPort categorias;

    public ActualizarProductoUseCaseImpl(ProductoRepositoryPort productos, CategoriaRepositoryPort categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @Override
    public Producto ejecutar(Long idProducto, DatosProductoCommand comando) {
        Producto producto = productos.buscarPorId(idProducto)
                .orElseThrow(() -> new ProductoNoEncontradoException(idProducto));
        productos.buscarPorCodigo(comando.codigoUnico())
                .filter(otro -> !otro.getIdProducto().equals(idProducto))
                .ifPresent(otro -> {
                    throw new CodigoProductoDuplicadoException(comando.codigoUnico());
                });
        if (comando.idCategoria() == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        Categoria categoria = categorias.buscarPorId(comando.idCategoria())
                .orElseThrow(() -> new CategoriaNoEncontradaException(comando.idCategoria()));
        producto.actualizar(comando.codigoUnico(), comando.nombre(), categoria, comando.precio(), comando.stock());
        return productos.guardar(producto);
    }
}
