package com.pasteleria.facturacion.architecture;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Hace cumplir la regla de las capas: cada capa solo puede depender de la inmediatamente inferior.
 * presentation → business → (dataaccess, integration). Nunca hacia arriba ni saltándose la capa de negocio.
 */
class DependenciasEntreCapasTest {

    private static final Path RAIZ = Path.of("src/main/java/com/pasteleria/facturacion");
    private static final String BASE = "com.pasteleria.facturacion.";

    private List<String> importsProhibidos(String capa, String... prohibidas) throws IOException {
        try (Stream<Path> archivos = Files.walk(RAIZ.resolve(capa))) {
            return archivos.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream()
                                    .filter(l -> l.startsWith("import "))
                                    .filter(l -> java.util.Arrays.stream(prohibidas).anyMatch(x -> l.contains(BASE + x + ".")))
                                    .map(l -> p.getFileName() + " -> " + l);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }).toList();
        }
    }

    @Test
    void presentacionNoConoceDatosNiIntegracion() throws IOException {
        assertThat(importsProhibidos("presentation", "dataaccess", "integration")).isEmpty();
    }

    @Test
    void negocioNoConocePresentacion() throws IOException {
        assertThat(importsProhibidos("business", "presentation")).isEmpty();
    }

    @Test
    void datosEIntegracionNoConocenNegocioNiPresentacion() throws IOException {
        assertThat(importsProhibidos("dataaccess", "presentation", "business")).isEmpty();
        assertThat(importsProhibidos("integration", "presentation", "dataaccess")).isEmpty();
    }
}
