package com.pasteleria.facturacion.api.controladores;

import com.pasteleria.facturacion.api.dto.ConversorDto;
import com.pasteleria.facturacion.api.dto.peticiones.CrearFacturaRequest;
import com.pasteleria.facturacion.api.dto.respuestas.FacturaResponse;
import com.pasteleria.facturacion.modelo.enumeraciones.Rol;
import com.pasteleria.facturacion.seguridad.AutorizacionInterceptor;
import com.pasteleria.facturacion.seguridad.RolesPermitidos;
import com.pasteleria.facturacion.seguridad.UsuarioAutenticado;
import com.pasteleria.facturacion.servicios.ServicioFacturacion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facturas")
public class FacturaControlador {

    private final ServicioFacturacion facturas;

    public FacturaControlador(ServicioFacturacion facturas) {
        this.facturas = facturas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse registrar(@Valid @RequestBody CrearFacturaRequest request,
                                     @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return ConversorDto.aRespuesta(facturas.registrarVenta(ConversorDto.aComando(request), usuario));
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse obtener(@PathVariable Long id) {
        return ConversorDto.aRespuesta(facturas.obtenerPorId(id));
    }

    @PutMapping("/{id}/anular")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public FacturaResponse anular(@PathVariable Long id,
                                  @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return ConversorDto.aRespuesta(facturas.anular(id, usuario));
    }
}
