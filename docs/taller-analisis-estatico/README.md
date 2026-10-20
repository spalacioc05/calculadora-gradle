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
| Quality Gate de SonarCloud (análisis CI) | No evaluado | No evaluado todavía — falta apagar "Automatic Analysis" (ver sección 6) |
| GitHub Actions (build + pruebas + cobertura) | No existía | ✓ verde |

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

El workflow de GitHub Actions (`.github/workflows/quality.yml`) corre en cada push/PR a `main`. No quedó bien a la primera — tuvo tres intentos, y vale la pena dejar los tres por escrito en vez de solo mostrar el que sí funcionó, porque cada uno fue un problema real y distinto:

**Intento 1 — el workflow no registraba ningún job.**
La primera versión usaba `secrets.SONAR_TOKEN` directamente dentro de un `if:`, y GitHub ni siquiera lograba interpretar el archivo (se notaba porque mostraba el nombre del archivo en vez del `name:` que le habíamos puesto). Se corrigió cambiándolo por una variable de entorno a nivel de job.

- Run: https://github.com/spalacioc05/calculadora-gradle/actions/runs/39346477989 — ✓ success, 25s. Compila, corre las 20 pruebas JUnit + 5 de Karate, genera JaCoCo y publica los artefactos. El paso de SonarCloud se omite con el aviso `No se encontro el secreto SONAR_TOKEN...`, porque en ese momento el secreto todavía no existía.

**Intento 2 — SonarCloud exige Java 21.**
Después se agregó el secreto `SONAR_TOKEN` en GitHub (eso sí fue manual, lo hizo quien tiene la cuenta). Con el secreto puesto, el paso de SonarCloud dejó de omitirse, pero falló:

- Run: https://github.com/spalacioc05/calculadora-gradle/actions/runs/39346617634 — build, JUnit, Karate y JaCoCo corrieron bien, pero `:sonar` falló con `Java 17 is not supported. Please upgrade to Java 21 or newer`. El escáner de SonarCloud ahora exige JDK 21 o superior, aparte de en qué Java compile el proyecto. **Esta ejecución no cuenta como análisis exitoso.**

Se agregó un paso `Configurar JDK 21 para SonarCloud` justo antes de `./gradlew sonar`, que solo cambia el JDK para ese paso (compilación, JUnit, Karate y JaCoCo siguen en Java 17, como se pidió).

**Intento 3 — SonarCloud con "Automatic Analysis" activado.**
Con JDK 21 puesto, `:sonar` avanzó más pero volvió a fallar, ahora con un mensaje distinto:

- Run: https://github.com/spalacioc05/calculadora-gradle/actions/runs/39348495892 — el paso `Configurar JDK 21 para SonarCloud` corrió bien, pero `:sonar` falló con: `You are running CI analysis while Automatic Analysis is enabled. Please consider disabling one or the other.` **Tampoco cuenta como análisis exitoso.**

Esto confirma algo que vimos por la API pública de SonarCloud: el proyecto `spalacioc05_calculadora-gradle` ya tenía un análisis **automático** registrado (el GitHub App de SonarCloud lo generó solo, sobre el commit inicial `ade29c0`, antes de que empezáramos a tocar nada). SonarCloud no deja correr los dos modos de análisis (automático y por CI) al mismo tiempo sobre el mismo proyecto, y hay que elegir uno. Como lo que pide el taller es análisis por CI con la cobertura de JaCoCo, el que hay que apagar es el automático.

**Esto no se corrige desde el repositorio ni desde GitHub Actions — es una opción del proyecto en sonarcloud.io.** El paso exacto que falta (manual, requiere la cuenta de SonarCloud):

1. Entrar a https://sonarcloud.io y abrir el proyecto `calculadora-gradle`.
2. Ir a **Administration → Analysis Method**.
3. Apagar **"Automatic Analysis"** (dejarlo solo en análisis por CI / GitHub Actions).
4. Guardar, y volver a ejecutar el workflow (un push nuevo, o "Re-run jobs" en Actions).

Mientras esa opción no se apague en sonarcloud.io, `:sonar` seguirá fallando con el mismo mensaje sin importar qué se cambie en el workflow.

### Lo que sí se pudo ver de SonarCloud, aunque sea del análisis automático viejo

Como el proyecto en SonarCloud es público, se pudo consultar su API sin necesitar el token (`sonarcloud.io/api/measures/component` y `.../issues/search`). Esto corresponde al análisis automático del commit inicial `ade29c0` — **antes** de cualquiera de nuestras correcciones, y sin la cobertura de JaCoCo (el análisis automático no la conoce):

| Métrica | Valor (commit inicial, análisis automático) |
|---|---|
| Bugs | 0 |
| Vulnerabilidades | 0 |
| Code smells | 2 |
| Líneas duplicadas | 0.0% |
| Complejidad ciclomática | 15 |
| Complejidad cognitiva | 14 |
| Deuda técnica estimada | 20 minutos |

Los 2 code smells son la misma regla en los dos archivos: `java:S1220 — Move this file to a named package`, tanto en `ProcesadorNotas.java` como en `ProcesadorNotasTest.java`. Es un hallazgo real y menor (el proyecto no usa paquetes Java, todo queda en el paquete por defecto) que no estaba en nuestra lista de la sección 3 porque lo encontramos vía la API pública, no leyendo el código a mano. No lo corregimos en esta entrega porque el pedido puntual era arreglar el workflow, no reestructurar paquetes — queda anotado como trabajo pendiente de baja prioridad.

Esta tabla **no** es el análisis de CI con el código final ni con la cobertura de JaCoCo — es solo lo único de SonarCloud que pudimos confirmar de verdad hasta ahora. El análisis que sí integra nuestra cobertura y nuestro código final sigue pendiente de que se apague "Automatic Analysis".

## 7. Qué quedó pendiente (acción manual)

Dos cosas, ambas fuera del repositorio:

1. **Apagar "Automatic Analysis" en sonarcloud.io** (ver sección 6) — es lo único que bloquea que `:sonar` termine bien ahora que el secreto y el JDK 21 ya están resueltos.
2. Después de apagarlo, volver a correr el workflow y confirmar que el job termine en verde con el paso de SonarCloud incluido — eso todavía no ha pasado y por eso esta entrega no lo presenta como terminado.

Si al revisar el proyecto en SonarCloud la organización o la clave no coinciden con `spalacioc05` / `spalacioc05_calculadora-gradle` (los valores que dejamos por convención en `build.gradle`), también habría que ajustar esas dos propiedades — pero según la API pública, los nombres sí coinciden, así que no debería hacer falta.

## 8. Resumen para la sustentación

El procesador de notas ya tenía pruebas unitarias; nosotros le agregamos medición de cobertura real con JaCoCo, encontramos y corregimos un bug genuino (`NaN`) y varios huecos de prueba usando el propio reporte de cobertura como guía, bajamos la complejidad del método principal separándolo en validaciones más pequeñas, le sumamos pruebas de aceptación con Karate, y automatizamos todo con GitHub Actions — que terminó verde después de tres intentos reales (sintaxis del workflow, versión de Java del escáner, y un paso de configuración en sonarcloud.io). El análisis de SonarCloud por CI, con la cobertura de JaCoCo integrada, todavía no se ha completado: lo único verificado de SonarCloud es un análisis automático viejo, de antes de nuestros cambios, que ya muestra 2 code smells menores y cero bugs. Falta un último clic en sonarcloud.io (apagar Automatic Analysis) para que el análisis real corra y se pueda mostrar completo.
