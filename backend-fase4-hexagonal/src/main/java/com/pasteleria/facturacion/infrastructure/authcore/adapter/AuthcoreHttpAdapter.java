package com.pasteleria.facturacion.infrastructure.authcore.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pasteleria.facturacion.domain.exception.AccesoDenegadoException;
import com.pasteleria.facturacion.domain.exception.AutenticacionException;
import com.pasteleria.facturacion.domain.exception.AuthcoreNoDisponibleException;
import com.pasteleria.facturacion.domain.port.output.AuthcorePort;
import com.pasteleria.facturacion.domain.valueobject.Rol;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementación real del puerto: consulta a authcore-service.
 * Contrato asumido: GET {base-url}{validate-path} con "Authorization: Bearer <token>" responde
 * 200 {"userId": "...", "username": "...", "role": "ADMINISTRADOR|EMPLEADO"}, o 401/403 si el token no sirve.
 * Si el contrato real de authcore-service es distinto, solo hay que cambiar ESTA clase.
 */
@Component
@ConditionalOnProperty(name = "authcore.mock.enabled", havingValue = "false")
public class AuthcoreHttpAdapter implements AuthcorePort {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AuthcoreUserResponse(String userId, String username, String role) {
    }

    private final RestClient restClient;
    private final String validatePath;

    public AuthcoreHttpAdapter(@Value("${authcore.base-url}") String baseUrl,
                               @Value("${authcore.validate-path}") String validatePath,
                               @Value("${authcore.timeout-ms:2000}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.validatePath = validatePath;
    }

    @Override
    public UsuarioAutenticado validarToken(String token) {
        AuthcoreUserResponse respuesta;
        try {
            respuesta = restClient.get().uri(validatePath)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(AuthcoreUserResponse.class);
        } catch (HttpClientErrorException e) {
            throw new AutenticacionException("Token inválido o expirado");
        } catch (RestClientException e) {
            throw new AuthcoreNoDisponibleException("No fue posible contactar a authcore-service", e);
        }
        if (respuesta == null || respuesta.userId() == null || respuesta.role() == null) {
            throw new AutenticacionException("authcore-service no devolvió una identidad válida");
        }
        return new UsuarioAutenticado(respuesta.userId(), respuesta.username() != null ? respuesta.username() : respuesta.userId(),
                convertirRol(respuesta.role()));
    }

    private Rol convertirRol(String rol) {
        try {
            return Rol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AccesoDenegadoException("El rol '" + rol + "' no tiene acceso a este sistema");
        }
    }
}
