package com.pasteleria.facturacion.infrastructure.adapter.in.web.controller;

import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;
import com.pasteleria.facturacion.application.port.in.GestionarProductosUseCase;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.mapper.WebMapper;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.ProductoResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.ActualizarProductoRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.CrearProductoRequest;
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

/** Adaptador de entrada (HTTP): solo traduce HTTP ⇄ casos de uso. No conoce entidades JPA ni repositorios. */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final GestionarProductosUseCase productos;

    public ProductoController(GestionarProductosUseCase productos) {
        this.productos = productos;
    }

    @GetMapping
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public List<ProductoResponse> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return productos.listar(soloDisponibles).stream().map(WebMapper::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public ProductoResponse obtener(@PathVariable Long id) {
        return WebMapper.aRespuesta(productos.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest request) {
        return WebMapper.aRespuesta(productos.crear(WebMapper.aComando(request)));
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest request) {
        return WebMapper.aRespuesta(productos.actualizar(id, WebMapper.aComando(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        productos.desactivar(id, usuario);
    }
}
