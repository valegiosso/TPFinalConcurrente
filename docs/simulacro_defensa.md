# Simulacro de Examen y Defensa Oral — Programación Concurrente 2026

Este documento contiene un banco de posibles preguntas y respuestas preparadas específicamente para la defensa de este proyecto. Las respuestas están redactadas de forma conceptual y directa, facilitando su explicación verbal y fluida frente a los profesores sin caer en excesos de tecnicismos matemáticos innecesarios.

---

## 📌 Bloque 1: Red de Petri, Matrices e Invariantes

### P1: ¿Qué representan las plazas en su Red de Petri y cuál es la capacidad del sistema?
**Respuesta hablada:**  
Las plazas de nuestra red modelan tanto los **recursos del sistema** (como canales de autorización de tarjetas o validación de transferencias) como las **etapas del ciclo de vida** de una transacción de pago (admisión, procesamiento interno y liquidación).  
La capacidad máxima del sistema está determinada por el marcado inicial de la plaza $P_0$, que tiene **3 tokens**. Esto, según el P-invariante correspondiente, limita a un máximo de **3 transacciones simultáneas activas** en todo el sistema. Es decir, no puede haber más de 3 operaciones en tránsito (sea ingresando, procesándose o esperando a ser liquidadas) al mismo tiempo.

---

### P2: ¿Qué son los Invariantes de Transición (T-Invariantes) en su red y qué representan físicamente en el negocio?
**Respuesta hablada:**  
Los T-Invariantes son secuencias de disparos de transiciones cíclicas que devuelven a la Red de Petri a su marcado inicial. Físicamente, en nuestro proyecto de pasarela de pagos, representan **los tres flujos de negocio principales** del sistema bancario:
1. **Flujo de Tarjetas ($T_{inv1}$):** Admisión $\rightarrow$ Tarjetas $\rightarrow$ Autorización $\rightarrow$ Captura $\rightarrow$ Liquidación ($T_0 \rightarrow T_1 \rightarrow T_2 \rightarrow T_3 \rightarrow T_9$).
2. **Flujo de Alto Riesgo ($T_{inv2}$):** Admisión $\rightarrow$ Alto Riesgo $\rightarrow$ Scoring Antifraude $\rightarrow$ Liquidación ($T_0 \rightarrow T_4 \rightarrow T_5 \rightarrow T_9$).
3. **Flujo de Transferencias ($T_{inv3}$):** Admisión $\rightarrow$ Transferencias $\rightarrow$ Validación $\rightarrow$ Ejecución $\rightarrow$ Liquidación ($T_0 \rightarrow T_6 \rightarrow T_7 \rightarrow T_8 \rightarrow T_9$).

Al estar la red completamente cubierta por estos T-invariantes, garantizamos que el sistema es cíclico y potencialmente vivaz (no se queda trabado definitivamente en un estado final de no retorno).

---

### P3: ¿Para qué sirven los Invariantes de Plaza (P-Invariantes) y qué propiedad física demuestran en su código?
**Respuesta hablada:**  
Los P-Invariantes son ecuaciones de conservación que demuestran que la suma de tokens en determinados conjuntos de plazas permanece constante durante toda la simulación. En nuestro código, nos sirven para **garantizar que la red está acotada** (no se acumulan infinitamente tokens en ninguna plaza) y para **garantizar la exclusión mutua de los flujos**.  
Por ejemplo, el P-invariante del flujo de Tarjetas es:  
$$M(P_7) + M(P_2) + M(P_3) + M(P_4) = 1$$  
Como la suma de tokens nunca puede superar 1, demostramos matemáticamente que **nunca puede haber más de una transacción en vuelo dentro del flujo de tarjetas**. Esto hace que la red sea segura (*1-bounded*) en sus caminos críticos, imposibilitando condiciones de carrera dentro del mismo flujo.

---

## 📌 Bloque 2: Diseño de Hilos y Concurrencia en Java

