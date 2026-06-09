# Justificación Teórica de la Cantidad y Responsabilidad de Hilos

Este documento detalla la justificación del diseño y asignación de hilos del programa, aplicando la metodología formal del paper de la cátedra: **"Algoritmos para determinar cantidad y responsabilidad de hilos en sistemas embebidos modelados con Redes de Petri S³PR"** (Luis Orlando Ventre, Orlando Micolini).

---

## 1. Estructura y Propiedades del Modelo

Para determinar los hilos, primero partimos de los **Invariantes de Transición (T-Invariants)** calculados mediante el análisis estructural de la Red de Petri en PIPE. Cada invariante representa un ciclo de vida completo de procesamiento de una transacción en el sistema (arribo, procesamiento por un flujo y salida/liquidación):

*   **$T_{inv1}$ (Flujo Tarjetas):** $\{T_0, T_1, T_2, T_3, T_9\}$
*   **$T_{inv2}$ (Flujo Alto Riesgo):** $\{T_0, T_4, T_5, T_9\}$
*   **$T_{inv3}$ (Flujo Transferencias):** $\{T_0, T_6, T_7, T_8, T_9\}$

---

## 2. Aplicación del Algoritmo de Segmentación de Responsabilidades (Sección 4.2)

El algoritmo divide la responsabilidad de ejecución de las transiciones de los invariantes en segmentos independientes, basándose en la presencia de bifurcaciones (forks) y confluencias (joins) en la red:

### A. Segmento Previo al Conflicto / Fork
La plaza $P_1$ representa un conflicto estructural (fork). Las transiciones posteriores $T_1$, $T_4$ y $T_6$ compiten por consumir el token depositado en esta plaza.
*   **Aplicación:** El Caso 2 del algoritmo indica que las transiciones previas al conflicto se agrupan en un segmento separado.
*   **Segmento Resultante:** Segmento de Entrada ($S_{entrada}$) responsable de la transición **$\{T_0\}$**.

### B. Segmentos Post-Conflicto (Flujos de Procesamiento)
Tras la bifurcación, existen 3 caminos lógicos de ejecución lineal que no comparten transiciones entre sí hasta la salida.
*   **Aplicación:** El Caso 1 del algoritmo indica que cada camino lineal independiente conforma un segmento propio.
*   **Segmentos Resultantes:**
    1.  **Segmento Tarjetas ($S_{tarjetas}$):** Responsable de las transiciones **$\{T_1, T_2, T_3\}$** (plazas de acción $P_2, P_3$).
    2.  **Segmento Alto Riesgo ($S_{altoRiesgo}$):** Responsable de las transiciones **$\{T_4, T_5\}$** (plaza de acción $P_4$).
    3.  **Segmento Transferencia ($S_{transferencias}$):** Responsable de las transiciones **$\{T_6, T_7, T_8\}$** (plazas de acción $P_5, P_6$).

### C. Segmento Posterior a la Unión / Join
Los tres caminos lógicos confluyen (join) en la plaza de salida $P_9$ (buffer de transacciones procesadas) antes de reinyectar el token al inicio del ciclo mediante la transición de liquidación $T_9$.
*   **Aplicación:** El Caso 3 del algoritmo indica que tras la confluencia de múltiples invariantes de transición, se debe segmentar la responsabilidad de la transición de salida común.
*   **Segmento Resultante:** Segmento de Salida ($S_{salida}$) responsable de la transición **$\{T_9\}$**.

---

## 3. Algoritmo de Determinación de Hilos Máximos por Segmento (Sección 4.3)

Para cada segmento $S_i$, se calcula el marcado máximo simultáneo ($Max(MS_i)$) en su conjunto de plazas de acción asociadas. Este valor determina cuántos hilos concurrentes máximos requiere cada segmento para evitar cuellos de botella:

1.  **Segmento Entrada ($S_{entrada}$):**
    *   *Plaza de acción:* $P_1$ (Admisión).
    *   *Marcado Máximo:* $Max(M(P_1)) = 1$.
    *   **Hilos requeridos: 1**

2.  **Segmento Tarjetas ($S_{tarjetas}$):**
    *   *Plazas de acción:* $\{P_2, P_3\}$ (Autorización y Captura).
    *   *Marcado Máximo:* Acotado por el invariante de plazas $M(P_2) + M(P_3) + M(P_4) + M(P_7) = 1$. Por ende, $Max(M(P_2) + M(P_3)) = 1$.
    *   **Hilos requeridos: 1**

3.  **Segmento Alto Riesgo ($S_{altoRiesgo}$):**
    *   *Plaza de acción:* $\{P_4\}$ (Scoring Antifraude).
    *   *Marcado Máximo:* Acotado por el invariante de plazas anterior a $Max(M(P_4)) = 1$.
    *   **Hilos requeridos: 1**

4.  **Segmento Transferencia ($S_{transferencias}$):**
    *   *Plazas de acción:* $\{P_5, P_6\}$ (Validación y Ejecución).
    *   *Marcado Máximo:* Acotado por el invariante de plazas $M(P_4) + M(P_5) + M(P_6) + M(P_8) = 1$. Por ende, $Max(M(P_5) + M(P_6)) = 1$.
    *   **Hilos requeridos: 1**

5.  **Segmento Salida ($S_{salida}$):**
    *   *Plaza de acción:* $\{P_9\}$ (Liquidación).
    *   *Marcado Máximo:* $Max(M(P_9)) = 1$ (se procesa y vacía de forma inmediata para reiniciar el ciclo en $P_0$).
    *   **Hilos requeridos: 1**

---

## 4. Cálculo Final de Hilos del Sistema

El algoritmo establece que la cantidad máxima de hilos necesarios para la ejecución del sistema con el mayor paralelismo posible es la suma de los hilos de todos los segmentos:

$$\text{Hilos Totales} = \text{Hilos}(S_{entrada}) + \text{Hilos}(S_{tarjetas}) + \text{Hilos}(S_{altoRiesgo}) + \text{Hilos}(S_{transferencias}) + \text{Hilos}(S_{salida})$$
$$\text{Hilos Totales} = 1 + 1 + 1 + 1 + 1 = \mathbf{5\text{ hilos}}$$

---

## 5. Mapeo a las Clases en Código (Implementación)

Esta distribución matemática de responsabilidades de ejecución y cantidad de hilos se mapea directamente al código de la clase [Main.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/Main.java) en donde se lanzan exactamente estos 5 trabajadores:

*   **1 Hilo Generador (Entrada):** Instancia de [HiloGenerador.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/HiloGenerador.java) asignado al segmento $S_{entrada}$ (ejecuta transition `0`).
*   **1 Hilo Tarjetas:** Instancia de [HiloProcesadorTarjetas.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/HiloProcesadorTarjetas.java) asignado al segmento $S_{tarjetas}$ (ejecuta transitions `1, 2, 3`).
*   **1 Hilo Alto Riesgo:** Instancia de [HiloProcesadorAltoRiesgo.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/HiloProcesadorAltoRiesgo.java) asignado al segmento $S_{altoRiesgo}$ (ejecuta transitions `4, 5`).
*   **1 Hilo Transferencias:** Instancia de [HiloProcesadorTransferencias.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/HiloProcesadorTransferencias.java) asignado al segmento $S_{transferencias}$ (ejecuta transitions `6, 7, 8`).
*   **1 Hilo Salida:** Instancia de [HiloGenerador.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/HiloGenerador.java) asignado al segmento $S_{salida}$ (ejecuta transition `9`).
