package com.pasteleria.facturacion.controller;

import com.pasteleria.facturacion.controller.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.controller.security.RolesPermitidos;
import com.pasteleria.facturacion.model.enums.Rol;
import com.pasteleria.facturacion.model.security.UsuarioAutenticado;
import com.pasteleria.facturacion.model.service.FacturaService;
import com.pasteleria.facturacion.view.ViewMapper;
import com.pasteleria.facturacion.view.request.CrearFacturaRequest;
import com.pasteleria.facturacion.view.response.FacturaResponse;

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

    private final FacturaService facturas;

    public FacturaController(FacturaService facturas) {
        this.facturas = facturas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse registrar(@Valid @RequestBody CrearFacturaRequest request,
                                     @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return ViewMapper.aRespuesta(facturas.registrarVenta(ViewMapper.aComando(request), usuario));
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse obtener(@PathVariable Long id) {
        return ViewMapper.aRespuesta(facturas.obtenerPorId(id));
    }

    @PutMapping("/{id}/anular")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public FacturaResponse anular(@PathVariable Long id,
                                  @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return ViewMapper.aRespuesta(facturas.anular(id, usuario));
    }
}
