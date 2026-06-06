package monitor;

/**
 * Monitor de concurrencia para la ejecucion de la Red de Petri.
 *
 * Implementa MonitorInterface exponiendo unicamente fireTransition().
 * Internamente gestiona la exclusion mutua (Mutex), la Red de Petri (RdP),
 * la politica de conflictos (Politica) y el registro de disparos (Logger).
 *
 * El monitor es agnostico a la red: no contiene referencias a transiciones
 * puntuales. Toda la logica depende de las matrices y vectores inyectados.
 */
public class Monitor implements MonitorInterface {

    private final RdP rdp;
    private final Mutex mutex;
    private final Politica politica;
    private final Logger logger;
    private int contadorInvariantes;
    private volatile boolean running;
    private final int maxInvariantes;

    // Transicion de salida (la ultima del ciclo, que deposita en P9)
    // Se usa para contar invariantes completados.
    // TODO: Configurar segun la red
    private final int transicionSalida;

    public Monitor(RdP rdp, Politica politica, Logger logger, int maxInvariantes, int transicionSalida) {
        int cantTransiciones = rdp.getVectorSensibilizadas().getCantidad();
        this.rdp = rdp;
        this.mutex = new Mutex(cantTransiciones);
        this.politica = politica;
        this.logger = logger;
        this.contadorInvariantes = 0;
        this.running = true;
        this.maxInvariantes = maxInvariantes;
        this.transicionSalida = transicionSalida;
    }

    @Override
    public boolean fireTransition(int transition) {
        mutex.acquire();
        try {
            // Si el sistema ya termino, salir
            while (running) {
                // Verificar si la transicion esta sensibilizada por marcado
                VectorSensibilizadas vs = rdp.getVectorSensibilizadas();

                if (!vs.sensibilizadaPorMarcado(transition)) {
                    // No sensibilizada por marcado: esperar en la condicion
                    try {
                        mutex.await(transition);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                    continue; // Re-evaluar al despertar
                }

                // Sensibilizada por marcado. Verificar ventana temporal.
                if (vs.estaSensibilizadoPeroAntes(transition)) {
                    // Hay que esperar: liberar lock, dormir, re-intentar
                    long espera = vs.tiempoRestante(transition);
                    vs.getTiempo(transition).setEsperando(true);
                    mutex.release();

                    try {
                        Thread.sleep(espera);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }

                    // Re-adquirir el lock para re-intentar
                    mutex.acquire();
                    vs.getTiempo(transition).setEsperando(false);
                    continue; // Re-evaluar
                }

                // Intentar disparar
                boolean disparada = rdp.disparar(transition);

                if (disparada) {
                    // Registrar en el log
                    logger.escribirDisparo(transition);

                    // Verificar invariantes de plaza
                    if (!rdp.getEstadoActual().verificarInvariantePlazas()) {
                        System.err.println("ERROR: Invariante de plaza violado despues de disparar T" + transition);
                    }

                    // Contar si es la transicion de salida
                    if (transition == transicionSalida) {
                        contadorInvariantes++;
                        if (contadorInvariantes >= maxInvariantes) {
                            stop();
                            return true;
                        }
                    }

                    // Consultar politica para despertar hilos
                    despertarSegunPolitica();

                    return true;
                } else {
                    // No se pudo disparar (no deberia pasar si estaba sensibilizada)
                    try {
                        mutex.await(transition);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }

            return false; // Sistema terminado
        } finally {
            mutex.release();
        }
    }

    /**
     * Usa la politica para decidir a cual transicion habilitada con hilos
     * esperando se le da prioridad, y la despierta.
     */
    private void despertarSegunPolitica() {
        VectorSensibilizadas vs = rdp.getVectorSensibilizadas();
        int cant = vs.getCantidad();

        boolean[] habilitadas = new boolean[cant];
        boolean[] conHilosEsperando = new boolean[cant];

        for (int i = 0; i < cant; i++) {
            habilitadas[i] = vs.sensibilizadaPorMarcado(i);
            conHilosEsperando[i] = mutex.hasWaiters(i);
        }

        int seleccionada = politica.decidirTransicion(habilitadas, conHilosEsperando);

        if (seleccionada >= 0) {
            mutex.signal(seleccionada);
        }
    }

    /**
     * Detiene la ejecucion del sistema.
     * Despierta a todos los hilos para que terminen limpiamente.
     */
    public void stop() {
        running = false;
        mutex.signalAll();
    }

    public boolean isRunning() {
        return running;
    }

    public int getContadorInvariantes() {
        return contadorInvariantes;
    }
}
