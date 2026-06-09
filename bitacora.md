# Archivo para registar desiciones tomadas a lo largo del Trabjo practico
## Analisis pipe
### Matriz de incidence & marking
#### Petri net incidence and marking

**Forwards incidence matrix I+**

|   | T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| P0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| P1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P2 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P3 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P4 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |
| P5 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 |
| P6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 |
| P7 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 0 |
| P8 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 1 | 0 |
| P9 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 1 | 0 |

**Backwards incidence matrix I-**

|   | T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| P0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P1 | 0 | 1 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 0 |
| P2 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P3 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 |
| P4 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 |
| P5 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 |
| P6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 |
| P7 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |
| P8 | 0 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 0 |
| P9 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 |

**Combined incidence matrix I**

|   | T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| P0 | -1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| P1 | 1 | -1 | 0 | 0 | -1 | 0 | -1 | 0 | 0 | 0 |
| P2 | 0 | 1 | -1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P3 | 0 | 0 | 1 | -1 | 0 | 0 | 0 | 0 | 0 | 0 |
| P4 | 0 | 0 | 0 | 0 | 1 | -1 | 0 | 0 | 0 | 0 |
| P5 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | -1 | 0 | 0 |
| P6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | -1 | 0 |
| P7 | 0 | -1 | 0 | 1 | -1 | 1 | 0 | 0 | 0 | 0 |
| P8 | 0 | 0 | 0 | 0 | -1 | 1 | -1 | 0 | 1 | 0 |
| P9 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 1 | -1 |

**Marking**

|   | P0 | P1 | P2 | P3 | P4 | P5 | P6 | P7 | P8 | P9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| Initial | 3 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 1 | 0 |
| Current | 0 | 1 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 0 |

**Enabled transitions**

| T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|---|---|---|---|---|---|---|---|---|
| no | no | no | yes | no | no | no | no | yes | no |

#### Petri net invariant analysis results

##### T-Invariants

| T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 1 | 1 | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 1 |
| 1 | 0 | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 1 |
| 1 | 0 | 0 | 0 | 0 | 0 | 1 | 1 | 1 | 1 |

The net is covered by positive T-Invariants, therefore it might be bounded and live.

##### P-Invariants

| P0 | P1 | P2 | P3 | P4 | P5 | P6 | P7 | P8 | P9 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 0 | 0 | 1 | 1 | 1 | 0 | 0 | 1 | 0 | 0 |
| 0 | 0 | 0 | 0 | 1 | 1 | 1 | 0 | 1 | 0 |
| 1 | 1 | 1 | 1 | 1 | 1 | 1 | 0 | 0 | 1 |

The net is covered by positive P-Invariants, therefore it is bounded.

##### P-Invariant equations

M(P2) + M(P3) + M(P4) + M(P7) = 1  
M(P4) + M(P5) + M(P6) + M(P8) = 1  
M(P0) + M(P1) + M(P2) + M(P3) + M(P4) + M(P5) + M(P6) + M(P9) = 3  

Analysis time: 0.003

## Decisiones de Código y Refactorización Final
Fecha de actualización: 08/06/2026

### 1. Cambio Arquitectónico: De Locks a Semáforos ("Passing the Baton")
Para lograr un cumplimiento absoluto con el Enunciado del TP Final y garantizar que la ejecución siga de forma estricta la semántica del diagrama de secuencia, se reemplazó el uso de cerrojos y condiciones (`ReentrantLock`, `Condition[]`) por Semáforos:
- Se eliminó la clase obsoleta `Mutex.java`.
- Se implementó la clase `Colas.java` que gestiona un arreglo de semáforos (`Semaphore[]`), uno para cada transición de la red.
- El `Monitor.java` controla el acceso principal con un semáforo de exclusión mutua (`mutex = new Semaphore(1, true)`).
- Cuando una transición no está sensibilizada o requiere esperar en cola, el hilo libera el mutex y hace `acquire()` en su semáforo de transición correspondiente.
- Al disparar con éxito, el monitor decide mediante la clase `Politica` cuál hilo durmiente despertar y le hace `release()`, saliendo inmediatamente del monitor **sin liberar el mutex principal**, delegando directamente el testigo del lock al hilo despertado (semántica **Passing the Baton**).

### 2. Encapsulamiento del Monitor
Se eliminaron todos los métodos públicos del monitor que exponían el control del estado interno (como `stop()`, `isRunning()` y `getContadorInvariantes()`), dejando únicamente el método requerido por el enunciado:
- `boolean fireTransition(int transition)` es el único método público expuesto por `MonitorInterface` y `Monitor`.

### 3. Mecanismo de Finalización Limpia (Wakeup en Cascada)
Al no existir métodos públicos externos de control, para garantizar que todos los hilos durmientes finalicen limpiamente al alcanzar los 200 invariantes procesados (transición `T9` disparada 200 veces), se diseñó la finalización en cascada:
- Cuando el monitor detecta que `contadorInvariantes >= maxInvariantes`, llama a `despertarATodosYSalir()`.
- Este método hace `release()` de todas las colas de transiciones de forma simultánea y libera el `mutex` principal.
- Los hilos que despiertan comprueban la condición de fin inmediatamente dentro de `fireTransition()` y retornan `false`, saliendo de sus respectivos bucles de ejecución y terminando ordenadamente sin dejar hilos huérfanos.

### 4. Componentes y Estructura Actual del Código
El paquete `monitor` se compone de los siguientes elementos limpios:

