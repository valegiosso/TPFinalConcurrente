# Checklist TP Final — Programación Concurrente 2026

Checklist basado en el [Enunciado](file:///d:/facultad/concurrente/TPFinalConcurrente/Enunciado%20TP%20Final%20Concurrente%202026.md). Marcar con `[x]` lo completado, `[/]` lo en progreso y `[ ]` lo pendiente.

---

## 1. Propiedades de la Red (PIPE)

- [x] Modelar la red en PIPE → Archivo [RedDePetri.xml](file:///d:/facultad/concurrente/TPFinalConcurrente/RedDePetri.xml) presente
- [x] Imagen de la red → [RedPetri.png](file:///d:/facultad/concurrente/TPFinalConcurrente/RedPetri.png) presente
- [ ] Determinar con PIPE: **Deadlock** (¿la red es libre de deadlocks?)
- [ ] Determinar con PIPE: **Vivacidad** (¿todas las transiciones son L1-vivas?)
- [ ] Determinar con PIPE: **Seguridad / Acotamiento** (¿es bounded?)
- [x] Identificar **Invariantes de Plaza** (P-Invariants) → Documentados en [justificacion_hilos.md](file:///d:/facultad/concurrente/TPFinalConcurrente/justificacion_hilos.md) y verificados en código en [VectorDeEstado.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/VectorDeEstado.java)
  - [x] $M(P_2) + M(P_3) + M(P_4) + M(P_7) = 1$
  - [x] $M(P_4) + M(P_5) + M(P_6) + M(P_8) = 1$
  - [x] $M(P_0) + M(P_1) + M(P_2) + M(P_3) + M(P_4) + M(P_5) + M(P_6) + M(P_9) = 3$
- [x] Identificar **Invariantes de Transición** (T-Invariants)
  - [x] $T_{inv1}$: {T0, T1, T2, T3, T9} (Tarjetas)
  - [x] $T_{inv2}$: {T0, T4, T5, T9} (Alto Riesgo)
  - [x] $T_{inv3}$: {T0, T6, T7, T8, T9} (Transferencias)
- [ ] Breve descripción de qué representan los invariantes en el modelo (para el informe)

---

## 2. Implementación del Monitor

### 2.1 Tablas del Sistema
- [ ] Tabla con los **estados del sistema** (marcados posibles / significado de cada plaza)
- [ ] Tabla con los **eventos del sistema** (significado de cada transición)

### 2.2 Hilos
- [x] Determinar la **cantidad de hilos** necesarios → 5 hilos justificados en [justificacion_hilos.md](file:///d:/facultad/concurrente/TPFinalConcurrente/justificacion_hilos.md)
  - [x] **Caso 1 (Conflicto/Fork):** Hilo antes del conflicto en $P_1$ + hilo por cada invariante post-conflicto
  - [x] **Caso 2 (Join):** Hilo para las transiciones post-join ($T_9$)
- [ ] Gráfico con las responsabilidades de cada hilo **con colores** (como la Figura 2 del enunciado)
  - [ ] Imagen de la red con flechas coloreadas mostrando qué hilo dispara qué transiciones

### 2.3 Interfaz y Monitor Agnóstico
- [x] Interfaz `MonitorInterface` con `boolean fireTransition(int transition)` → [MonitorInterface.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/MonitorInterface.java)
- [x] `fireTransition` es el **único método público** del Monitor → [Monitor.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/Monitor.java)
- [x] Monitor **agnóstico** a la red (sin referencias a transiciones puntuales) → Verificado, no hay `if (transition == X)` en Monitor.java

---

## 3. Semántica Temporal

- [x] Transiciones temporales implementadas: {T2, T3, T5, T7, T8} → [SensibilizadoConTiempo.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/SensibilizadoConTiempo.java)
- [x] Tiempos asignados en milisegundos en [Main.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/Main.java)
  - [x] T2: 100ms, T3: 100ms, T5: 150ms, T7: 120ms, T8: 120ms
- [x] Sleep fuera del monitor (no bloquea al sistema) → Implementado en Monitor.java (libera mutex antes de dormir)
- [ ] **Análisis temporal analítico** (cálculo teórico de cuánto debería tardar)
- [ ] **Análisis temporal práctico** (resultados de múltiples ejecuciones)
- [ ] **Variar los tiempos** asignados y analizar los cambios en los resultados
- [ ] **Obtener conclusiones** del análisis temporal

---

## 4. Políticas de Resolución de Conflictos

- [x] **Política Aleatoria** implementada → [PoliticaAleatoria.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/PoliticaAleatoria.java)
- [x] **Política Priorizada** implementada (prioriza alto riesgo T4, T5) → [PoliticaPriorizada.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/PoliticaPriorizada.java)
- [ ] Ejecuciones y análisis con **política aleatoria** (resultados guardados)
- [ ] Ejecuciones y análisis con **política priorizada** (resultados guardados)
- [ ] Comparación entre ambas políticas con conclusiones

---

## 5. Requerimientos Específicos (1 al 13)

### Req. 1: Proyecto Java con Monitor de Concurrencia
- [x] Código en Java con monitor de concurrencia
- [x] Sin librerías externas (solo `java.util.concurrent`)
- [ ] Verificar que corre en **cualquier máquina** sin configuración adicional (sin dependencias de IDE)

### Req. 2: Clase Main
- [x] Clase [Main.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/Main.java) presente y funcional

### Req. 3: Finalización limpia
- [x] El programa finaliza (no quedan hilos activos) → `despertarATodosYSalir()` en Monitor + `join()` en Main

### Req. 4: Objeto Política
- [x] Interfaz [Politica.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/Politica.java) + 2 implementaciones

### Req. 5: Diagrama de Clases
- [/] Diagrama en [DiagramdeClases.md](file:///d:/facultad/concurrente/TPFinalConcurrente/DiagramdeClases.md) (en formato Mermaid)
- [ ] **Exportar como imagen** en buena calidad (archivo de imagen `.png` o `.svg`)

### Req. 6: Diagrama de Secuencia
- [/] Diagrama en [DiagramaSecuencia.md](file:///d:/facultad/concurrente/TPFinalConcurrente/DiagramaSecuencia.md) (en formato Mermaid, recientemente mejorado)
  - [x] Muestra el flujo de `fireTransition(t)`
  - [x] Muestra la adquisición y liberación del mutex
  - [x] Muestra la consulta a `RdP.disparar()`
  - [x] Muestra el uso de la **Política** (`decidirTransicion()`)
  - [x] Muestra el *Passing the Baton* (`colas.release` sin liberar mutex)
  - [x] Muestra el caso de bloqueo en cola cuando la transición no está habilitada
  - [x] Muestra el manejo de transiciones temporales (sleep fuera del monitor)
- [ ] **Exportar como imagen** en buena calidad (archivo de imagen `.png` o `.svg`)

### Req. 7: Justificación de cantidad de hilos
- [x] Documentada en [justificacion_hilos.md](file:///d:/facultad/concurrente/TPFinalConcurrente/justificacion_hilos.md) con referencia al paper de Ventre y Micolini

### Req. 8: Múltiples ejecuciones con 200 invariantes
- [x] El sistema está configurado para 200 invariantes (`MAX_INVARIANTES = 200` en Main.java)
- [ ] Realizar **múltiples ejecuciones** y guardar resultados
  - [ ] a) Demostrar cumplimiento de políticas en la distribución de carga
  - [ ] b) Mostrar la cantidad de cada tipo de invariante y justificar

### Req. 9: Archivo de log
- [x] Logger implementado → [Logger.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/Logger.java) escribe `log_disparos.txt`

### Req. 10: Análisis de tiempos (20-40 seg)
- [ ] Verificar que el programa demora **entre 20 y 40 segundos**
- [ ] Documentar tiempos de ejecución de múltiples corridas

### Req. 11: Mostrar e interpretar invariantes
- [x] P-Invariantes identificados y verificados en código
- [x] T-Invariantes identificados
- [ ] **Interpretar** en el informe qué significa cada invariante en el modelo de negocio

### Req. 12: Verificar P-Invariantes tras cada disparo
- [x] Implementado en [VectorDeEstado.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/VectorDeEstado.java) → `verificarInvariantePlazas()` llamado desde Monitor tras cada disparo exitoso

### Req. 13: Verificar T-Invariantes con Regex
- [x] Script [regex.py](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/regex.py) implementado
  - [x] Patrón Tarjetas: `^(T1T2T3)+$`
  - [x] Patrón Alto Riesgo: `^(T4T5)+$`
  - [x] Patrón Transferencias: `^(T6T7T8)+$`
  - [x] Verificación de contadores globales ($T_0 == T_9$)

---

## 6. Entregables

- [ ] a) Imagen del **diagrama de clases** en buena calidad
- [ ] b) Imagen del **diagrama de secuencia** en buena calidad
- [x] c) **Código fuente** Java del proyecto
- [ ] d) **Informe obligatorio** que documente:
  - [ ] Lo realizado
  - [ ] Explicación del código
  - [ ] Criterios adoptados
  - [ ] Resultados obtenidos

---

## Resumen Rápido

| Categoría | Completado | En progreso | Pendiente |
|---|:---:|:---:|:---:|
| Propiedades de la Red (PIPE) | 6 | 0 | 4 |
| Monitor e Hilos | 6 | 0 | 3 |
| Semántica Temporal | 4 | 0 | 4 |
| Políticas | 2 | 0 | 3 |
| Requerimientos (1-13) | 11 | 2 | 8 |
| Entregables | 1 | 0 | 3 |
| **TOTAL** | **30** | **2** | **25** |
