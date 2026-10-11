package com.pasteleria.facturacion.infrastructure.adapter.in.web.controller;

import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;
import com.pasteleria.facturacion.application.port.in.FacturacionUseCase;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.mapper.WebMapper;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.FacturaResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.CrearFacturaRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.security.RolesPermitidos;

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
public class FacturaController {

    private final FacturacionUseCase facturacion;

    public FacturaController(FacturacionUseCase facturacion) {
        this.facturacion = facturacion;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse registrar(@Valid @RequestBody CrearFacturaRequest request,
                                     @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return WebMapper.aRespuesta(facturacion.registrarVenta(WebMapper.aComando(request), usuario));
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse obtener(@PathVariable Long id) {
        return WebMapper.aRespuesta(facturacion.obtenerPorId(id));
    }

    @PutMapping("/{id}/anular")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public FacturaResponse anular(@PathVariable Long id,
                                  @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return WebMapper.aRespuesta(facturacion.anular(id, usuario));
    }
}
