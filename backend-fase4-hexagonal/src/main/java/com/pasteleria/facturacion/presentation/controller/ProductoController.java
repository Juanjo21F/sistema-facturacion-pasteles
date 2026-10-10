package com.pasteleria.facturacion.presentation.controller;

import com.pasteleria.facturacion.domain.port.input.ActualizarProductoUseCase;
import com.pasteleria.facturacion.domain.port.input.ConsultarProductosUseCase;
import com.pasteleria.facturacion.domain.port.input.CrearProductoUseCase;
import com.pasteleria.facturacion.domain.port.input.DesactivarProductoUseCase;
import com.pasteleria.facturacion.domain.valueobject.Rol;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import com.pasteleria.facturacion.presentation.dto.ActualizarProductoRequest;
import com.pasteleria.facturacion.presentation.dto.ApiMapper;
import com.pasteleria.facturacion.presentation.dto.CrearProductoRequest;
import com.pasteleria.facturacion.presentation.dto.ProductoResponse;
import com.pasteleria.facturacion.presentation.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.presentation.security.RolesPermitidos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final CrearProductoUseCase crear;
    private final ConsultarProductosUseCase consultar;
    private final ActualizarProductoUseCase actualizar;
    private final DesactivarProductoUseCase desactivar;

    public ProductoController(CrearProductoUseCase crear, ConsultarProductosUseCase consultar,
                              ActualizarProductoUseCase actualizar, DesactivarProductoUseCase desactivar) {
        this.crear = crear;
        this.consultar = consultar;
        this.actualizar = actualizar;
        this.desactivar = desactivar;
    }

    @GetMapping
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public List<ProductoResponse> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return consultar.listar(soloDisponibles).stream().map(ApiMapper::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public ProductoResponse obtener(@PathVariable Long id) {
        return ApiMapper.aRespuesta(consultar.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest request) {
        return ApiMapper.aRespuesta(crear.ejecutar(ApiMapper.aComando(request)));
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest request) {
        return ApiMapper.aRespuesta(actualizar.ejecutar(id, ApiMapper.aComando(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        desactivar.ejecutar(id, usuario);
    }
}
