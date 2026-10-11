package com.pasteleria.facturacion.infrastructure.adapter.in.web.controller;

import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;
import com.pasteleria.facturacion.application.port.in.GestionarClientesUseCase;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.mapper.WebMapper;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.ClienteResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.ActualizarClienteRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.CrearClienteRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.security.RolesPermitidos;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
@RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
public class ClienteController {

    private final GestionarClientesUseCase clientes;

    public ClienteController(GestionarClientesUseCase clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return clientes.listar(soloActivos).stream().map(WebMapper::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return WebMapper.aRespuesta(clientes.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody CrearClienteRequest request) {
        return WebMapper.aRespuesta(clientes.crear(WebMapper.aComando(request)));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarClienteRequest request) {
        return WebMapper.aRespuesta(clientes.actualizar(id, WebMapper.aComando(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        clientes.desactivar(id, usuario);
    }
}