### P4: ¿Por qué decidieron implementar la interfaz `Runnable` en lugar de heredar de la clase `Thread`?
**Respuesta hablada:**  
Decidimos implementar `Runnable` en nuestra clase [HiloBase.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/HiloBase.java) por dos razones principales de diseño de Java:
1. **Desacoplamiento:** Separa la **tarea lógica** que debe ejecutarse (las transiciones que tiene asignadas el hilo) de la **infraestructura de ejecución** que provee la clase `Thread`. Esto nos permitió crear una única clase genérica `HiloBase` y luego instanciarla en hilos con diferentes responsabilidades en [Main.java](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/src/monitor/Main.java).
2. **Herencia Simple:** Como Java no permite la herencia múltiple, si heredáramos de `Thread` bloquearíamos la posibilidad de que nuestro hilo herede de alguna otra clase base en el futuro. Implementar `Runnable` mantiene nuestro modelo de clases flexible y limpio.

---

### P5: ¿Cómo decidieron la cantidad de hilos y qué transiciones dispara cada uno?
**Respuesta hablada:**  
Aplicamos el algoritmo formal del paper de la cátedra (*"Algoritmos para determinar cantidad y responsabilidad de hilos..."*). El algoritmo prescribe separar responsabilidades basándose en los conflictos (bifurcaciones) y las confluencias (uniones) de la red:
- **HiloGenerador:** Se encarga de la admisión (transición $T_0$), previo al conflicto de flujos.
- **HiloTarjetas:** Ejecuta secuencialmente el flujo interno de tarjetas ($T_1, T_2, T_3$).
- **HiloAltoRiesgo:** Ejecuta secuencialmente el flujo de alto riesgo ($T_4, T_5$).
- **HiloTransferencias:** Ejecuta secuencialmente el flujo de transferencias ($T_6, T_7, T_8$).
- **HiloSalida:** Se encarga de la liquidación final (transición $T_9$), posterior a la confluencia.

Esto da un total de **5 hilos activos** que representan el paralelismo óptimo del sistema, evitando cuellos de botella y minimizando cambios de contexto innecesarios.

---

## 📌 Bloque 3: Sincronización, Monitor y "Passing the Baton"

### P6: ¿Qué es "Passing the Baton" (Paso del Testigo) y cómo lo implementaron en su Monitor?
**Respuesta hablada:**  
*Passing the Baton* es una técnica de sincronización para monitores concurrentes basada en semáforos. En lugar de usar los mecanismos tradicionales de Java como `notifyAll()`, el monitor gestiona la exclusión mutua con un semáforo principal (`mutex`) y las esperas con un arreglo de semáforos individuales para cada transición (`colas`).  
El concepto clave es que el control del monitor se trata como un **testigo** (o baton):
1. Cuando un hilo entra y hace un disparo exitoso, evalúa qué transiciones con hilos esperando en cola quedaron sensibilizadas por marcado.
2. Consulta a la `Politica` y elige una.
3. Si hay una elegida, hace un `release()` sobre la cola de esa transición específica y **retorna del monitor inmediatamente sin liberar el mutex principal** (`mutex.release()`).
4. De esta manera, el hilo que estaba dormido se despierta y **hereda el testigo del mutex de forma directa**, garantizando que continúe ejecutando dentro del monitor sin que otros hilos puedan interferir.

---

### P7: En el bucle de su Monitor, hay una variable boolean `k`. ¿Qué representa y cómo funciona?
**Respuesta hablada:**  
La variable `k` es la que controla el bucle de ejecución dentro del monitor para el hilo actual. Su funcionamiento es el siguiente:
- Se inicializa en `true`. El hilo entra al bucle e intenta disparar su transición.
- **Si el disparo es exitoso (`k = true`):** El hilo actual intenta despertar a otro hilo que esté esperando en una transición sensibilizada. Si lo encuentra, le cede el testigo (hace `colas.release()`) y sale del bucle y del monitor (retornando `true`). Si no hay nadie esperando que pueda disparar, el hilo pone `k = false`, sale del bucle, libera el mutex con `mutex.release()` y se retira.
- **Si el disparo falla (`k = false`):** El hilo libera el mutex (`mutex.release()`), se bloquea en la cola de su transición (`colas.acquire()`) y sale del bucle temporalmente. Cuando otro hilo lo despierte, el hilo despertado recupera la ejecución **heredando el mutex**, pone `k = true` y vuelve a iterar el bucle para intentar disparar su transición ahora que sabe que está sensibilizada.

