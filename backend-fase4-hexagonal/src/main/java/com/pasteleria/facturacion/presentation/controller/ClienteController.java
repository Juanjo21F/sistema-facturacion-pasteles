package com.pasteleria.facturacion.presentation.controller;

import com.pasteleria.facturacion.domain.port.input.ActualizarClienteUseCase;
import com.pasteleria.facturacion.domain.port.input.ConsultarClientesUseCase;
import com.pasteleria.facturacion.domain.port.input.CrearClienteUseCase;
import com.pasteleria.facturacion.domain.port.input.DesactivarClienteUseCase;
import com.pasteleria.facturacion.domain.valueobject.Rol;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import com.pasteleria.facturacion.presentation.dto.ActualizarClienteRequest;
import com.pasteleria.facturacion.presentation.dto.ApiMapper;
import com.pasteleria.facturacion.presentation.dto.ClienteResponse;
import com.pasteleria.facturacion.presentation.dto.CrearClienteRequest;
import com.pasteleria.facturacion.presentation.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.presentation.security.RolesPermitidos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
public class ClienteController {

    private final CrearClienteUseCase crear;
    private final ConsultarClientesUseCase consultar;
    private final ActualizarClienteUseCase actualizar;
    private final DesactivarClienteUseCase desactivar;

    public ClienteController(CrearClienteUseCase crear, ConsultarClientesUseCase consultar,
                             ActualizarClienteUseCase actualizar, DesactivarClienteUseCase desactivar) {
        this.crear = crear;
        this.consultar = consultar;
        this.actualizar = actualizar;
        this.desactivar = desactivar;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return consultar.listar(soloActivos).stream().map(ApiMapper::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return ApiMapper.aRespuesta(consultar.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody CrearClienteRequest request) {
        return ApiMapper.aRespuesta(crear.ejecutar(ApiMapper.aComando(request)));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarClienteRequest request) {
        return ApiMapper.aRespuesta(actualizar.ejecutar(id, ApiMapper.aComando(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        desactivar.ejecutar(id, usuario);
    }
}
