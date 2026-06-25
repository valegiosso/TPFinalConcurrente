package monitor;

import java.util.concurrent.Semaphore;

/**
 * Monitor de concurrencia para la ejecucion de la Red de Petri.
 *
 * Implementa MonitorInterface exponiendo unicamente fireTransition().
 * Internamente gestiona la exclusion mutua (Semaphore), las colas de espera, 
 * la Red de Petri (RdP) y la politica de conflictos (Politica).
 */
public class Monitor implements MonitorInterface {

    private final RdP rdp;
    private final Semaphore mutex;
    private final Colas colas;
    private final Politica politica;

    public Monitor(RdP rdp, Politica politica) {
        int cantTransiciones = rdp.getCantidadTransiciones();
        this.rdp = rdp;
        this.mutex = new Semaphore(1, true);
        this.colas = new Colas(cantTransiciones);
        this.politica = politica;
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
            if (rdp.isFinalizada()) {
                despertarATodosYSalir();
                return false;
            }

            // Evitar admitir mas transacciones que el limite maximo
            if (rdp.bloquearTransicion(transition)) {
                mutex.release();
                return false;
            }

            // Si hay que esperar por tiempo
            if (rdp.estaSensibilizadoPeroAntes(transition)) {
                long espera = rdp.tiempoRestante(transition);
                rdp.setEsperando(transition, true);
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
                
                rdp.setEsperando(transition, false);
                continue; // Reevaluar despues del sleep
            }

            // Intentar disparar
            k = rdp.disparar(transition);

            if (k) {
                // Si este disparo completo el total de invariantes
                if (rdp.isFinalizada()) {
                    despertarATodosYSalir();
                    return false;
                }

                // Obtener arreglos para la politica
                boolean[] sensibilizadas = rdp.getSensibilizadasPorMarcado();
                boolean[] quienesEstan = colas.quienesEstan();

                // Calcular 'm' (sensibilizadas AND quienesEstan) y verificar si hay al menos una transicion
                boolean[] m = new boolean[sensibilizadas.length];
                boolean m_distinto_de_cero = false;
                for (int i = 0; i < sensibilizadas.length; i++) {
                    m[i] = sensibilizadas[i] && quienesEstan[i];
                    if (m[i]) {
                        m_distinto_de_cero = true;
                    }
                }

                if (m_distinto_de_cero) {
                    // Consultar politica para despertar hilos
                    int seleccionada = politica.decidirTransicion(m);
                    colas.release(seleccionada);
                    return true; // Sale del monitor sin liberar mutex (pasando el testigo)
                } else {
                    k = false; // Termina el ciclo k
                }
            } else {
                mutex.release();
                colas.acquire(transition);

                // Si al despertar ya termino el programa
                if (rdp.isFinalizada()) {
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
        for (int i = 0; i < rdp.getCantidadTransiciones(); i++) {
            colas.release(i);
        }
        mutex.release();
    }
}

