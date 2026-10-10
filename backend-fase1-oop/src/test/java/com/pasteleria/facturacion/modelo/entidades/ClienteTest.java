package com.pasteleria.facturacion.modelo.entidades;

import com.pasteleria.facturacion.modelo.base.Activable;
import com.pasteleria.facturacion.modelo.base.EntidadActivable;
import com.pasteleria.facturacion.modelo.base.EntidadBase;
import com.pasteleria.facturacion.modelo.base.Persona;
import com.pasteleria.facturacion.modelo.base.Vendible;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Herencia y polimorfismo: Cliente es Persona, EntidadActivable, Activable y EntidadBase a la vez. */
class ClienteTest {

    @Test
    void creaClienteActivoYEsPolimorficoConSusAncestros() { // Prueba 5
        Cliente c = Cliente.crear("123", "Ana Pérez", "300", "ana@mail.com");

        assertThat(c.estaActivo()).isTrue();
        assertThat(c).isInstanceOf(Persona.class).isInstanceOf(EntidadActivable.class)
                .isInstanceOf(Activable.class).isInstanceOf(EntidadBase.class);
        Persona persona = c;
        assertThat(persona.descripcion()).contains("Ana Pérez").contains("123");
    }

    @Test
    void desactivaDesdeLaInterfaz() {
        Activable activable = Cliente.crear("123", "Ana Pérez", "300", "ana@mail.com");
        activable.desactivar();

        assertThat(activable.estaActivo()).isFalse();
    }

    @Test
    void exigeTodosLosDatos() {
        assertThatThrownBy(() -> Cliente.crear(" ", "Ana", "300", "a@b.c")).isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> Cliente.crear("1", "Ana", "300", null)).isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void productoYFacturaImplementanSusContratos() {
        Producto p = Producto.crear("T", "Torta", new Categoria("Tortas", null), BigDecimal.TEN, 2);
        Vendible vendible = p;
        vendible.descontarStock(2);
        assertThat(vendible.getStock()).isZero();
        assertThat(vendible.tieneStockPara(1)).isFalse();
    }
}
