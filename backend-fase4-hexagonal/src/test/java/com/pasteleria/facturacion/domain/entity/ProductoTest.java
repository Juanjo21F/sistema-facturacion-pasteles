package com.pasteleria.facturacion.domain.entity;

import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.support.InMemoryCategoriaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoTest {

    private final Categoria categoria = InMemoryCategoriaRepository.TORTAS;

    @Test
    void creaProductoCorrectamente() { // Prueba 1
        Producto p = Producto.crear("TOR-001", "Torta de chocolate", categoria, new BigDecimal("45000"), 10);

        assertThat(p.getCodigoUnico()).isEqualTo("TOR-001");
        assertThat(p.getPrecio()).isEqualByComparingTo("45000");
        assertThat(p.getStock()).isEqualTo(10);
        assertThat(p.estaActivo()).isTrue();
    }

    @Test
    void rechazaPrecioCeroONegativo() { // Prueba 2
        assertThatThrownBy(() -> Producto.crear("X", "Torta", categoria, BigDecimal.ZERO, 1))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("precio");
        assertThatThrownBy(() -> Producto.crear("X", "Torta", categoria, new BigDecimal("-5"), 1))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void rechazaStockNegativo() { // Prueba 3
        assertThatThrownBy(() -> Producto.crear("X", "Torta", categoria, BigDecimal.TEN, -1))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("stock");
    }
}
