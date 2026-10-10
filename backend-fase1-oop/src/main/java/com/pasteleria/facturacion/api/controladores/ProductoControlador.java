package com.pasteleria.facturacion.api.controladores;

import com.pasteleria.facturacion.api.dto.ConversorDto;
import com.pasteleria.facturacion.api.dto.peticiones.ActualizarProductoRequest;
import com.pasteleria.facturacion.api.dto.peticiones.CrearProductoRequest;
import com.pasteleria.facturacion.api.dto.respuestas.ProductoResponse;
import com.pasteleria.facturacion.modelo.enumeraciones.Rol;
import com.pasteleria.facturacion.seguridad.AutorizacionInterceptor;
import com.pasteleria.facturacion.seguridad.RolesPermitidos;
import com.pasteleria.facturacion.seguridad.UsuarioAutenticado;
import com.pasteleria.facturacion.servicios.ServicioProductos;

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
public class ProductoControlador {

    private final ServicioProductos productos;

    public ProductoControlador(ServicioProductos productos) {
        this.productos = productos;
    }

    @GetMapping
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public List<ProductoResponse> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return productos.listar(soloDisponibles).stream().map(ConversorDto::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public ProductoResponse obtener(@PathVariable Long id) {
        return ConversorDto.aRespuesta(productos.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest request) {
        return ConversorDto.aRespuesta(productos.crear(ConversorDto.aComando(request)));
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest request) {
        return ConversorDto.aRespuesta(productos.actualizar(id, ConversorDto.aComando(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public void desactivar(@PathVariable Long id,
                           @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        productos.desactivar(id, usuario);
    }
}