---

### P8: ¿Por qué no utilizaron `synchronized` o `notifyAll()` nativos de Java para este proyecto?
**Respuesta hablada:**  
No los usamos por dos razones fundamentales de eficiencia y control:
1. **Evitar el "Thundering Herd" (Alboroto de Hilos):** `notifyAll()` despierta a **todos** los hilos que están esperando en el monitor. Al despertarse todos a la vez, se produce una competencia feroz por el lock, pero solo uno logrará entrar y los demás volverán a dormirse. Esto genera un desperdicio enorme de recursos de CPU debido a los cambios de contexto.
2. **Falta de Selectividad:** Los monitores nativos de Java no permiten elegir a qué hilo específico despertar en base al estado de la Red de Petri. Con *Passing the Baton* y semáforos individuales en cada cola de transición, podemos despertar **únicamente y con precisión quirúrgica** al hilo que está esperando en la transición que acaba de quedar sensibilizada por el marcado.

---

## 📌 Bloque 4: Semántica Temporal y el Flag `esperando`

### P9: ¿Cómo manejan el tiempo en el monitor? ¿Por qué duermen a los hilos fuera del monitor en lugar de adentro?
**Respuesta hablada:**  
Cuando un hilo intenta disparar una transición temporizada (como la autorización o validación) y el monitor detecta que está sensibilizada por marcado pero aún no se abrió su ventana temporal ($\text{ahora} - \text{timeStamp} < \alpha$), el monitor calcula cuántos milisegundos faltan para que se abra la ventana.  
Luego, **libera el mutex y duerme al hilo usando `Thread.sleep(espera)` fuera del monitor**.  
Hacemos esto obligatoriamente fuera del monitor porque si durmiéramos al hilo adentro sosteniendo el `mutex`, bloquearíamos la exclusión mutua de todo el sistema. Ningún otro hilo podría entrar al monitor, provocando un congelamiento total (deadlock) y deteniendo la simulación. Al despertar, el hilo simplemente vuelve a solicitar el `mutex` y reevalúa el disparo.

---

### P10: ¿Para qué sirve el flag `esperando` en las transiciones temporizadas y qué problema evita?
**Respuesta hablada:**  
El flag `esperando` evita el **"robo de transiciones"** y la inanición (*starvation*).  
Cuando un hilo detecta que debe esperar por tiempo, pone el flag `esperando` de esa transición en `true`, libera el mutex y se va a dormir afuera del monitor. Mientras ese hilo está durmiendo, la ventana temporal podría abrirse. Si en ese intervalo entra al monitor otro hilo rápido del mismo segmento e intenta disparar la misma transición, el monitor lee que `esperando == true` y rechaza el disparo de este nuevo hilo (retornando `false` en `estaSensibilizado()`), obligándolo a ir a la cola de espera.  
Esto asegura que el hilo que se tomó el trabajo de dormir y esperar su tiempo sea el que efectivamente dispare la transición cuando despierte, respetando el orden y la justicia del sistema.

---

### P11: En el informe dice que la ventana temporal superior ($\beta$) es de 10.000 ms, pero en el código Java usan `Long.MAX_VALUE`. ¿Por qué?
**Respuesta hablada:**  
En el modelo teórico de la Red de Petri, definimos un límite superior $\beta = 10000\text{ ms}$ para representar que las transacciones no pueden quedar colgadas indefinidamente.  
Sin embargo, en la implementación práctica sobre Java, configurar un timeout estricto de $\beta$ haría que las transacciones fallen si el planificador del sistema operativo pausa el hilo por sobrecarga de la CPU (lo que retrasaría el disparo más allá de los 10 segundos de forma involuntaria). Para hacer el software robusto ante la latencia del sistema operativo y evitar excepciones espurias durante la simulación, decidimos utilizar `Long.MAX_VALUE` (equivalente a infinito) como límite superior en el código.

