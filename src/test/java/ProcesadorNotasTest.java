import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@DisplayName("Procesador de notas ponderadas")
class ProcesadorNotasTest {

    // Margen pequeño para comparar valores double.
    private static final double TOLERANCIA = 0.0001;

    private ProcesadorNotas procesador;
    private List<Double> notas;
    private List<Double> porcentajes;

    // @BeforeEach se ejecuta antes de cada prueba y prepara los objetos comunes.
    // Los datos propios de cada escenario se siguen preparando en ARRANGE.
    @BeforeEach
    void preparar() {
        procesador = new ProcesadorNotas();
        notas = new ArrayList<>();
        porcentajes = new ArrayList<>();
    }

    // @AfterEach se ejecuta después de cada prueba y limpia los datos utilizados.
    @AfterEach
    void limpiar() {
        notas.clear();
        porcentajes.clear();
    }

    @Test
    @DisplayName("Calcula correctamente una nota ponderada")
    void calculaNotaFinalCorrectamente() {
        // ARRANGE
        notas.add(4.0);
        notas.add(3.5);
        notas.add(4.5);
        porcentajes.add(30.0);
        porcentajes.add(30.0);
        porcentajes.add(40.0);
        double resultadoEsperado = 4.05;

        // ACT
        double resultadoObtenido = procesador.calcularNotaFinal(notas, porcentajes);

        // ASSERT
        assertEquals(resultadoEsperado, resultadoObtenido, TOLERANCIA);
    }

    @Test
    @DisplayName("Acepta una nota en el límite inferior")
    void aceptaNotaCero() {
        // ARRANGE
        notas.add(0.0);
        notas.add(4.0);
        porcentajes.add(50.0);
        porcentajes.add(50.0);
        double resultadoEsperado = 2.0;

        // ACT
        double resultadoObtenido = procesador.calcularNotaFinal(notas, porcentajes);

        // ASSERT
        assertEquals(resultadoEsperado, resultadoObtenido, TOLERANCIA);
    }

    @Test
    @DisplayName("Acepta una nota en el límite superior")
    void aceptaNotaCinco() {
        // ARRANGE
        notas.add(5.0);
        notas.add(4.0);
        porcentajes.add(50.0);
        porcentajes.add(50.0);
        double resultadoEsperado = 4.5;

        // ACT
        double resultadoObtenido = procesador.calcularNotaFinal(notas, porcentajes);

        // ASSERT
        assertEquals(resultadoEsperado, resultadoObtenido, TOLERANCIA);
    }

    @Test
    @DisplayName("Rechaza una nota mayor que cinco")
    void rechazaNotaMayorQueCinco() {
        // ARRANGE
        notas.add(4.0);
        notas.add(5.8);
        notas.add(3.0);
        porcentajes.add(30.0);
        porcentajes.add(30.0);
        porcentajes.add(40.0);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.calcularNotaFinal(notas, porcentajes));
    }

    @Test
    @DisplayName("Rechaza una nota negativa")
    void rechazaNotaNegativa() {
        // ARRANGE
        notas.add(4.0);
        notas.add(-1.0);
        notas.add(3.0);
        porcentajes.add(30.0);
        porcentajes.add(30.0);
        porcentajes.add(40.0);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.calcularNotaFinal(notas, porcentajes));
    }

    @Test
    @DisplayName("Rechaza listas de diferente tamaño")
    void rechazaListasDeDiferenteTamano() {
        // ARRANGE
        notas.add(4.0);
        notas.add(3.5);
        notas.add(4.5);
        porcentajes.add(50.0);
        porcentajes.add(50.0);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.calcularNotaFinal(notas, porcentajes));
    }

    @Test
    @DisplayName("Rechaza porcentajes que no suman cien")
    void rechazaPorcentajesQueNoSumanCien() {
        // ARRANGE
        notas.add(4.0);
        notas.add(3.5);
        notas.add(4.5);
        porcentajes.add(30.0);
        porcentajes.add(30.0);
        porcentajes.add(30.0);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.calcularNotaFinal(notas, porcentajes));
    }

    @Test
    @DisplayName("Rechaza listas vacías")
    void rechazaListasVacias() {
        // ARRANGE
        // Las listas ya fueron creadas vacías por @BeforeEach.

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.calcularNotaFinal(notas, porcentajes));
    }

    @Test
    @DisplayName("Rechaza una lista null")
    void rechazaListaNull() {
        // ARRANGE
        porcentajes.add(100.0);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.calcularNotaFinal(null, porcentajes));
    }

    @Test
    @DisplayName("Rechaza un porcentaje mayor que cien")
    void rechazaPorcentajeMayorQueCien() {
        // ARRANGE
        notas.add(4.0);
        porcentajes.add(120.0);

        // ACT + ASSERT
        assertThrows(
                IllegalArgumentException.class,
                () -> procesador.calcularNotaFinal(notas, porcentajes));
    }

    @Test
    @DisplayName("Calcula una lista pequeña dentro del tiempo esperado")
    void calculaListaPequenaDentroDelTiempoEsperado() {
        // ARRANGE
        notas.add(4.0);
        notas.add(3.5);
        notas.add(4.5);
        porcentajes.add(30.0);
        porcentajes.add(30.0);
        porcentajes.add(40.0);
        Duration tiempoMaximo = Duration.ofMillis(10);
        double resultadoEsperado = 4.05;

        // ACT + ASSERT
        double resultadoObtenido = assertTimeout(
                tiempoMaximo,
                () -> procesador.calcularNotaFinal(notas, porcentajes));

        assertEquals(resultadoEsperado, resultadoObtenido, TOLERANCIA);
    }

    @Test
    @DisplayName("Calcula cien notas dentro del tiempo esperado")
    void calculaMayorCargaDentroDelTiempoEsperado() {
        // ARRANGE
        // Los datos se generan antes de comenzar a medir.
        for (int i = 0; i < 100; i++) {
            notas.add(4.0);
            porcentajes.add(1.0);
        }
        Duration tiempoMaximo = Duration.ofMillis(50);
        double resultadoEsperado = 4.0;

        // ACT + ASSERT
        double resultadoObtenido = assertTimeout(
                tiempoMaximo,
                () -> procesador.calcularNotaFinal(notas, porcentajes));

        assertEquals(resultadoEsperado, resultadoObtenido, TOLERANCIA);
    }

    @Test
    @DisplayName("Calcula diez mil notas dentro del tiempo esperado")
    void calculaListaGrandeDentroDelTiempoEsperado() {
        // ARRANGE
        // Los 10.000 elementos se preparan antes de comenzar a medir.
        for (int i = 0; i < 10_000; i++) {
            notas.add(4.0);
            porcentajes.add(0.01);
        }
        Duration tiempoMaximo = Duration.ofMillis(200);
        double resultadoEsperado = 4.0;

        // ACT + ASSERT
        double resultadoObtenido = assertTimeout(
                tiempoMaximo,
                () -> procesador.calcularNotaFinal(notas, porcentajes));

        assertEquals(resultadoEsperado, resultadoObtenido, TOLERANCIA);
    }
}