| Clase/Interfaz | Responsabilidad |
| :--- | :--- |
| **MonitorInterface** | Interfaz del monitor con el método único `fireTransition(int)`. |
| **Monitor** | Orquestador concurrente agnóstico a la Red de Petri. Controla la exclusión mutua, las esperas de hilos, los invariantes dinámicos de plazas y el conteo de finalización. |
| **Colas** | Colección de semáforos (uno por transición) para la suspensión y reactivación de hilos. |
| **RdP** | Red de Petri genérica. Gestiona el marcado del sistema y ejecuta los disparos matemáticos ($M_{nuevo} = M_{actual} - Pre + Post$). |
| **Matrizi** | Clase auxiliar para operaciones y dimensiones de matrices. |
| **VectorDeEstado** | Representa el marcado y valida activamente que se cumplan las ecuaciones de invariantes de plaza tras cada disparo. |
| **VectorSensibilizadas** | Calcula las transiciones sensibilizadas y coordina las marcas de tiempo para transiciones temporizadas. |
| **SensibilizadoConTiempo** | Define el intervalo de tiempo $[\alpha, \beta]$ asociado a transiciones temporizadas. |
| **Politica** | Interfaz para la estrategia de resolución de conflictos. |
| **PoliticaAleatoria** | Resolución de conflictos seleccionando aleatoriamente entre transiciones sensibilizadas que tienen hilos esperando. |
| **PoliticaPriorizada** | Prioriza las transiciones del flujo de alto riesgo (`T4`, `T5`) frente a otras en conflicto. |
| **Logger** | Escribe de manera segura y sincronizada los disparos en `log_disparos.txt`. |
| **HiloBase** | Clase abstracta que sirve de base para los hilos de la aplicación. |
| **HiloGenerador** | Hilo encargado del flujo de admisión (`T0`) o salida (`T9`). |
| **HiloProcesadorTarjetas** | Hilo para el flujo secuencial `T1` $\rightarrow$ `T2` $\rightarrow$ `T3`. |
| **HiloProcesadorAltoRiesgo** | Hilo para el flujo secuencial `T4` $\rightarrow$ `T5`. |
| **HiloProcesadorTransferencias** | Hilo para el flujo secuencial `T6` $\rightarrow$ `T7` $\rightarrow$ `T8`. |
| **Main** | Define matrices y marcado inicial de la red, instancia el monitor con la política elegida, inicia los hilos y espera su finalización con `join()`. |

## ecuaciones
\begin{aligned}
& \text{\textbf{1. Marcado}} \\
& M = [m_{p_0}, m_{p_1}, m_{p_2}, \dots, m_{p_9}] \\
& M_0 = [1, 0, 0, 0, 0, 0, 0, 1, 1, 0] \\[10pt]

& \text{\textbf{2. Matriz de Incidencia}} \\
& I = I^- - I^+ \\
& I^+[t][p] = \text{tokens que la transición } t \text{ consume de la plaza } p \text{ (Pre)} \\
& I^-[t][p] = \text{tokens que la transición } t \text{ produce en la plaza } p \text{ (Post)} \\
& I[t][p] = I^-[t][p] - I^+[t][p] \\[10pt]

& \text{\textbf{3. Condición de Sensibilización}} \\
& \forall p \in P : M[p] - I^+[t][p] \geq 0 \\[10pt]

& \text{\textbf{4. Ecuación de Estado}} \\
& M' = M + I[t] = M + I^-[t] - I^+[t] \\[10pt]

& \text{\textbf{5. P-invariantes}} \\
& \text{Condición matemática: } \vec{y} \cdot I = \vec{y} \cdot (I^- - I^+) = \vec{0} \\
& \text{Propiedad en runtime: } \vec{y} \cdot M = \vec{y} \cdot M_0 = k \quad \forall M \text{ alcanzable} \\
& \text{Los tres P-invariantes de la red:} \\
& y_1 \cdot M = m_{P_0}+m_{P_1}+m_{P_2}+m_{P_3}+m_{P_4}+m_{P_5}+m_{P_6}+m_{P_9} = 1 \\
& y_2 \cdot M = m_{P_2}+m_{P_3}+m_{P_4}+m_{P_7} = 1 \\
& y_3 \cdot M = m_{P_4}+m_{P_5}+m_{P_6}+m_{P_8} = 1 \\[10pt]

& \text{\textbf{6. T-invariantes}} \\
& \text{Condición matemática: } I \cdot \vec{x} = (I^- - I^+) \cdot \vec{x} = \vec{0} \\
& \text{Los tres T-invariantes de la red:} \\
& x_1 = \{T_0, T_1, T_2, T_3, T_9\} \quad \text{(flujo tarjeta)} \\
& x_2 = \{T_0, T_4, T_5, T_9\} \quad \text{(flujo alto riesgo)} \\
& x_3 = \{T_0, T_6, T_7, T_8, T_9\} \quad \text{(flujo bancario)} \\[10pt]

& \text{\textbf{7. Semántica Temporal}} \\
& \text{Condición de sensibilización extendida:} \\
& sensibilizada(t) \iff \left(\forall p: M[p] - I^+[t][p] \geq 0\right) \land \left(t_{ahora} - t_{stamp}[t] \geq \alpha[t]\right) \\
& \text{Tiempo de espera (condición estructural se cumple, temporal no):} \\
& t_{espera} = \alpha[t] - (t_{ahora} - t_{stamp}[t]) \\
& \text{Condición para dormir el hilo:} \\
& t_{espera} > 0 \implies \text{sleep}(t_{espera}) \\[10pt]

& \text{\textbf{8. Verificación en cada disparo}} \\
& \text{Invariante de plaza válido si y solo si:} \\
& \sum_{p \in P} y_i[p] \cdot M'[p] = \sum_{p \in P} y_i[p] \cdot M_0[p] \quad \forall i
\end{aligned}