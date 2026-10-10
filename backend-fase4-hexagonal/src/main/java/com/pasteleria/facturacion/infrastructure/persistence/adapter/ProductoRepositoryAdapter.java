package com.pasteleria.facturacion.infrastructure.persistence.adapter;

import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;
import com.pasteleria.facturacion.infrastructure.persistence.entity.ProductoJpaEntity;
import com.pasteleria.facturacion.infrastructure.persistence.repository.CategoriaJpaRepository;
import com.pasteleria.facturacion.infrastructure.persistence.repository.ProductoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository jpa;
    private final CategoriaJpaRepository categoriaJpa;

    public ProductoRepositoryAdapter(ProductoJpaRepository jpa, CategoriaJpaRepository categoriaJpa) {
        this.jpa = jpa;
        this.categoriaJpa = categoriaJpa;
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoJpaEntity entidad = producto.getIdProducto() == null
                ? new ProductoJpaEntity()
                : jpa.findById(producto.getIdProducto())
                        .orElseThrow(() -> new ProductoNoEncontradoException(producto.getIdProducto()));
        entidad.setCodigoUnico(producto.getCodigoUnico());
        entidad.setNombre(producto.getNombre());
        entidad.setCategoria(categoriaJpa.getReferenceById(producto.getCategoria().getIdCategoria()));
        entidad.setPrecio(producto.getPrecio());
        entidad.setStock(producto.getStock());
        entidad.setEstado(producto.getEstado());
        ProductoJpaEntity guardado = jpa.saveAndFlush(entidad);
        return Producto.reconstruir(guardado.getId(), producto.getCodigoUnico(), producto.getNombre(),
                producto.getCategoria(), producto.getPrecio(), producto.getStock(), producto.getEstado());
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return jpa.findByIdConCategoria(id).map(PersistenceMapper::aDominio);
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigoUnico) {
        return jpa.findByCodigoConCategoria(codigoUnico).map(PersistenceMapper::aDominio);
    }

    @Override
    public List<Producto> buscarTodos(boolean soloActivos) {
        List<ProductoJpaEntity> entidades = soloActivos
                ? jpa.findByEstadoConCategoria(EstadoRegistro.ACTIVO)
                : jpa.findAllConCategoria();
        return entidades.stream().map(PersistenceMapper::aDominio).toList();
    }

    @Override
    public List<Producto> buscarPorIdsConBloqueo(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jpa.findByIdInParaActualizar(ids).stream().map(PersistenceMapper::aDominio).toList();
    }
}
