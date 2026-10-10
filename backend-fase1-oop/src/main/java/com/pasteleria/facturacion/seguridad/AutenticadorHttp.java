package com.pasteleria.facturacion.seguridad;

import com.pasteleria.facturacion.modelo.excepciones.AutenticacionException;
import com.pasteleria.facturacion.modelo.excepciones.AuthcoreNoDisponibleException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementación real: consulta a authcore-service.
 * Contrato asumido: GET {base-url}{validate-path} con "Authorization: Bearer <token>" responde
 * 200 {"userId": "...", "username": "...", "role": "ADMINISTRADOR|EMPLEADO"}, o 401/403 si el token no sirve.
 * Si el contrato real de authcore-service es distinto, solo hay que cambiar ESTA clase.
 */
@Component
@ConditionalOnProperty(name = "authcore.mock.enabled", havingValue = "false")
public class AutenticadorHttp extends AutenticadorBase {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AuthcoreUserResponse(String userId, String username, String role) {
    }

    private final RestClient restClient;
    private final String validatePath;

    public AutenticadorHttp(@Value("${authcore.base-url}") String baseUrl,
                               @Value("${authcore.validate-path}") String validatePath,
                               @Value("${authcore.timeout-ms:2000}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.validatePath = validatePath;
    }

    @Override
    protected UsuarioAutenticado consultarIdentidad(String token) {
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

}
