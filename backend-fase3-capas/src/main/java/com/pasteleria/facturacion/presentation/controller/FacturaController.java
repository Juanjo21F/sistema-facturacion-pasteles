package com.pasteleria.facturacion.presentation.controller;

import com.pasteleria.facturacion.business.dto.FacturaResponse;
import com.pasteleria.facturacion.business.security.Rol;
import com.pasteleria.facturacion.business.security.UsuarioAutenticado;
import com.pasteleria.facturacion.business.service.FacturacionService;
import com.pasteleria.facturacion.presentation.mapper.RequestMapper;
import com.pasteleria.facturacion.presentation.request.CrearFacturaRequest;
import com.pasteleria.facturacion.presentation.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.presentation.security.RolesPermitidos;

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

    private final FacturacionService facturacion;

    public FacturaController(FacturacionService facturacion) {
        this.facturacion = facturacion;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse registrar(@Valid @RequestBody CrearFacturaRequest request,
                                     @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return facturacion.registrarVenta(RequestMapper.aComando(request), usuario);
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse obtener(@PathVariable Long id) {
        return facturacion.obtenerPorId(id);
    }

    @PutMapping("/{id}/anular")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public FacturaResponse anular(@PathVariable Long id,
                                  @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return facturacion.anular(id, usuario);
    }
}
