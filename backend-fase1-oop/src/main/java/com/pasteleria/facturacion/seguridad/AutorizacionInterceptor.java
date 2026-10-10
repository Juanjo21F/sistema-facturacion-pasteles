package com.pasteleria.facturacion.seguridad;

import com.pasteleria.facturacion.modelo.excepciones.AccesoDenegadoException;
import com.pasteleria.facturacion.modelo.excepciones.AutenticacionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Pide a authcore-service (vía Autenticador) la identidad del token y valida el rol requerido por el endpoint.
 * No guarda usuarios ni contraseñas: solo confía en lo que authcore-service responde.
 */
@Component
public class AutorizacionInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_USUARIO = "usuarioAutenticado";
    private static final String PREFIJO_BEARER = "Bearer ";

    private final Autenticador authcore;

    public AutorizacionInterceptor(Autenticador authcore) {
        this.authcore = authcore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod metodo)) {
            return true;
        }
        UsuarioAutenticado usuario = authcore.validarToken(extraerToken(request));

        RolesPermitidos requerido = metodo.getMethodAnnotation(RolesPermitidos.class);
        if (requerido == null) {
            requerido = AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), RolesPermitidos.class);
        }
        if (requerido != null && !usuario.tieneAlgunRol(requerido.value())) {
            throw new AccesoDenegadoException("El rol " + usuario.rol() + " no tiene permiso para esta operación");
        }
        request.setAttribute(ATRIBUTO_USUARIO, usuario);
        return true;
    }

    private String extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO_BEARER) || cabecera.length() == PREFIJO_BEARER.length()) {
            throw new AutenticacionException("Se requiere el encabezado Authorization: Bearer <token>");
        }
        return cabecera.substring(PREFIJO_BEARER.length()).trim();
    }
}
