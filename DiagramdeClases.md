# Diagrama de Clases - TP Final Programación Concurrente 2026

Este archivo contiene el diagrama de clases que modela el sistema de procesamiento de pagos (PSP) basado en la Red de Petri descripta en el enunciado y la secuencia de llamadas definida en [DiagramdeSecuencia.md](file:///d:/facultad/concurrente/TPFinalConcurrente/DiagramdeSecuencia.md).

## Diagrama en Mermaid

```mermaid
classDiagram
    class MonitorInterface {
        <<interface>>
        +fireTransition(int transition) boolean
    }

    class Monitor {
        -RdP rdp
        -Mutex mutex
        -Politica politica
        -boolean running
        +Monitor(RdP rdp, Politica politica)
        +fireTransition(int transition) boolean
        +stop() void
    }

    class Mutex {
        -ReentrantLock lock
        -Condition[] conds
        +Mutex(int cantidadTransiciones)
        +acquire() void
        +release() void
        +await(int transition) void
        +signal(int transition) void
        +signalAll() void
    }

    class RdP {
        -Matrizi matrizI
        -VectorDeEstado vectorDeEstado
        -VectorSensibilizadas vectorSensibilizadas
        +RdP(Matrizi matrizI, VectorDeEstado estadoInicial)
        +disparar(int transition) boolean
        +getMatrizIncidencia() Matrizi
        +getEstadoActual() VectorDeEstado
        +getVectorSensibilizadas() VectorSensibilizadas
    }

    class Matrizi {
        -int[][] matrix
        +Matrizi(int[][] matrix)
        +getColumna(int transition) int[]
        +getFilas() int
        +getColumnas() int
    }

    class VectorDeEstado {
        -int[] marcado
        +VectorDeEstado(int[] marcadoInicial)
        +getMarcado() int[]
        +sumarColumna(int[] columna) void
        +verificarInvariantePlazas() boolean
    }

    class VectorSensibilizadas {
        -boolean[] sensibilizadas
        -SensibilizadoConTiempo[] tiempos
        +VectorSensibilizadas(int cantidadTransiciones)
        +estaSensibilizado(int transition) boolean
        +actualiceSensibilizadoT(int transition, boolean state) void
        +update(VectorDeEstado estado, Matrizi matriz) void
    }

    class SensibilizadoConTiempo {
        -long timeStamp
        -long alfa
        -long beta
        -boolean esperando
        +SensibilizadoConTiempo(long alfa, long beta)
        +testVentanaTiempo() boolean
        +antesDeLaVentana() boolean
        +setNuevoTimeStamp() void
        +setEsperando(boolean esp) void
        +resetEsperando() void
    }

    class Politica {
        <<interface>>
        +decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) int
    }

    class PoliticaAleatoria {
        +decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) int
    }

    class PoliticaPriorizada {
        +decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) int
    }

    class HiloBase {
        <<abstract>>
        #MonitorInterface monitor
        #int[] transicionesAsignadas
        +HiloBase(MonitorInterface monitor, int[] transiciones)
        +run() void
    }

    class HiloGenerador {
        +HiloGenerador(MonitorInterface monitor, int[] transiciones)
        +run() void
    }

    class HiloProcesadorTarjetas {
        +HiloProcesadorTarjetas(MonitorInterface monitor, int[] transiciones)
        +run() void
    }

    class HiloProcesadorTransferencias {
        +HiloProcesadorTransferencias(MonitorInterface monitor, int[] transiciones)
        +run() void
    }

    class HiloProcesadorAltoRiesgo {
        +HiloProcesadorAltoRiesgo(MonitorInterface monitor, int[] transiciones)
        +run() void
    }

    %% Relaciones
    Monitor ..|> MonitorInterface : implementa
    Monitor "1" *-- "1" RdP : composición
    Monitor "1" *-- "1" Mutex : composición
    Monitor "1" o-- "1" Politica : agregación
    
    PoliticaAleatoria ..|> Politica : implementa
    PoliticaPriorizada ..|> Politica : implementa
    
    RdP "1" *-- "1" Matrizi : composición
    RdP "1" *-- "1" VectorDeEstado : composición
    RdP "1" *-- "1" VectorSensibilizadas : composición
    
    VectorSensibilizadas "1" *-- "*" SensibilizadoConTiempo : composición
    
    HiloBase ..> MonitorInterface : usa
    HiloGenerador --|> HiloBase : hereda
    HiloProcesadorTarjetas --|> HiloBase : hereda
    HiloProcesadorTransferencias --|> HiloBase : hereda
    HiloProcesadorAltoRiesgo --|> HiloBase : hereda
```

## Relación con el Diagrama de Secuencia

El diagrama de secuencia describe el flujo detallado dentro de un intento de disparo (`fireTransition`):
1. **Hilo de Ejecución** (por ejemplo, `HiloProcesadorTarjetas`) llama a `monitor.fireTransition(t)`.
2. El `Monitor` bloquea el acceso mediante `mutex.acquire()`.
3. Llama a `rdp.disparar(t)` para evaluar la red:
   - `rdp` consulta a `vectorSensibilizadas.estaSensibilizado(t)`.
   - `vectorSensibilizadas` consulta a la instancia correspondiente de `SensibilizadoConTiempo` mediante `testVentanaTiempo()`.
   - Si está dentro de la ventana de tiempo (`ventana == true`), se procede a actualizar el marcado.
   - Si no está habilitado o está antes de la ventana, el hilo libera el lock (`mutex.release()`), marca el estado como esperando (`setEsperando(true)`), y duerme (`sleep`).
4. Si la transición se dispara exitosamente (`k == true`):
   - El estado cambia sumando la columna de la matriz de incidencia (`estado = estado + columna`).
   - Se actualizan los índices de sensibilización de las transiciones afectadas llamando a `actualiceSensibilizadoT(t, state)` y se actualiza el `timeStamp` para las transiciones recién sensibilizadas.
5. El monitor consulta a `Politica` para ver a qué transición habilitada y con hilos en espera debe despertar, y envía la señal a través del `Mutex` (`mutex.signal(t_seleccionada)`).
6. El `Monitor` libera el lock con `mutex.release()`.
