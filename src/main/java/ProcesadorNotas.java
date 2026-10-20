import java.util.List;

public class ProcesadorNotas {

    private static final double NOTA_MINIMA = 0.0;
    private static final double NOTA_MAXIMA = 5.0;
    private static final double PORCENTAJE_MINIMO = 0.0;
    private static final double PORCENTAJE_MAXIMO = 100.0;
    private static final double TOLERANCIA_SUMA_PORCENTAJES = 0.0001;
    private static final double SUMA_PORCENTAJES_ESPERADA = 100.0;

    public double calcularNotaFinal(List<Double> notas, List<Double> porcentajes) {
        validarListas(notas, porcentajes);

        double sumaPorcentajes = 0.0;

        // Recorremos las listas para revisar cada nota y cada porcentaje.
        // i representa la posición actual dentro de las listas.
        for (int i = 0; i < notas.size(); i++) {
            Double nota = notas.get(i);
            Double porcentaje = porcentajes.get(i);

            validarNota(nota);
            validarPorcentaje(porcentaje);

            // Acumulamos los porcentajes para comprobar su suma al terminar el recorrido.
            sumaPorcentajes += porcentaje;
        }

        validarSumaPorcentajes(sumaPorcentajes);

        double notaFinal = 0.0;

        // Recorremos nuevamente las notas para calcular el aporte de cada una.
        // i vuelve a representar la posición que comparten ambas listas.
        for (int i = 0; i < notas.size(); i++) {
            notaFinal += notas.get(i) * porcentajes.get(i) / 100;
        }

        // return entrega la nota ponderada que se terminó de acumular.
        return notaFinal;
    }

    private void validarListas(List<Double> notas, List<Double> porcentajes) {
        // Se lanza esta excepción porque sin las listas no se puede hacer el cálculo.
        if (notas == null || porcentajes == null) {
            throw new IllegalArgumentException("Las listas no pueden ser null");
        }

        // Las dos listas deben tener al menos un elemento.
        if (notas.isEmpty() || porcentajes.isEmpty()) {
            throw new IllegalArgumentException("Las listas no pueden estar vacías");
        }

        // Cada nota necesita un porcentaje en la misma posición.
        if (notas.size() != porcentajes.size()) {
            throw new IllegalArgumentException("Las listas deben tener el mismo tamaño");
        }
    }

    private void validarNota(Double nota) {
        // Double.isNaN se revisa aparte porque NaN no es menor, mayor ni igual
        // a ningún número: las comparaciones con < y > siempre dan false y el
        // valor pasaría la validación sin ser un número real.
        if (nota == null || Double.isNaN(nota) || nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException("Cada nota debe estar entre 0.0 y 5.0");
        }
    }

    private void validarPorcentaje(Double porcentaje) {
        if (porcentaje == null || Double.isNaN(porcentaje)
                || porcentaje < PORCENTAJE_MINIMO || porcentaje > PORCENTAJE_MAXIMO) {
            throw new IllegalArgumentException("Cada porcentaje debe estar entre 0 y 100");
        }
    }

    private void validarSumaPorcentajes(double sumaPorcentajes) {
        // Se usa una tolerancia pequeña porque los valores double pueden tener
        // diferencias mínimas al sumar muchos decimales, como 10.000 veces 0.01.
        if (Math.abs(sumaPorcentajes - SUMA_PORCENTAJES_ESPERADA) > TOLERANCIA_SUMA_PORCENTAJES) {
            throw new IllegalArgumentException("La suma de los porcentajes debe ser 100");
        }
    }
}
