Feature: Reglas de negocio del procesador de notas ponderadas

  # El procesador es una clase Java normal, no un servicio web, así que estas
  # pruebas de aceptación llaman directamente a ProcesadorNotas usando la
  # interoperabilidad con Java que ofrece Karate (Java.type). No se crea una
  # API REST porque no hace falta: el objetivo es validar reglas de negocio.

  Background:
    * def Arrays = Java.type('java.util.Arrays')
    * def ProcesadorNotas = Java.type('ProcesadorNotas')
    * def procesador = new ProcesadorNotas()

  Scenario: Calcula correctamente una nota final ponderada
    * def notas = Arrays.asList(4.0, 3.5, 4.5)
    * def porcentajes = Arrays.asList(30.0, 30.0, 40.0)
    * def resultado = procesador.calcularNotaFinal(notas, porcentajes)
    * match resultado == 4.05

  Scenario: Rechaza una nota fuera del rango permitido
    * def notas = Arrays.asList(4.0, 5.8, 3.0)
    * def porcentajes = Arrays.asList(30.0, 30.0, 40.0)
    * def huboError = false
    * eval
      """
      try {
        procesador.calcularNotaFinal(notas, porcentajes)
      } catch (e) {
        huboError = true
      }
      """
    * match huboError == true

  Scenario: Rechaza porcentajes que no suman cien
    * def notas = Arrays.asList(4.0, 3.5, 4.5)
    * def porcentajes = Arrays.asList(30.0, 30.0, 30.0)
    * def huboError = false
    * eval
      """
      try {
        procesador.calcularNotaFinal(notas, porcentajes)
      } catch (e) {
        huboError = true
      }
      """
    * match huboError == true

  Scenario: Rechaza una entrada incorrecta (listas de distinto tamaño)
    * def notas = Arrays.asList(4.0, 3.5, 4.5)
    * def porcentajes = Arrays.asList(50.0, 50.0)
    * def huboError = false
    * eval
      """
      try {
        procesador.calcularNotaFinal(notas, porcentajes)
      } catch (e) {
        huboError = true
      }
      """
    * match huboError == true

  Scenario: Rechaza una entrada incorrecta (lista vacía)
    * def notas = Arrays.asList()
    * def porcentajes = Arrays.asList()
    * def huboError = false
    * eval
      """
      try {
        procesador.calcularNotaFinal(notas, porcentajes)
      } catch (e) {
        huboError = true
      }
      """
    * match huboError == true
