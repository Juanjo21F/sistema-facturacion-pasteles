package com.pasteleria.facturacion.infrastructure.adapter.out.persistence;

import com.pasteleria.facturacion.application.port.out.ProductoRepositoryPort;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.CategoriaRepository;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.ProductoRepository;
import com.pasteleria.facturacion.domain.model.EstadoRegistro;
import com.pasteleria.facturacion.domain.model.Producto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adaptador de salida: implementa el puerto de productos con Spring Data JPA. */
@Component
public class ProductoPersistenceAdapter implements ProductoRepositoryPort {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoPersistenceAdapter(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoEntity entidad = PersistenceMapper.aEntidad(producto,
                categorias.getReferenceById(producto.getCategoria().id()));
        ProductoEntity guardada = productos.saveAndFlush(entidad);
        return productos.findByIdConCategoria(guardada.getIdProducto()).map(PersistenceMapper::aDominio).orElseThrow();
    }

    @Override
    public void guardarTodos(Collection<Producto> lista) {
        productos.saveAll(lista.stream()
                .map(p -> PersistenceMapper.aEntidad(p, categorias.getReferenceById(p.getCategoria().id())))
                .toList());
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return productos.findByIdConCategoria(id).map(PersistenceMapper::aDominio);
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigoUnico) {
        return productos.findByCodigoConCategoria(codigoUnico).map(PersistenceMapper::aDominio);
    }

    @Override
    public List<Producto> listar(boolean soloActivos) {
        List<ProductoEntity> lista = soloActivos
                ? productos.findByEstadoConCategoria(EstadoRegistro.ACTIVO)
                : productos.findAllConCategoria();
        return lista.stream().map(PersistenceMapper::aDominio).toList();
    }

    @Override
    public List<Producto> buscarPorIdsParaActualizar(Collection<Long> ids) {
        return productos.findByIdInParaActualizar(ids).stream().map(PersistenceMapper::aDominio).toList();
    }
}
