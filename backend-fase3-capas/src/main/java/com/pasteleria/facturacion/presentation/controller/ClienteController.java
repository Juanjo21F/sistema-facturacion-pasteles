package com.pasteleria.facturacion.presentation.controller;

import com.pasteleria.facturacion.business.dto.ClienteResponse;
import com.pasteleria.facturacion.business.security.Rol;
import com.pasteleria.facturacion.business.security.UsuarioAutenticado;
import com.pasteleria.facturacion.business.service.ClienteService;
import com.pasteleria.facturacion.presentation.mapper.RequestMapper;
import com.pasteleria.facturacion.presentation.request.ActualizarClienteRequest;
import com.pasteleria.facturacion.presentation.request.CrearClienteRequest;
import com.pasteleria.facturacion.presentation.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.presentation.security.RolesPermitidos;

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

    private final ClienteService clientes;

    public ClienteController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return clientes.listar(soloActivos);
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clientes.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody CrearClienteRequest request) {
        return clientes.crear(RequestMapper.aComando(request));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarClienteRequest request) {
        return clientes.actualizar(id, RequestMapper.aComando(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        clientes.desactivar(id, usuario);
    }
}
