# Taller de Análisis de Código Estático

Este documento explica qué hicimos para el taller del 10% de Calidad de Software, sobre el mismo proyecto del procesador de notas ponderadas que ya teníamos. La idea no era rehacer la calculadora, sino tomarla como está y meterle encima todo el proceso de análisis y aseguramiento de calidad: cobertura, análisis estático, integración continua y pruebas de aceptación.

## 1. Estado inicial del proyecto

Antes de tocar nada, así estaba el repo:

- 1 clase de producción: `ProcesadorNotas.java`, con el método `calcularNotaFinal`.
- 13 pruebas unitarias con JUnit 5, incluyendo 3 pruebas de tiempo (3, 100 y 10.000 notas).
- Gradle con un solo plugin (`java`), sin JaCoCo, sin SonarCloud, sin GitHub Actions.
- Las 13 pruebas pasaban (`BUILD SUCCESSFUL`).
- Nada de Karate, nada de cobertura medida, nada de CI.

Antes de empezar a cambiar código, le agregamos el plugin de JaCoCo a `build.gradle` sin tocar la clase de producción, solo para medir la cobertura real que tenían esas 13 pruebas originales. El resultado:

| Métrica | Valor |
|---|---|
| Cobertura de línea | 22/22 → **100%** |
| Cobertura de ramas (branch) | 23/28 → **82.1%** |

O sea que las 13 pruebas ya ejecutaban todas las líneas, pero dejaban 5 combinaciones de condiciones sin probar (ramas de los `if` con `||`). Esto fue útil porque nos dio pistas concretas de qué faltaba, en vez de inventarnos casos al azar.

## 2. Configuración que agregamos

### JaCoCo

En `build.gradle` agregamos el plugin `jacoco` y la tarea `jacocoTestReport`, configurada para generar tanto HTML (para revisar a ojo) como XML (para que SonarCloud lo pueda leer). Quedó enganchado para que corra automáticamente después de `test`:

```groovy
test {
    finalizedBy jacocoTestReport
}
```

Para verlo:

```powershell
.\gradlew.bat clean test jacocoTestReport
start build\reports\jacoco\test\html\index.html
```

### SonarCloud

Agregamos el plugin `org.sonarqube` y la configuración con `sonar.organization`, `sonar.projectKey` y la ruta al XML de JaCoCo. **Esto quedó listo en el código, pero el análisis todavía no se ha ejecutado**, porque hace falta un token de SonarCloud que nosotros (la IA que trabajó en esto) no tenemos ni podemos generar. Ver la sección 7 para el paso exacto que falta.

### Karate

Agregamos la dependencia `com.intuit.karate:karate-junit5` y un archivo `.feature` con los escenarios de aceptación. Como `ProcesadorNotas` es una clase Java normal (no una API), usamos la interoperabilidad Java de Karate (`Java.type(...)`) en vez de montar un servicio web solo para poder probarlo. Nos pareció la opción correcta para no sobrecomplicar un taller que es sobre análisis estático, no sobre arquitectura.

### GitHub Actions

Creamos `.github/workflows/quality.yml`, que en cada push o PR a `main`: compila, corre JUnit + Karate, genera el reporte de JaCoCo, intenta correr SonarCloud (si existe el secreto `SONAR_TOKEN`) y sube como artefactos el reporte de pruebas, el de cobertura y el `.jar` generado.

## 3. Problemas reales que encontramos y corregimos

Importante: **no usamos SonarCloud para encontrar estos problemas** porque todavía no hemos podido ejecutarlo (ver sección 7). Los encontramos revisando el código a mano y mirando el reporte de JaCoCo. Son reales, no inventados para la entrega.

### 3.1 `Double.NaN` pasaba la validación (bug real)

