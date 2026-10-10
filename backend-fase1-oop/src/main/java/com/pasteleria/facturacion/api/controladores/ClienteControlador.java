package com.pasteleria.facturacion.api.controladores;

import com.pasteleria.facturacion.api.dto.ConversorDto;
import com.pasteleria.facturacion.api.dto.peticiones.ActualizarClienteRequest;
import com.pasteleria.facturacion.api.dto.peticiones.CrearClienteRequest;
import com.pasteleria.facturacion.api.dto.respuestas.ClienteResponse;
import com.pasteleria.facturacion.modelo.enumeraciones.Rol;
import com.pasteleria.facturacion.seguridad.AutorizacionInterceptor;
import com.pasteleria.facturacion.seguridad.RolesPermitidos;
import com.pasteleria.facturacion.seguridad.UsuarioAutenticado;
import com.pasteleria.facturacion.servicios.ServicioClientes;

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
public class ClienteControlador {

    private final ServicioClientes clientes;

    public ClienteControlador(ServicioClientes clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return clientes.listar(soloActivos).stream().map(ConversorDto::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return ConversorDto.aRespuesta(clientes.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody CrearClienteRequest request) {
        return ConversorDto.aRespuesta(clientes.crear(ConversorDto.aComando(request)));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarClienteRequest request) {
        return ConversorDto.aRespuesta(clientes.actualizar(id, ConversorDto.aComando(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        clientes.desactivar(id, usuario);
    }
}
