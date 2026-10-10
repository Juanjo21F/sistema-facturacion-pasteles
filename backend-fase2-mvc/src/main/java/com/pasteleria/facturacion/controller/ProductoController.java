package com.pasteleria.facturacion.controller;

import com.pasteleria.facturacion.controller.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.controller.security.RolesPermitidos;
import com.pasteleria.facturacion.model.enums.Rol;
import com.pasteleria.facturacion.model.security.UsuarioAutenticado;
import com.pasteleria.facturacion.model.service.ProductoService;
import com.pasteleria.facturacion.view.ViewMapper;
import com.pasteleria.facturacion.view.request.ActualizarProductoRequest;
import com.pasteleria.facturacion.view.request.CrearProductoRequest;
import com.pasteleria.facturacion.view.response.ProductoResponse;

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

/** Controlador: recibe la petición, delega en el modelo y elige la vista (DTO JSON). */
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
        return productos.listar(soloDisponibles).stream().map(ViewMapper::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public ProductoResponse obtener(@PathVariable Long id) {
        return ViewMapper.aRespuesta(productos.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest request) {
        return ViewMapper.aRespuesta(productos.crear(ViewMapper.aComando(request)));
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest request) {
        return ViewMapper.aRespuesta(productos.actualizar(id, ViewMapper.aComando(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        productos.desactivar(id, usuario);
    }
}
