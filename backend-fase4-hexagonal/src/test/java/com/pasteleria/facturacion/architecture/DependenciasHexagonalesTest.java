package com.pasteleria.facturacion.architecture;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Hace cumplir la regla de dependencias del hexágono: todo apunta hacia el dominio.
 * domain ← application ← infrastructure (adaptadores). El dominio y la aplicación nunca conocen un adaptador ni un framework.
 */
class DependenciasHexagonalesTest {

    private static final Path RAIZ = Path.of("src/main/java/com/pasteleria/facturacion");
    private static final String BASE = "com.pasteleria.facturacion.";

    private List<String> importsProhibidos(String paquete, String... prohibidos) throws IOException {
        try (Stream<Path> archivos = Files.walk(RAIZ.resolve(paquete))) {
            return archivos.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream()
                                    .filter(l -> l.startsWith("import "))
                                    .filter(l -> Arrays.stream(prohibidos).anyMatch(l::contains))
                                    .map(l -> p.getFileName() + " -> " + l);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }).toList();
        }
    }

    @Test
    void dominioNoDependeDeNadaExterno() throws IOException {
        assertThat(importsProhibidos("domain", BASE + "application", BASE + "infrastructure",
                "org.springframework", "jakarta.persistence", "jakarta.validation", "com.fasterxml")).isEmpty();
    }

    @Test
    void aplicacionSoloConoceElDominioYSusPuertos() throws IOException {
        // única concesión: la anotación @Transactional para delimitar la transacción del caso de uso
        assertThat(importsProhibidos("application", BASE + "infrastructure", "jakarta.persistence",
                "org.springframework.stereotype", "org.springframework.data", "org.springframework.web")).isEmpty();
    }

    @Test
    void adaptadoresDeEntradaYSalidaNoSeConocenEntreSi() throws IOException {
        assertThat(importsProhibidos("infrastructure/adapter/in", BASE + "infrastructure.adapter.out")).isEmpty();
        assertThat(importsProhibidos("infrastructure/adapter/out", BASE + "infrastructure.adapter.in")).isEmpty();
    }

    @Test
    void controladoresUsanCasosDeUsoNoServicios() throws IOException {
        assertThat(importsProhibidos("infrastructure/adapter/in", BASE + "application.service")).isEmpty();
    }
}