---

## 📌 Bloque 5: Autómatas, Regex y Verificación de Correctitud

### P12: Formalmente, ¿cómo se mapea la Red de Petri del TP Final a un Autómata Finito $A = (E, S, Q, f, g)$?
**Respuesta hablada:**  
El mapeo formal que implementamos se estructura de la siguiente manera:
- **$E$ (Vocabulario de entrada):** El alfabeto son las transiciones de la red $\{T_0, T_1, ..., T_9\}$. Cada disparo de una transición es la lectura de un símbolo del alfabeto.
- **$Q$ (Conjunto de estados):** Los estados corresponden a los vectores de marcado posibles de la red ($M = [P_0, ..., P_9]$). El estado inicial $q_0$ es el marcado inicial $M_0 = [3, 0, 0, 0, 0, 0, 0, 1, 1, 0]$.
- **$f$ (Función de transición):** Es la ecuación matricial de estado $M_{nuevo} = M + W \cdot \vec{v}$, que implementamos en el método `rdp.disparar()`.
- **$g$ (Función de salida) y $S$ (Vocabulario de salida):** Produce `VALIDO` si la secuencia de disparos completa los 200 ciclos de invariantes sin residuos en el log, o `INVALIDO` si hay un error en el orden de disparos detectado por el validador.

---

### P13: ¿Por qué el lenguaje de ejecuciones de su red es un Lenguaje Regular de Tipo 3?
**Respuesta hablada:**  
Es Tipo 3 (Regular) porque la secuencia de disparos que conforma cada uno de los tres T-Invariantes se puede generar mediante una **gramática lineal a derecha**.  
Las reglas de producción de la gramática tienen la forma $A \rightarrow aB$ o $A \rightarrow a$ (donde las minúsculas son las transiciones y las mayúsculas son los estados intermedios del flujo). Al no existir recursión ni necesidad de anidamientos complejos (como paréntesis balanceados, que requerirían una memoria de pila), el lenguaje de la red no necesita un autómata de pila, sino que puede ser reconocido por un **Autómata Finito** simple o expresarse mediante una **Expresión Regular**.

---

### P14: ¿Cómo valida el script en Python la correctitud concurrente si el log tiene disparos entrelazados (interleaving)?
**Respuesta hablada:**  
Dado que múltiples hilos disparan transiciones en paralelo, el archivo `log_disparos.txt` registra las transiciones entrelazadas (por ejemplo, un paso de tarjeta intercalado con uno de transferencia).  
Para verificar la correctitud sin separar los flujos manualmente, usamos una única expresión regular central:  
```
(T0)(.*?)((T1)(.*?)(T2)(.*?)(T3)|(T4)(.*?)(T5)|(T6)(.*?)(T7)(.*?)(T8))(.*?)(T9)
```
- Los fragmentos `(.*?)` son cuantificadores **no codiciosos (lazy)** que absorben cualquier disparo de *otros* hilos que ocurra en el medio de un invariante activo.
- Usamos la función `re.subn()` en Python para buscar coincidencias completas de un invariante, contar a qué flujo pertenecía (mediante grupos de captura), y reemplazarlo en el texto conservando únicamente las cadenas capturadas por los grupos de interleaving.
- Este proceso de reducción se repite en un bucle hasta que no haya más coincidencias. Si al finalizar el log quedó **completamente vacío**, demostramos matemáticamente que el log se redujo a la palabra vacía $\varepsilon$ y que la ejecución concurrente es 100% correcta y respetó los invariantes.

---

