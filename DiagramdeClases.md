classDiagram
    class MonitorInterface {
        <<interface>>
        +fireTransition(int transition) boolean
    }

    class Monitor {
        -RdP rdp
        -Semaphore mutex
        -Colas colas
        -Politica politica
        -Logger logger
        -ControlDeEjecucion control
        +Monitor(RdP rdp, Politica politica, Logger logger, ControlDeEjecucion control)
        +fireTransition(int transition) boolean
        -despertarATodosYSalir() void
    }

    class Logger {
        -String archivoPath
        +Logger(String archivoPath)
        +escribirDisparo(int transition) void
        +cerrarLog() void
    }

    class Colas {
        -Semaphore[] semaforos
        +Colas(int cantidadTransiciones)
        +acquire(int transition) void
        +release(int transition) void
        +quienesEstan() boolean[]
    }

    class RdP {
        -Matrizi matrizPre
        -Matrizi matrizPost
        -VectorDeEstado vectorDeEstado
        -VectorSensibilizadas vectorSensibilizadas
        +RdP(Matrizi matrizPre, Matrizi matrizPost, VectorDeEstado estadoInicial, VectorSensibilizadas vectorSensibilizadas)
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
        +VectorSensibilizadas(int cantidadTransiciones, SensibilizadoConTiempo[] tiempos)
        +estaSensibilizado(int transition) boolean
        +estaSensibilizadoPeroAntes(int transition) boolean
        +tiempoRestante(int transition) long
        +sensibilizadaPorMarcado(int transition) boolean
        +actualiceSensibilizadoT(int transition, boolean state) void
        +update(VectorDeEstado estado, Matrizi matrizPre) void
        +getTiempo(int transition) SensibilizadoConTiempo
        +getCantidad() int
    }

    class SensibilizadoConTiempo {
        -long timeStamp
        -long alfa
        -long beta
        -boolean esperando
        +SensibilizadoConTiempo(long alfa, long beta)
        +testVentanaTiempo() boolean
        +antesDeLaVentana() boolean
        +tiempoRestante() long
        +setNuevoTimeStamp() void
        +setEsperando(boolean esp) void
        +resetEsperando() void
        +isEsperando() boolean
    }

    class Politica {
        <<interface>>
        +decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) int
    }

    class PoliticaAleatoria {
        +decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) int
    }

    class PoliticaPriorizada {
        -int[] transicionesPrioritarias
        +PoliticaPriorizada(int[] transicionesPrioritarias)
        +decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) int
    }

    class ControlDeEjecucion {
        <<interface>>
        +notificarDisparo(int transition) void
        +debeFinalizar() boolean
        +bloquearTransicion(int transition) boolean
    }

    class ControlPSP {
        -int transicionEntrada
        -int transicionSalida
        -int maxInvariantes
        -int contadorAdmitidas
        -int contadorInvariantes
        +ControlPSP(int transicionEntrada, int transicionSalida, int maxInvariantes)
        +notificarDisparo(int transition) void
        +debeFinalizar() boolean
        +bloquearTransicion(int transition) boolean
    }

    class HiloBase {
        #MonitorInterface monitor
        #int[] transicionesAsignadas
        +HiloBase(MonitorInterface monitor, int[] transiciones)
        +run() void
    }

    %% Relaciones
    Monitor ..|> MonitorInterface : implementa
    Monitor "1" *-- "1" RdP : composición
    Monitor "1" *-- "1" Semaphore : composición
    Monitor "1" *-- "1" Colas : composición
    Monitor "1" o-- "1" Politica : agregación
    Monitor "1" o-- "1" Logger : agregación
    Monitor "1" o-- "1" ControlDeEjecucion : agregación
    
    PoliticaAleatoria ..|> Politica : implementa
    PoliticaPriorizada ..|> Politica : implementa
    
    ControlPSP ..|> ControlDeEjecucion : implementa
    
    RdP "1" *-- "2" Matrizi : composición (Pre y Post)
    RdP "1" *-- "1" VectorDeEstado : composición
    RdP "1" *-- "1" VectorSensibilizadas : composición
    
    VectorSensibilizadas "1" *-- "*" SensibilizadoConTiempo : composición
    
    HiloBase ..|> Runnable : implementa
    HiloBase ..> MonitorInterface : usa