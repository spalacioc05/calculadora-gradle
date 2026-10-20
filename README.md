<div align="center">

# 🎓 Procesador de Notas Ponderadas

Proyecto académico para aplicar<br>
**pruebas unitarias, patrón AAA y pruebas básicas de rendimiento.**

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Karate](https://img.shields.io/badge/Karate-FF6E42?style=for-the-badge&logo=cucumber&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-E24329?style=for-the-badge&logoColor=white)

[![Calidad de software](https://github.com/spalacioc05/calculadora-gradle/actions/workflows/quality.yml/badge.svg)](https://github.com/spalacioc05/calculadora-gradle/actions/workflows/quality.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=spalacioc05_calculadora-gradle&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=spalacioc05_calculadora-gradle)

**Universidad de Antioquia**<br>
Calidad de Software

**Santiago Palacio Cárdenas · Andrés Felipe Bernal Molina**

</div>

## 🧮 ¿Qué hace el programa?

Recibe una lista de notas y otra con sus respectivos porcentajes. Luego suma el aporte de cada nota:

```text
Notas:       [4.0, 3.5, 4.5]
Porcentajes: [30,  30,  40]

4.0 × 30 % = 1.20
3.5 × 30 % = 1.05
4.5 × 40 % = 1.80
--------------------
Nota final  = 4.05
```

## 📋 Reglas del programa

| Regla | Condición |
|---|---|
| Rango de cada nota | `0.0 <= nota <= 5.0` |
| Rango de cada porcentaje | `0 <= porcentaje <= 100` |
| Cantidad de elementos | `cantidad de notas = cantidad de porcentajes` |
| Total de porcentajes | `suma de porcentajes = 100 %` |
| Listas | No pueden ser `null` ni estar vacías |

Cuando una regla no se cumple, el método lanza `IllegalArgumentException` con un mensaje sencillo.

## 🧪 Patrón AAA

| Parte | Propósito |
|---|---|
| **ARRANGE** | Preparamos los datos. |
| **ACT** | Ejecutamos el método. |
| **ASSERT** | Comprobamos el resultado. |

Ejemplo real del proyecto:

```java
// ARRANGE
double resultadoEsperado = 4.05;

// ACT
double resultadoObtenido = procesador.calcularNotaFinal(notas, porcentajes);

// ASSERT
assertEquals(resultadoEsperado, resultadoObtenido, TOLERANCIA);
```

> **Arrange prepara · Act ejecuta · Assert verifica**

## 🔄 Ciclo de vida de cada prueba

```text
@BeforeEach
     ↓
Preparar objetos
     ↓
   @Test
     ↓
ARRANGE → ACT → ASSERT
     ↓
@AfterEach
     ↓
Limpiar
```

- `@BeforeEach` crea el procesador y las listas antes de cada prueba.
- Arrange agrega los datos específicos del escenario. Por eso, `@BeforeEach` no reemplaza Arrange.
- `@AfterEach` limpia las listas después de cada prueba.

## ✅ Happy Path y ❌ Unhappy Path

### Happy Path

```text
Notas:       [4.0, 3.5, 4.5]
Porcentajes: [30, 30, 40]
Resultado:   4.05
```

Los datos son válidos y el cálculo termina normalmente.

### Unhappy Path

```text
Nota:      5.8
Resultado: IllegalArgumentException
```

Las pruebas también comprueban:

- una nota negativa;
- listas de diferente tamaño;
- porcentajes que no suman 100;
- listas vacías;
- una lista `null`;
- un porcentaje mayor que 100.

## ⏱️ Pruebas básicas de rendimiento

| Escenario | Cantidad | Tiempo máximo |
|---|---:|---:|
| Lista pequeña | 3 | 10 ms |
| Mayor volumen | 100 | 50 ms |
| Lista grande | 10.000 | 200 ms |

Los límites de tiempo son criterios definidos para este proyecto académico; no son estándares universales.

Los datos se preparan en Arrange antes de comenzar a medir. Así, las pruebas verifican:

1. que `calcularNotaFinal()` termine dentro del tiempo definido;
2. que el resultado siga siendo correcto.

La prueba de 10.000 elementos aumenta considerablemente el volumen de entrada, pero no representa una prueba completa de estrés de un sistema desplegado.

## 🛠️ Tecnologías

| Tecnología | Uso en el proyecto |
|---|---|
| **Java** | Lenguaje de programación utilizado para implementar el cálculo. |
| **JUnit 5** | Framework utilizado para escribir y ejecutar las pruebas unitarias. |
| **Karate** | Framework usado para las pruebas de aceptación sobre las reglas de negocio. |
| **Gradle** | Gestiona dependencias, compila el código y ejecuta las pruebas. |
| **Gradle Wrapper** | Permite usar la versión de Gradle configurada para el proyecto. |
| **JaCoCo** | Mide qué porcentaje del código ejecutan realmente las pruebas. |
| **SonarCloud** | Analiza el código en busca de bugs, code smells y complejidad. |
| **GitHub Actions** | Ejecuta build, pruebas, cobertura y análisis en cada push/PR. |

## 📁 Estructura

```text
src/
├── main/java/
│   └── ProcesadorNotas.java
│
└── test/
    ├── java/
    │   ├── ProcesadorNotasTest.java
    │   └── ProcesadorNotasAcceptanceTest.java   (runner de Karate)
    └── resources/karate/
        └── procesador-notas.feature

.github/workflows/quality.yml
docs/taller-analisis-estatico/
build.gradle
settings.gradle
README.md
gradlew.bat
```

## ▶️ Ejecución en Windows

Abrir PowerShell en la carpeta del proyecto y ejecutar:

```powershell
.\gradlew.bat clean test
```

Esto corre en el mismo paso las 20 pruebas JUnit 5 **y** los 5 escenarios de aceptación de Karate (están registrados como una prueba más dentro de `test`), y al terminar genera automáticamente el reporte de cobertura con JaCoCo.

`BUILD SUCCESSFUL` significa que Gradle completó correctamente las tareas solicitadas. No significa que el software no pueda tener otros defectos.

Para abrir el reporte HTML nativo de Gradle:

```powershell
start build\reports\tests\test\index.html
```

El reporte se genera en:

```text
build/reports/tests/test/index.html
```

Los nombres de las pruebas aparecen en español gracias a `@DisplayName`.

## 📊 Cobertura de pruebas con JaCoCo

```powershell
.\gradlew.bat clean test jacocoTestReport
start build\reports\jacoco\test\html\index.html
```

El reporte HTML muestra línea por línea qué se ejecutó durante las pruebas, y también se genera en XML (`build/reports/jacoco/test/jacocoTestReport.xml`) para que SonarCloud pueda leer la cobertura real.

## 🥋 Pruebas de aceptación con Karate

`ProcesadorNotas` es una clase Java normal, no un servicio web, así que las pruebas de aceptación usan la interoperabilidad Java de Karate (`Java.type('ProcesadorNotas')`) en vez de llamadas HTTP. Las reglas de negocio que se verifican están en [`src/test/resources/karate/procesador-notas.feature`](src/test/resources/karate/procesador-notas.feature):

- Cálculo correcto de una nota final ponderada.
- Rechazo de una nota fuera de 0.0–5.0.
- Rechazo de porcentajes que no suman 100.
- Rechazo de listas de distinto tamaño.
- Rechazo de listas vacías.

Corren junto con el resto de pruebas (`.\gradlew.bat clean test`) y el reporte queda en `build/test-results/test/` y `target/karate-reports/` (si se ejecuta Karate por separado).

## 🔎 Análisis estático con SonarCloud

El proyecto tiene el plugin `org.sonarqube` configurado en `build.gradle`, apuntando a `sonar.organization = spalacioc05` y `sonar.projectKey = spalacioc05_calculadora-gradle`. Para ejecutarlo localmente hace falta un token personal de SonarCloud:

```powershell
.\gradlew.bat clean test jacocoTestReport sonar -Dsonar.token=TU_TOKEN
```

En GitHub Actions el token se lee del secreto `SONAR_TOKEN` (ver [`docs/taller-analisis-estatico/`](docs/taller-analisis-estatico/) para el detalle de cómo se configuró y qué encontró).

## ⚙️ Integración continua

El workflow [`quality.yml`](.github/workflows/quality.yml) se ejecuta en cada *push* y *pull request* hacia `main`:

1. Compila el proyecto con Java 17.
2. Corre JUnit 5 y Karate.
3. Genera el reporte de cobertura JaCoCo.
4. Ejecuta el análisis de SonarCloud (si el secreto `SONAR_TOKEN` existe).
5. Publica como artefactos el reporte de pruebas, el reporte de cobertura y el `.jar` generado.

## 📚 Documentación del taller

El detalle completo del taller de análisis estático (estado inicial, hallazgos, correcciones, métricas y limitaciones) está en [`docs/taller-analisis-estatico/`](docs/taller-analisis-estatico/).
