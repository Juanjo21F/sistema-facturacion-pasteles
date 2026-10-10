package com.pasteleria.facturacion.seguridad;

import com.pasteleria.facturacion.modelo.enumeraciones.Rol;
import com.pasteleria.facturacion.modelo.excepciones.AutenticacionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Polimorfismo: el código usa la interfaz Autenticador y no sabe si detrás hay un mock o authcore real. */
class AutenticadorTest {

    private final Autenticador autenticador = new AutenticadorMock();

    @Test
    void identificaAdministradorYEmpleado() {
        assertThat(autenticador.validarToken("admin-token").rol()).isEqualTo(Rol.ADMINISTRADOR);
        assertThat(autenticador.validarToken("empleado-token").tieneAlgunRol(Rol.EMPLEADO)).isTrue();
    }

    @Test
    void rechazaTokensVaciosOInvalidos() {
        assertThatThrownBy(() -> autenticador.validarToken(" ")).isInstanceOf(AutenticacionException.class);
        assertThatThrownBy(() -> autenticador.validarToken("otro")).isInstanceOf(AutenticacionException.class);
        assertThat(autenticador).isInstanceOf(AutenticadorBase.class);
    }
}
