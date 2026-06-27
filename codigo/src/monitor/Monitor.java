package monitor;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * monitor de concurrencia para la ejecucion de la Red de Petri
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
            System.out.println("[" + Thread.currentThread().getName() + "]: Interrumpido.");
            Thread.currentThread().interrupt();
            return false;
        }

        System.out.println("[" + Thread.currentThread().getName() + "]: Entrando al monitor.");
        boolean k = true;
        while (k) {
            // verificar si llegamos al limite de invariantes para finalizar
            if (rdp.isFinalizada()) {
                despertarATodosYSalir();
                return false;
            }

            // evitar admitir mas transacciones que el limite maximo
            if (rdp.bloquearTransicion(transition)) {
                mutex.release();
                return false;
            }
            // si hay que esperar por tiempo
            if (rdp.estaSensibilizadoPeroAntes(transition)) {
                long espera = rdp.tiempoRestante(transition);
                rdp.setEsperando(transition, true);
                System.out.println(
                        "[" + Thread.currentThread().getName() + "]: A dormir con sleep() y liberar el mutex.");
                mutex.release();

                try {
                    Thread.sleep(espera);
                } catch (InterruptedException e) {
                    System.out.println("[" + Thread.currentThread().getName() + "]: Interrumpido.");
                    Thread.currentThread().interrupt();
                    return false;
                }

                try {
                    mutex.acquire();
                    System.out.println("[" + Thread.currentThread().getName() + "]: Me desperté del sleep().");
                } catch (InterruptedException e) {
                    System.out.println("[" + Thread.currentThread().getName() + "]: Interrumpido.");
                    Thread.currentThread().interrupt();
                    return false;
                }
                rdp.setEsperando(transition, false);
                continue; // Reevaluar despues del sleep
            }
            // intentar disparar
            k = rdp.disparar(transition);

            if (k) {
                // si este disparo completo el total de invariantes
                if (rdp.isFinalizada()) {
                    despertarATodosYSalir();
                    return false;
                }

                // obtener arreglos para la politica
                boolean[] sensibilizadas = rdp.getSensibilizadasPorMarcado();
                boolean[] quienesEstan = colas.quienesEstan();

                System.out.println(Arrays.toString(rdp.getMarcadoActual()) + ": Marcado");
                System.out.println(Arrays.toString(sensibilizadas) + ": Sensibilizadas");
                System.out.println(Arrays.toString(quienesEstan) + ": Dormidos");

                // calcular 'm' (sensibilizadas AND quienesEstan) y verificar si hay al menos
                // una transicion
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
                    System.out.println("[" + Thread.currentThread().getName() + "]: Levanta el hilo del semaforo "
                            + seleccionada + " y abandona el monitor.");
                    colas.release(seleccionada);
                    return true; // Sale del monitor sin liberar mutex (pasando el testigo)
                } else {
                    System.out.println("[" + Thread.currentThread().getName() + "]: Ningún hilo para levantar.");
                    k = false; // Termina el ciclo k
                }
            } else {
                System.out.println("[" + Thread.currentThread().getName() + "]: Faltan tokens para T" + transition
                        + ". A dormir la siesta.");
                mutex.release();
                colas.acquire(transition);
                System.out.println("[" + Thread.currentThread().getName() + "]: Se despertó de la siesta.");

                // si al despertar ya termino el programa
                if (rdp.isFinalizada()) {
                    despertarATodosYSalir();
                    return false;
                }
                k = true; // sigue en el ciclo k
            }
        }

        System.out.println("[" + Thread.currentThread().getName() + "]: Abandona el monitor.");
        mutex.release(); // si sale del while por m==0 (k=false)
        return true;
    }

    private void despertarATodosYSalir() {
        System.out
                .println("[" + Thread.currentThread().getName() + "]: Invariantes completados. Abandonando monitor. ");
        for (int i = 0; i < rdp.getCantidadTransiciones(); i++) {
            colas.release(i);
        }
        mutex.release();
    }
}
