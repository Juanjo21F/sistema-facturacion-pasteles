package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.domain.port.input.ConsultarProductosUseCase;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarProductosUseCaseImpl implements ConsultarProductosUseCase {

    private final ProductoRepositoryPort productos;

    public ConsultarProductosUseCaseImpl(ProductoRepositoryPort productos) {
        this.productos = productos;
    }

    @Override
    public List<Producto> listar(boolean soloDisponibles) {
        return productos.buscarTodos(soloDisponibles);
    }

    @Override
    public Producto obtenerPorId(Long idProducto) {
        return productos.buscarPorId(idProducto).orElseThrow(() -> new ProductoNoEncontradoException(idProducto));
    }
}