El método validaba que cada nota estuviera entre 0.0 y 5.0, y cada porcentaje entre 0 y 100, con comparaciones `<` y `>`. El problema es que en Java (y en general en IEEE 754), **`NaN` no es menor, mayor ni igual a ningún número**: `NaN < 0.0` y `NaN > 5.0` dan `false` los dos. Eso significa que una nota `Double.NaN` pasaba la validación sin ser un número válido, y contaminaba el resultado final.

Lo corregimos agregando `Double.isNaN(...)` explícito en las dos validaciones (nota y porcentaje), y agregamos dos pruebas nuevas (`rechazaNotaNaN`, `rechazaPorcentajeNaN`) que fallaban antes de la corrección y pasan después.

### 3.2 Ramas sin probar (gracias a JaCoCo)

El reporte de cobertura de ramas mostró 5 combinaciones de `if` sin cubrir. Revisando cuáles eran, encontramos que nadie había probado:

- Que solo una de las dos listas sea `null` (antes solo se probaba `notas = null`).
- Que solo una de las dos listas esté vacía (antes solo se probaba con las dos vacías).
- Un porcentaje negativo (sí se probaba un porcentaje mayor a 100, pero no uno negativo).
- Un elemento `null` *dentro* de la lista de notas o de porcentajes (distinto de que la lista completa sea `null`).

Agregamos una prueba por cada caso. Con eso la cobertura de ramas subió a 100%.

### 3.3 El método hacía demasiadas cosas (complejidad / mantenibilidad)

`calcularNotaFinal` validaba las listas, validaba cada nota, validaba cada porcentaje, acumulaba la suma, validaba la suma y además calculaba el resultado, todo en un solo método con varios `if` anidados y dos ciclos `for`. Esto es justo el tipo de cosa que las herramientas de análisis estático marcan como complejidad cognitiva alta, aunque en este caso lo detectamos antes de correr SonarCloud, simplemente leyendo el código.

Lo separamos en métodos privados con una sola responsabilidad cada uno: `validarListas`, `validarNota`, `validarPorcentaje` y `validarSumaPorcentajes`. El método principal quedó como una lista de pasos fácil de leer de arriba a abajo. **No cambiamos ninguna regla de negocio ni ningún mensaje de error** — las 20 pruebas (más las 5 de Karate) siguen pasando exactamente igual después del refactor.

### 3.4 Cosas que quedaron igual, a propósito

No tocamos los límites de tiempo de las pruebas de rendimiento (10 ms / 50 ms / 200 ms) porque funcionan bien en esta máquina y cambiarlos sin evidencia real de que fallan en GitHub Actions sería inventar un problema que no confirmamos. Si en CI llegan a fallar por ser una máquina más lenta, es algo que habría que ajustar con datos reales de esas ejecuciones, no de antemano.

Tampoco agregamos validación para casos que no pueden pasar en la práctica (por ejemplo, el constructor de la clase no necesita validar nada porque no recibe parámetros). Evitamos sobre-validar código que ya es correcto solo para "verse mejor".

## 4. Métricas: antes y después

| Métrica | Antes | Después |
|---|---|---|
| Pruebas unitarias (JUnit 5) | 13 | 20 |
| Pruebas de aceptación (Karate) | 0 | 5 |
| Total de pruebas | 13 | 25 |
| Cobertura de línea (JaCoCo) | 100% (22/22) | 100% (31/31) |
| Cobertura de ramas (JaCoCo) | 82.1% (23/28) | 100% (32/32) |
| Bugs conocidos | 1 (NaN no validado) | 0 conocidos |
| Métodos en `ProcesadorNotas` | 1 | 5 (1 público + 4 privados de validación) |
| Quality Gate de SonarCloud | No evaluado | No evaluado todavía (ver sección 7) |
| GitHub Actions | No existía | ✓ verde (ver sección 6) |

Todos estos números salen de correr `./gradlew clean test jacocoTestReport` localmente y leer el XML/HTML que genera JaCoCo, no son estimaciones.

## 5. Pruebas Karate