### P15: Expliquen en detalle qué hace cada parte de esa expresión regular y cómo funciona el reemplazo.
**Respuesta hablada:**  
La expresión regular se compone de **18 grupos de captura** (definidos por paréntesis `()`) que se desglosan de la siguiente manera:

1. **`(T0)` (Grupo 1):** Es el disparo de admisión. Marca el inicio obligatorio de cualquiera de los tres invariantes de la red.
2. **`(.*?)` (Grupo 2):** Absorbe de forma no codiciosa (*lazy*) cualquier secuencia de disparos ajenos (interleaving) que ocurra entre la admisión y el inicio del procesamiento del flujo específico.
3. **El bloque central `((T1)(.*?)(T2)(.*?)(T3)|(T4)(.*?)(T5)|(T6)(.*?)(T7)(.*?)(T8))` (Grupo 3):** Es una **alternancia (u operador OR `|`)** que define las tres ramas de la red. El motor de regex elegirá exactamente una:
   - **Rama Tarjetas:** `(T1)(.*?)(T2)(.*?)(T3)` (Grupos 4 al 8). Evalúa el disparo de $T_1$, $T_2$ y $T_3$ con su respectivo interleaving intermedio.
   - **Rama Alto Riesgo:** `(T4)(.*?)(T5)` (Grupos 9 al 11). Evalúa el disparo de $T_4$ y $T_5$ con su interleaving intermedio.
   - **Rama Transferencias:** `(T6)(.*?)(T7)(.*?)(T8)` (Grupos 12 al 16). Evalúa el disparo de $T_6$, $T_7$ y $T_8$ con su interleaving intermedio.
4. **`(.*?)` (Grupo 17):** Absorbe el interleaving que pueda ocurrir entre la finalización del flujo ($T_3$, $T_5$ o $T_8$) y la liquidación.
5. **`(T9)` (Grupo 18):** Es el disparo de liquidación final. Marca el cierre obligatorio del invariante.

#### El mecanismo de reemplazo e interleaving:
Para ir reduciendo la cadena del log sin romper el entrelazamiento de los otros hilos, usamos la siguiente **cadena de reemplazo**:
```python
reemplazo = r"\g<2>\g<5>\g<7>\g<10>\g<13>\g<15>\g<17>"
```
Esto le indica a Python que, tras encontrar una coincidencia de un invariante completo, **conserve únicamente el contenido de los grupos de interleaving** (los "gaps" o huecos representados por `.*?`) y elimine los símbolos del invariante procesado ($T_0$, $T_1$, etc.). Al limpiar lo ya validado, la cadena se vuelve a juntar de forma compacta para que en la siguiente iteración del bucle se reconozcan los invariantes que antes estaban tapados o divididos por este.

---

## 📌 Bloque 6: Diagrama de Secuencia de Ejecución

