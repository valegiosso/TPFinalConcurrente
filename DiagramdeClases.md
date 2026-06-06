classDiagram
    class MonitorInterface {
        <<interface>>
        +fireTransition(int transition) boolean
    }

    class Monitor {
        -RdP rdp
        -Mutex mutex
        -Politica politica
        -Logger logger
        -int contadorInvariantes
        -boolean running
        +Monitor(RdP rdp, Politica politica, Logger logger)
        +fireTransition(int transition) boolean
        +stop() void
    }

    class Logger {
        -String archivoPath
        +Logger(String archivoPath)
        +escribirDisparo(int transition) void
        +cerrarLog() void
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
        -Matrizi matrizPre
        -Matrizi matrizPost
        -VectorDeEstado vectorDeEstado
        -VectorSensibilizadas vectorSensibilizadas
        +RdP(Matrizi matrizPre, Matrizi matrizPost, VectorDeEstado estadoInicial)
        +disparar(int transition) boolean
        +getMatrizPre() Matrizi
        +getMatrizPost() Matrizi
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
        +restarColumna(int[] columnaPre) void
        +sumarColumna(int[] columnaPost) void
        +verificarInvariantePlazas() boolean
    }

    class VectorSensibilizadas {
        -boolean[] sensibilizadas
        -SensibilizadoConTiempo[] tiempos
        +VectorSensibilizadas(int cantidadTransiciones)
        +estaSensibilizado(int transition) boolean
        +actualiceSensibilizadoT(int transition, boolean state) void
        +update(VectorDeEstado estado, Matrizi matrizPre) void
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
    Monitor "1" o-- "1" Logger : agregación
    
    PoliticaAleatoria ..|> Politica : implementa
    PoliticaPriorizada ..|> Politica : implementa
    
    RdP "1" *-- "2" Matrizi : composición (Pre y Post)
    RdP "1" *-- "1" VectorDeEstado : composición
    RdP "1" *-- "1" VectorSensibilizadas : composición
    
    VectorSensibilizadas "1" *-- "*" SensibilizadoConTiempo : composición
    
    HiloBase ..> MonitorInterface : usa
    HiloGenerador --|> HiloBase : hereda
    HiloProcesadorTarjetas --|> HiloBase : hereda
    HiloProcesadorTransferencias --|> HiloBase : hereda
    HiloProcesadorAltoRiesgo --|> HiloBase : hereda