El archivo [`src/test/resources/karate/procesador-notas.feature`](../../src/test/resources/karate/procesador-notas.feature) tiene 5 escenarios:

1. Calcula correctamente una nota final ponderada.
2. Rechaza una nota fuera del rango permitido.
3. Rechaza porcentajes que no suman 100.
4. Rechaza una entrada incorrecta (listas de distinto tamaño).
5. Rechaza una entrada incorrecta (lista vacía).

Corren junto con las pruebas de JUnit al ejecutar `.\gradlew.bat clean test` (Karate se integra como un `@Test` más de JUnit 5 a través de `ProcesadorNotasAcceptanceTest`). Las 5 pasaron en la última ejecución local.

## 6. Integración continua

El workflow de GitHub Actions (`.github/workflows/quality.yml`) corre en cada push/PR a `main`. La primera versión que subimos en realidad no registraba ningún job (GitHub la mostraba usando el nombre del archivo en vez del `name:` del workflow, que es la señal de que no pudo interpretarlo), así que tuvo que corregirse — el problema era usar `secrets.SONAR_TOKEN` directamente dentro de un `if:`, que no es un patrón seguro; se cambió por una variable de entorno a nivel de job que sí se puede leer en el `if`.

Después de esa corrección, la ejecución quedó verde de verdad:

- Run: https://github.com/spalacioc05/calculadora-gradle/actions/runs/39346477989 (✓ success, 25s)
- Compila, corre las 20 pruebas JUnit + 5 de Karate, genera el reporte de JaCoCo y publica tres artefactos: el reporte de pruebas, el reporte de cobertura y el `.jar`.
- El paso de SonarCloud se omite con el aviso `No se encontro el secreto SONAR_TOKEN...` tal como estaba pensado, porque ese secreto todavía no existe (ver sección 7).

## 7. Qué quedó pendiente (acción manual)

Esto es lo único que no pudimos terminar nosotros directamente, porque requiere una cuenta y credenciales que no están disponibles en este entorno:

1. Crear una cuenta en [SonarCloud](https://sonarcloud.io) (se puede hacer con el login de GitHub).
2. Importar el repositorio `spalacioc05/calculadora-gradle` como un proyecto nuevo.
3. SonarCloud va a asignar una organización y una clave de proyecto. Si no coinciden exactamente con `spalacioc05` y `spalacioc05_calculadora-gradle` (que dejamos como valores por convención en `build.gradle`), hay que ajustar esas dos propiedades en el bloque `sonar { properties { ... } }`.
4. Generar un token de SonarCloud (Account → Security → Generate Token).
5. En GitHub: Settings → Secrets and variables → Actions → New repository secret, con nombre `SONAR_TOKEN` y el valor del token.
6. Volver a correr el workflow (o hacer un push nuevo) para que el paso de SonarCloud deje de omitirse y se ejecute el análisis real.

Mientras ese secreto no exista, el workflow sigue funcionando (compila, prueba, genera cobertura), pero el paso de SonarCloud se salta con un aviso explícito en los logs — no se inventa un resultado falso.

Una vez se agregue el token, en el dashboard de SonarCloud se podrá ver el Quality Gate, los code smells reales (si aparece alguno más allá de los que ya corregimos a mano) y la deuda técnica estimada. Ese reporte no está incluido en esta entrega porque, siendo honestos, no se ha podido generar todavía.

## 8. Resumen para la sustentación

En una frase: el procesador de notas ya tenía pruebas unitarias, nosotros le agregamos medición de cobertura real con JaCoCo, encontramos y corregimos un bug genuino (`NaN`) y varios huecos de prueba usando el propio reporte de cobertura como guía, bajamos la complejidad del método principal separándolo en validaciones más pequeñas, le sumamos pruebas de aceptación con Karate sobre las reglas de negocio, y automatizamos todo con GitHub Actions. Lo único que falta para que SonarCloud muestre su dashboard real es un token que hay que generar manualmente.
