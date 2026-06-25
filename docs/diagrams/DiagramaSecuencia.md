# Diagrama de Secuencia del Monitor de Concurrencia (Ejecución con Tiempo y Políticas)

Este diagrama representa el ciclo de vida del monitor de concurrencia durante la ejecución de la función `fireTransition(t)` de acuerdo con los **Requerimientos 6 y 10** del enunciado, tomando como referencia el diseño y la estructura del proyecto.

Muestra en detalle:
1. La adquisición del `mutex`.
2. La **gestión del tiempo** (espera en transiciones temporales durmiendo fuera del monitor para liberar la exclusión mutua, y posterior reingreso).
3. El intento de disparo en la Red de Petri (`RdP`), incluyendo la verificación de la ventana temporal.
4. El cálculo de la plaza de conflicto `m = sensibilizadas AND quienesEstan`.
5. El uso de la `Politica` para resolver conflictos de hilos encolados en transiciones sensibilizadas mediante `decidirTransicion(m)`.
6. El traspaso de testigo (*Passing the Baton*), despertando hilos en cola sin liberar el mutex.
7. El bloqueo en la cola de la transición si el disparo no es factible en ese instante.

```mermaid
sequenceDiagram
    autonumber
    actor hilo1 as Hilo 1 (Activo)
    actor hilo2 as Hilo 2 (Dormido en Cola)
    participant m as monitor : Monitor
    participant mutex as mutex : Semaphore
    participant rdp as redPetri : RdP
    participant vs as vectorSens : VectorSensibilizadas
    participant st as tiempoTrans : SensibilizadoConTiempo
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
        m ->> vs: estaSensibilizadoPeroAntes(t) : boolean
        activate vs
        vs ->> st: antesDeLaVentana() : boolean
        activate st
        st -->> vs: antes
        deactivate st
        vs -->> m: antes
        deactivate vs

        alt antes == true (Debemos esperar que se abra la ventana)
            m ->> vs: tiempoRestante(t) : long
            activate vs
            vs ->> st: tiempoRestante() : long
            activate st
            st -->> vs: espera
            deactivate st
            vs -->> m: espera
            deactivate vs

            m ->> st: setEsperando(true)
            m ->> mutex: release()
            
            Note over hilo1: Hilo 1 duerme fuera del monitor para no bloquear a otros hilos
            hilo1 ->> hilo1: sleep(espera)
            hilo1 -->> m: (despierta del sleep)
            
            m ->> mutex: acquire()
            m ->> st: setEsperando(false)
            Note over m: Continúa el bucle (continue) para re-evaluar

        else antes == false (Intentar disparo inmediato)
            m ->> rdp: disparar(t) : boolean
            activate rdp
            rdp ->> vs: estaSensibilizado(t) : boolean
            activate vs
            vs ->> st: testVentanaTiempo() : boolean
            activate st
            st -->> vs: ventana
            deactivate st
            
            alt ventana == true y esperando == false
                vs ->> st: setNuevoTimeStamp()
                vs -->> rdp: true
                rdp ->> rdp: calcularNuevoEstado()
                rdp ->> st: resetEsperando()
                rdp ->> vs: update(estado, pre)
                activate vs
                Note over vs: Actualiza sensibilización y timestamps de nuevas habilitadas
                vs -->> rdp: 
                deactivate vs
                rdp -->> m: true
            else ventana == false o esperando == true
                vs -->> rdp: false
                rdp -->> m: false
            end
            deactivate vs
            rdp -->> m: k
            deactivate rdp

            alt k == true (Disparo Exitoso)
                alt rdp.isFinalizada() == true (Llegamos al límite de invariantes)
                    Note over m: Despierta a todos los hilos durmiendo en cola y termina
                    m -->> hilo1: return false
                end
                
                m ->> rdp: getSensibilizadasPorMarcado() : boolean[]
                activate rdp
                rdp -->> m: sensibilizadas
                deactivate rdp
                
                m ->> colas: quienesEstan() : boolean[]
                activate colas
                colas -->> m: quienesEstan
                deactivate colas

                m ->> m: m = sensibilizadas AND quienesEstan

                alt m != vacio (Hay hilos en conflicto esperando)
                    m ->> pol: decidirTransicion(m) : int
                    activate pol
                    pol -->> m: seleccionada
                    deactivate pol

                    m ->> colas: release(seleccionada)
                    activate colas
                    colas -->> hilo2: (despierta)
                    deactivate colas
                    Note over hilo2: Hilo 2 se despierta y hereda el mutex (Passing the Baton)
                    m -->> hilo1: return true
                    Note over hilo1: Hilo 1 sale del monitor sin liberar el mutex
                else m == vacio (No hay hilos en conflicto)
                    m ->> m: k = false
                end

            else k == false (Disparo Bloqueado por Marcado o Tiempo)
                m ->> mutex: release()
                activate mutex
                mutex -->> m: 
                deactivate mutex

                m ->> colas: acquire(t)
                activate colas
                Note over hilo1: Hilo 1 se bloquea en la cola de la transición t
                colas -->> m: (despierta por traspaso de testigo)
                deactivate colas
                Note over hilo1: Hilo 1 hereda el mutex y reanuda la ejecución
                m ->> m: k = true
            end
        end
    end

    Note over m: Salida normal liberando exclusión mutua (k = false)
    m ->> mutex: release()
    activate mutex
    mutex -->> m: 
    deactivate mutex
    m -->> hilo1: return true
    deactivate m
```