### P16: Expliquen en detalle el flujo del diagrama de secuencia final (diagramasecunafinal.png) del monitor paso a paso.
**Respuesta hablada:**  
El diagrama de secuencia final ([diagramasecunafinal.png](file:///d:/facultad/concurrente/TPFinalConcurrente/diagramasecunafinal.png)) describe el ciclo completo del método `fireTransition(t)` sobre el Monitor de la Red de Petri. Se compone de 44 pasos ordenados en 7 fases lógicas:

#### 1. Entrada al Monitor (Exclusión Mutua)
*   **Paso 1 (`fireTransition(t)`)**: El `Hilo 1 (Activo)` intenta disparar la transición `t` llamando al monitor.
*   **Pasos 2 y 3 (`acquire()`)**: Solicita la exclusión mutua mediante el semáforo `mutex` y entra al monitor.
*   **Paso 4 (`k = true`)**: Se inicializa la variable local `k` en `true` para entrar al bucle `while (k)`.

#### 2. Bucle Principal y Evaluaciones
*   **Pasos 5 y 6 (`isFinalizada()`)**: Verifica si se completaron los 200 invariantes. Retorna `false`.
*   **Pasos 7 y 8 (`bloquearTransicion(t)`)**: Verifica si se alcanzó el límite de admisiones para detener el ingreso. Retorna `false`.
*   **Pasos 9 y 10 (`estaSensibilizadoPeroAntes(t)`)**: Compara el tiempo transcurrido desde la sensibilización. Si el tiempo actual está antes de $\alpha$, retorna `enVentana = true`.

#### 3. Bifurcación A: `enVentana == true` (Espera de ventana temporal)
*   Si hay que esperar por tiempo, el hilo duerme fuera del monitor:
    *   **Pasos 11 y 12 (`tiempoRestante(t)`)**: Calcula el tiempo de `espera` restante para abrir la ventana.
    *   **Paso 13 (`setEsperando(t, true)`)**: Marca la transición como en espera temporal en `redPetri`.
    *   **Pasos 14 y 15 (`release()`)**: Libera el `mutex` para que otros hilos puedan usar el monitor.
    *   **Paso 16 (`Thread.sleep(espera)`)**: Duerme fuera del monitor para no bloquear el sistema.
    *   **Pasos 17 y 18 (`acquire()`)**: Al despertar, vuelve a competir y adquiere el `mutex`.
    *   **Paso 19 (`setEsperando(t, false)`)**: Limpia el flag de espera y vuelve a evaluar el bucle `while`.

#### 4. Bifurcación B: `enVentana == false` (Transición lista para disparar)
*   **Pasos 20 y 21 (`disparar(t) : boolean`)**: Intenta disparar la transición en `redPetri`. Si el disparo tiene éxito por marcado, se actualiza el estado y devuelve `k = true`.

#### 5. Sub-bifurcación: `k == true` (Disparo Exitoso y Paso del Testigo)
*   **Pasos 22 y 23 (`isFinalizada()`)**: Verifica si la simulación terminó tras este disparo. Retorna `false`.
*   **Pasos 24 y 25 (`getSensibilizadasPorMarcado()`)**: Obtiene qué transiciones quedaron habilitadas por marcado.
*   **Pasos 26 y 27 (`quienesEstan()`)**: Obtiene en qué semáforos individuales de la clase `Colas` hay hilos dormidos.
*   **Paso 28 (`m = sensibilizadas AND conHilosEsperando`)**: Realiza la intersección lógica para determinar a quién se puede despertar.
    - **Caso 5.1: `m != vacio` (Despertar e irse):**
        *   **Pasos 29 y 30 (`decidirTransicion(m)`)**: La `politica` elige cuál despertar. Retorna la transición `seleccionada`.
        *   **Paso 31 y 32 (`release(seleccionada)`)**: Hace release del semáforo. El `Hilo 2 (Dormido en Cola)` se despierta.
        *   **Paso 33 (`return true`)**: El `Hilo 1` sale del monitor **sin liberar el mutex**. El `Hilo 2` hereda directamente la exclusión mutua (Traspaso de Testigo / Passing the Baton).
    - **Caso 5.2: `m == vacio` (Nadie a quien despertar):**
        *   **Paso 34 (`k = false`)**: Termina la condición para salir del bucle.

#### 6. Sub-bifurcación: `k == false` (Transición no sensibilizada por marcado)
*   Si no se puede disparar por falta de tokens, el hilo debe bloquearse:
    *   **Pasos 35 y 36 (`release()`)**: Libera el `mutex` del monitor para ceder el paso a otros hilos.
    *   **Pasos 37 y 38 (`acquire(t)`)**: El `Hilo 1` se encola en la cola de la transición `t` y se duerme. Más tarde, se despierta por traspaso de testigo heredando el mutex de entrada.
    *   **Pasos 39 y 40 (`isFinalizada()`)**: Al despertar, re-verifica la bandera de fin. Retorna `false`.
    *   **Paso 41 (`k = true`)**: Vuelve a habilitar el bucle para intentar disparar la transición.

#### 7. Salida Directa del Monitor (Bucle finalizado sin traspaso)
*   **Pasos 42 y 43 (`release()`)**: Si no hubo hilos a los que transferir el testigo (`k = false` en paso 34), el hilo actual libera la exclusión mutua de entrada del monitor.
*   **Paso 44 (`return true`)**: Retorna `true` saliendo ordenadamente del monitor.

---

## 📌 Bloque 7: Análisis Temporal — Deducción de los Valores de α

### P17: ¿Cómo dedujeron los valores de α (tiempo mínimo) de cada transición para que el programa dure entre 20 y 40 segundos?
**Respuesta hablada:**  
El proceso fue al revés: **partimos del tiempo total requerido y calculamos cuánto debe tardar como mínimo cada transición**.

#### 1. Punto de partida: paralelismo real de la red
Aunque $P_0$ tiene 3 tokens, los P-invariantes de las plazas de recurso limitan el paralelismo real a **2 flujos simultáneos como máximo**:
- `P7` (1 token) → Tarjetas **y** Alto Riesgo compiten por él.
- `P8` (1 token) → Transferencias **y** Alto Riesgo compiten por él.
- Alto Riesgo (`T4`) necesita **ambos** `P7` y `P8` al mismo tiempo → no puede correr con nadie más.

Por lo tanto, el único par que puede ejecutarse en verdadero paralelo es **Tarjetas + Transferencias**.

#### 2. La ecuación clave (cota mínima)
Con 2 flujos en paralelo procesando 200 invariantes, la cota mínima de ejecución es:

$$T_{min} = \left\lceil \frac{200}{2} \right\rceil \times \tau_{flujo\_lento} = 100 \times \tau_{flujo\_lento}$$

Para cumplir el requisito $T_{min} \geq 20 \text{ s}$:

$$100 \times \tau_{flujo\_lento} \geq 20.000 \text{ ms} \implies \tau_{flujo\_lento} \geq 200 \text{ ms}$$

#### 3. Distribución entre transiciones
El flujo más lento es **Transferencias**, que tiene 2 transiciones temporizadas (T7 y T8):

$$\alpha_{T7} + \alpha_{T8} \geq 200 \text{ ms}$$

Elegimos **120 ms + 120 ms = 240 ms** (un poco por encima del mínimo para absorber el overhead de la JVM y el planificador del SO).

Los demás valores se ajustaron de forma proporcional y coherente con la semántica del negocio:

| Transición | α elegido | Razonamiento de negocio |
|:---:|:---:|:---|
| **T2** (Autorización Tarjeta) | 100 ms | Consulta al issuer bancario, rápida |
| **T3** (Captura Tarjeta) | 100 ms | Confirmación local, rápida |
| **T5** (Antifraude Alto Riesgo) | 150 ms | Scoring ML más pesado, un solo paso |
| **T7** (Validación Transferencia) | 120 ms | Consulta COELSA, moderada |
| **T8** (Ejecución Transferencia) | 120 ms | Operación en red bancaria, moderada |

#### 4. Verificación de la cota máxima
El peor caso teórico (ejecución 100% serial por el camino más lento) sería:

$$T_{max}^{teo} = 200 \times 240 \text{ ms} = 48 \text{ s}$$

Esto supera los 40s del enunciado, pero representa un **extremo inalcanzable** en condiciones normales (requeriría que el paralelismo colapsara por completo). El tiempo real promedio medido fue de **~23.5 s**, perfectamente dentro del rango requerido.

#### 5. Resumen de la fórmula de diseño

```
α_flujo_lento ≈ tiempo_objetivo_min / (N / paralelismo_real)
             ≈ 20.000 ms / (200 / 2)
             ≈ 20.000 ms / 100
             ≈ 200 ms  →  se reparte en 120 ms + 120 ms para T7 y T8
```

> Los valores de los demás flujos (Tarjetas y Alto Riesgo) se eligieron proporcionalmente más bajos para que Transferencias sea el cuello de botella estructural del sistema, tal como lo predicen los P-invariantes.
