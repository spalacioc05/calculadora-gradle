import com.intuit.karate.junit5.Karate;

class ProcesadorNotasAcceptanceTest {

    @Karate.Test
    Karate pruebasDeAceptacion() {
        return Karate.run("classpath:karate/procesador-notas.feature");
    }
}
