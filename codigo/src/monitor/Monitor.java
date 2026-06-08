package monitor;

import java.util.concurrent.Semaphore;

/**
 * Monitor de concurrencia para la ejecucion de la Red de Petri.
 *
 * Implementa MonitorInterface exponiendo unicamente fireTransition().
 * Internamente gestiona la exclusion mutua (Semaphore), las colas de espera, 
 * la Red de Petri (RdP), la politica de conflictos (Politica) y el registro 
 * de disparos (Logger).
 *
 * El monitor es agnostico a la red: no contiene referencias a transiciones
 * puntuales. Toda la logica depende de las matrices y vectores inyectados.
 */
public class Monitor implements MonitorInterface {

    private final RdP rdp;
    private final Semaphore mutex;
    private final Colas colas;
    private final Politica politica;
    private final Logger logger;
    private int contadorInvariantes;
    private final int maxInvariantes;

    // Transicion de salida (la ultima del ciclo, que deposita en P9)
    // Se usa para contar invariantes completados.
    private final int transicionSalida;

    public Monitor(RdP rdp, Politica politica, Logger logger, int maxInvariantes, int transicionSalida) {
        int cantTransiciones = rdp.getVectorSensibilizadas().getCantidad();
        this.rdp = rdp;
        this.mutex = new Semaphore(1, true);
        this.colas = new Colas(cantTransiciones);
        this.politica = politica;
        this.logger = logger;
        this.contadorInvariantes = 0;
        this.maxInvariantes = maxInvariantes;
        this.transicionSalida = transicionSalida;
    }

    @Override
    public boolean fireTransition(int transition) {
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }

        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        boolean k = true;
        while (k) {
            // Verificar si llegamos al limite de invariantes para finalizar
            if (contadorInvariantes >= maxInvariantes) {
                despertarATodosYSalir();
                return false;
            }

            VectorSensibilizadas vs = rdp.getVectorSensibilizadas();

            // Si hay que esperar por tiempo
            if (vs.estaSensibilizadoPeroAntes(transition)) {
                long espera = vs.tiempoRestante(transition);
                vs.getTiempo(transition).setEsperando(true);
                mutex.release();
                
                try {
                    Thread.sleep(espera);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                
                try {
                    mutex.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                
                vs.getTiempo(transition).setEsperando(false);
                continue; // Reevaluar despues del sleep
            }

            // Intentar disparar
            k = rdp.disparar(transition);

            if (k) {
                // Registrar en el log
                logger.escribirDisparo(transition);

                if (transition == transicionSalida) {
                    contadorInvariantes++;
                }

                // Verificar invariantes de plaza
                if (!rdp.getEstadoActual().verificarInvariantePlazas()) {
                    System.err.println("ERROR: Invariante de plaza violado despues de disparar T" + transition);
                }

                // Si este disparo completo el total de invariantes
                if (contadorInvariantes >= maxInvariantes) {
                    despertarATodosYSalir();
                    return false;
                }

                // Obtener arreglos para la politica
                boolean[] sensibilizadas = new boolean[vs.getCantidad()];
                for (int i = 0; i < vs.getCantidad(); i++) {
                    sensibilizadas[i] = vs.sensibilizadaPorMarcado(i);
                }
                boolean[] quienesEstan = colas.quienesEstan();

                // Consultar politica para despertar hilos
                int seleccionada = politica.decidirTransicion(sensibilizadas, quienesEstan);

                if (seleccionada >= 0) {
                    colas.release(seleccionada);
                    return true; // Sale del monitor sin liberar mutex (pasando el testigo)
                } else {
                    k = false; // Termina el ciclo k
                }
            } else {
                mutex.release();
                colas.acquire(transition);
                
                // Si al despertar ya termino el programa
                if (contadorInvariantes >= maxInvariantes) {
                    despertarATodosYSalir();
                    return false;
                }
                
                k = true; // Sigue en el ciclo k
            }
        }

        mutex.release(); // Si sale del while por m==0 (k=false)
        return true;
    }
    
    private void despertarATodosYSalir() {
        for (int i = 0; i < rdp.getVectorSensibilizadas().getCantidad(); i++) {
            colas.release(i);
        }
        mutex.release();
    }
}
