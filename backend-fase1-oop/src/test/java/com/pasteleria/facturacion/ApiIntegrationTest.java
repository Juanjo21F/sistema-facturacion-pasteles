package com.pasteleria.facturacion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Prueba de extremo a extremo (HTTP + H2 + authcore simulado): recorre los flujos principales del sistema.
 * Es idéntica en las tres arquitecturas: el comportamiento externo no cambia, solo la estructura interna.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final String ADMIN = "Bearer admin-token";
    private static final String EMPLEADO = "Bearer empleado-token";

    @Autowired
    private MockMvc mvc;

    private ResultActions enviar(MockHttpServletRequestBuilder req, String token, String json) throws Exception {
        req.contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            req.header("Authorization", token);
        }
        if (json != null) {
            req.content(json);
        }
        return mvc.perform(req);
    }

    private long idDe(ResultActions r, String campo) throws Exception {
        String cuerpo = r.andReturn().getResponse().getContentAsString();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + campo + "\":(\\d+)").matcher(cuerpo);
        if (!m.find()) {
            throw new IllegalStateException("No se encontró " + campo + " en " + cuerpo);
        }
        return Long.parseLong(m.group(1));
    }

    @Test
    void flujoCompletoDeFacturacion() throws Exception {
        // --- seguridad (authcore) ---
        enviar(get("/api/productos"), null, null).andExpect(status().isUnauthorized());
        enviar(get("/api/productos"), "Bearer malo", null).andExpect(status().isUnauthorized());
        enviar(post("/api/productos"), EMPLEADO, "{}").andExpect(status().isForbidden());

        // --- productos ---
        String producto = "{\"codigoUnico\":\"TOR-IT\",\"nombre\":\"Torta IT\",\"idCategoria\":1,\"precio\":45000,\"stock\":10}";
        long idProducto = idDe(enviar(post("/api/productos"), ADMIN, producto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("Tortas"))
                .andExpect(jsonPath("$.estado").value("ACTIVO")), "idProducto");
        enviar(post("/api/productos"), ADMIN, producto).andExpect(status().isConflict());               // código duplicado
        enviar(post("/api/productos"), ADMIN,
                "{\"codigoUnico\":\"X\",\"nombre\":\"X\",\"idCategoria\":1,\"precio\":0,\"stock\":1}")
                .andExpect(status().isBadRequest());                                                    // precio inválido
        enviar(post("/api/productos"), ADMIN,
                "{\"codigoUnico\":\"Y\",\"nombre\":\"Y\",\"idCategoria\":1,\"precio\":5,\"stock\":-1}")
                .andExpect(status().isBadRequest());                                                    // stock negativo

        // --- clientes ---
        String cliente = "{\"documentoIdentidad\":\"900100\",\"nombreCompleto\":\"Ana IT\",\"telefono\":\"300\",\"correo\":\"ana@it.com\"}";
        long idCliente = idDe(enviar(post("/api/clientes"), EMPLEADO, cliente).andExpect(status().isCreated()), "idCliente");
        enviar(post("/api/clientes"), EMPLEADO, cliente).andExpect(status().isConflict());              // documento duplicado

        // --- venta: total calculado por el backend y stock descontado ---
        String venta = "{\"idCliente\":" + idCliente + ",\"detalles\":[{\"idProducto\":" + idProducto + ",\"cantidad\":3}]}";
        long idFactura = idDe(enviar(post("/api/facturas"), EMPLEADO, venta)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(135000.0))
                .andExpect(jsonPath("$.estadoFactura").value("EMITIDA"))
                .andExpect(jsonPath("$.numeroFactura").value(org.hamcrest.Matchers.startsWith("FAC-")))
                .andExpect(jsonPath("$.detalles", hasSize(1))), "idFactura");
        enviar(get("/api/productos/" + idProducto), EMPLEADO, null).andExpect(jsonPath("$.stock").value(7));

        // --- stock insuficiente: 422 y nada cambia ---
        String ventaGrande = "{\"idCliente\":" + idCliente + ",\"detalles\":[{\"idProducto\":" + idProducto + ",\"cantidad\":8}]}";
        enviar(post("/api/facturas"), EMPLEADO, ventaGrande).andExpect(status().isUnprocessableEntity());
        enviar(get("/api/productos/" + idProducto), EMPLEADO, null).andExpect(jsonPath("$.stock").value(7));

        // --- reporte: incluye la venta emitida (solo ADMIN) ---
        int mes = java.time.LocalDate.now().getMonthValue();
        int anio = java.time.LocalDate.now().getYear();
        enviar(get("/api/reportes/ventas?mes=" + mes + "&anio=" + anio), EMPLEADO, null).andExpect(status().isForbidden());
        enviar(get("/api/reportes/ventas?mes=" + mes + "&anio=" + anio), ADMIN, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0].cantidadVendida").value(3));
        enviar(get("/api/reportes/ventas?mes=13&anio=" + anio), ADMIN, null).andExpect(status().isBadRequest());

        // --- anulación: solo ADMIN, restaura stock una sola vez y sale del reporte ---
        enviar(put("/api/facturas/" + idFactura + "/anular"), EMPLEADO, null).andExpect(status().isForbidden());
        enviar(put("/api/facturas/" + idFactura + "/anular"), ADMIN, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estadoFactura").value("ANULADA"));
        enviar(get("/api/productos/" + idProducto), ADMIN, null).andExpect(jsonPath("$.stock").value(10));
        enviar(put("/api/facturas/" + idFactura + "/anular"), ADMIN, null).andExpect(status().isUnprocessableEntity());
        enviar(get("/api/productos/" + idProducto), ADMIN, null).andExpect(jsonPath("$.stock").value(10));
        enviar(get("/api/reportes/ventas?mes=" + mes + "&anio=" + anio), ADMIN, null)
                .andExpect(jsonPath("$.productos", hasSize(0)));

        // --- desactivar: producto y cliente inactivos no se pueden vender ---
        enviar(delete("/api/productos/" + idProducto), ADMIN, null).andExpect(status().isNoContent());
        enviar(post("/api/facturas"), EMPLEADO, venta).andExpect(status().isUnprocessableEntity());
        enviar(get("/api/productos?soloDisponibles=true"), ADMIN, null).andExpect(jsonPath("$", hasSize(0)));
        enviar(delete("/api/clientes/" + idCliente), EMPLEADO, null).andExpect(status().isNoContent());
        enviar(get("/api/facturas/99999"), ADMIN, null).andExpect(status().isNotFound());
    }
}
