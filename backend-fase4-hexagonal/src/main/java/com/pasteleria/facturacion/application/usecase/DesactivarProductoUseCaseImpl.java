package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.application.service.AuditoriaService;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.domain.port.input.DesactivarProductoUseCase;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DesactivarProductoUseCaseImpl implements DesactivarProductoUseCase {

    private final ProductoRepositoryPort productos;
    private final AuditoriaService auditoria;

    public DesactivarProductoUseCaseImpl(ProductoRepositoryPort productos, AuditoriaService auditoria) {
        this.productos = productos;
        this.auditoria = auditoria;
    }

    @Override
    public void ejecutar(Long idProducto, UsuarioAutenticado usuario) {
        Producto producto = productos.buscarPorId(idProducto)
                .orElseThrow(() -> new ProductoNoEncontradoException(idProducto));
        producto.desactivar();
        productos.guardar(producto);
        auditoria.registrar(usuario, "DESACTIVAR_PRODUCTO", "producto=" + producto.getCodigoUnico());
    }
}
