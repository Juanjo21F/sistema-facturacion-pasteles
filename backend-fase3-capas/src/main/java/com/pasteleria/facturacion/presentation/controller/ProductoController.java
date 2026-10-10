package com.pasteleria.facturacion.presentation.controller;

import com.pasteleria.facturacion.business.dto.ProductoResponse;
import com.pasteleria.facturacion.business.security.Rol;
import com.pasteleria.facturacion.business.security.UsuarioAutenticado;
import com.pasteleria.facturacion.business.service.ProductoService;
import com.pasteleria.facturacion.presentation.mapper.RequestMapper;
import com.pasteleria.facturacion.presentation.request.ActualizarProductoRequest;
import com.pasteleria.facturacion.presentation.request.CrearProductoRequest;
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

/** Capa de presentación: solo traduce HTTP ⇄ capa de negocio. No conoce entidades ni repositorios. */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productos;

    public ProductoController(ProductoService productos) {
        this.productos = productos;
    }

    @GetMapping
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public List<ProductoResponse> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return productos.listar(soloDisponibles);
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public ProductoResponse obtener(@PathVariable Long id) {
        return productos.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest request) {
        return productos.crear(RequestMapper.aComando(request));
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest request) {
        return productos.actualizar(id, RequestMapper.aComando(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        productos.desactivar(id, usuario);
    }
}
