import java.util.List;

public class ProcesadorNotas {

    public double calcularNotaFinal(List<Double> notas, List<Double> porcentajes) {

        // Primero comprobamos que las listas existan.
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

        double sumaPorcentajes = 0.0;

        // Recorremos las listas para revisar cada nota y cada porcentaje.
        // i representa la posición actual dentro de las listas.
        for (int i = 0; i < notas.size(); i++) {
            // get(i) obtiene el elemento guardado en la posición i.
            Double nota = notas.get(i);
            Double porcentaje = porcentajes.get(i);

            // Una nota válida debe estar entre 0.0 y 5.0.
            if (nota == null || nota < 0.0 || nota > 5.0) {
                throw new IllegalArgumentException("Cada nota debe estar entre 0.0 y 5.0");
            }

            // Un porcentaje válido debe estar entre 0 y 100.
            if (porcentaje == null || porcentaje < 0.0 || porcentaje > 100.0) {
                throw new IllegalArgumentException("Cada porcentaje debe estar entre 0 y 100");
            }

            // Acumulamos los porcentajes para comprobar su suma al terminar el recorrido.
            sumaPorcentajes += porcentaje;
        }

        // Se usa una tolerancia pequeña porque los valores double pueden tener
        // diferencias mínimas al sumar muchos decimales, como 10.000 veces 0.01.
        if (Math.abs(sumaPorcentajes - 100.0) > 0.0001) {
            throw new IllegalArgumentException("La suma de los porcentajes debe ser 100");
        }

        double notaFinal = 0.0;

        // Recorremos nuevamente las notas para calcular el aporte de cada una.
        // i vuelve a representar la posición que comparten ambas listas.
        for (int i = 0; i < notas.size(); i++) {
            // get(i) permite usar la nota y su porcentaje correspondiente.
            // Cada aporte se acumula en notaFinal.
            notaFinal += notas.get(i) * porcentajes.get(i) / 100;
        }

        // return entrega la nota ponderada que se terminó de acumular.
        return notaFinal;
    }
}
