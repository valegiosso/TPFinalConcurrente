# Diagrama de Secuencia del Monitor de Concurrencia (Ejecución de un Disparo con Políticas)

Este diagrama representa el modelo del monitor de concurrencia durante la ejecución de la función `fireTransition(t)` de acuerdo con el **Requerimiento 6** del enunciado. Muestra la adquisición del `mutex`, el intento de disparo en la Red de Petri (`RdP`), el uso de la `Politica` para resolver conflictos de hilos encolados en transiciones sensibilizadas, el traspaso de testigo (*Passing the Baton*), y el bloqueo en la cola de la transición si el disparo no es posible.

```mermaid
sequenceDiagram
    autonumber
    actor hilo1 as Hilo 1 (Activo)
    actor hilo2 as Hilo 2 (Dormido en Cola)
    participant m as monitor : Monitor
    participant mutex as mutex : Semaphore
    participant rdp as redPetri : RdP
    participant colas as colas : Colas
    participant pol as politica : Politica

    hilo1 ->> m: fireTransition(t) : boolean
    activate m
    m ->> mutex: acquire()
    activate mutex
    mutex -->> m: 
    deactivate mutex
    m ->> m: k = true

    loop while (k == true)
        m ->> rdp: disparar(t) : boolean
        activate rdp
        rdp -->> m: k
        deactivate rdp

        alt k == true (Disparo Exitoso)
            m ->> rdp: getSensibilizadas() : boolean[]
            activate rdp
            rdp -->> m: sensibilizadas
            deactivate rdp

            m ->> colas: quienesEstan() : boolean[]
            activate colas
            colas -->> m: conHilosEsperando
            deactivate colas

            m ->> m: m = sensibilizadas AND conHilosEsperando

            alt m != vacio (hay hilos esperando en transiciones sensibilizadas)
                m ->> pol: decidirTransicion(sensibilizadas, conHilosEsperando) : int
                activate pol
                pol -->> m: seleccionada
                deactivate pol

                alt seleccionada >= 0
                    m ->> colas: release(seleccionada)
                    activate colas
                    colas -->> hilo2: (despierta)
                    deactivate colas
                    Note over hilo2: Hilo 2 se despierta y hereda el mutex (Passing the Baton)
                    m -->> hilo1: return true
                    Note over hilo1: Hilo 1 sale del monitor sin liberar mutex
                end
            else m == vacio (no hay nadie a quien despertar)
                m ->> m: k = false
            end

        else k == false (Disparo Bloqueado)
            m ->> mutex: release()
            activate mutex
            mutex -->> m: 
            deactivate mutex

            m ->> colas: acquire(t)
            activate colas
            Note over hilo1: Hilo 1 se bloquea en la cola de la transición t
            colas -->> m: (despierta por traspaso de testigo)
            deactivate colas
            Note over hilo1: Hilo 1 recupera la ejecución con el mutex heredado

            m ->> m: k = true
        end
    end

    Note over m: Salida normal sin traspaso de testigo (k = false)
    m ->> mutex: release()
    activate mutex
    mutex -->> m: 
    deactivate mutex
    m -->> hilo1: return true
    Note over hilo1: Hilo 1 sale del monitor liberando el mutex
    deactivate m
```