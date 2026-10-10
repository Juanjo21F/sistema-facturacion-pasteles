package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Categoria;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.CategoriaNoEncontradaException;
import com.pasteleria.facturacion.domain.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.port.input.CrearProductoUseCase;
import com.pasteleria.facturacion.domain.port.input.command.DatosProductoCommand;
import com.pasteleria.facturacion.domain.port.output.CategoriaRepositoryPort;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CrearProductoUseCaseImpl implements CrearProductoUseCase {

    private final ProductoRepositoryPort productos;
    private final CategoriaRepositoryPort categorias;

    public CrearProductoUseCaseImpl(ProductoRepositoryPort productos, CategoriaRepositoryPort categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @Override
    public Producto ejecutar(DatosProductoCommand comando) {
        productos.buscarPorCodigo(comando.codigoUnico()).ifPresent(existente -> {
            throw new CodigoProductoDuplicadoException(comando.codigoUnico());
        });
        Categoria categoria = buscarCategoria(comando.idCategoria());
        Producto producto = Producto.crear(comando.codigoUnico(), comando.nombre(), categoria,
                comando.precio(), comando.stock());
        return productos.guardar(producto);
    }

    private Categoria buscarCategoria(Long idCategoria) {
        if (idCategoria == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        return categorias.buscarPorId(idCategoria).orElseThrow(() -> new CategoriaNoEncontradaException(idCategoria));
    }
}
