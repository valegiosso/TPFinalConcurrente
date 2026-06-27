package monitor;

/**
 * hilo de ejecucion generico del sistema,cada hilo recibe un conjunto de
 * transiciones asignadas
 * que intenta disparar secuencialmente en bucle, hasta que el monitor lo
 * detenga
 */
public class HiloBase implements Runnable {

    protected final MonitorInterface monitor;
    protected final int[] transicionesAsignadas;

    public HiloBase(MonitorInterface monitor, int[] transiciones) {
        this.monitor = monitor;
        this.transicionesAsignadas = transiciones;
    }

    @Override
    public void run() {
        while (true) {
            for (int t : transicionesAsignadas) {
                boolean result = monitor.fireTransition(t);
                if (!result) {
                    // fireTransition retorna false cuando el monitor ya paro
                    return;
                }
            }
        }
    }
}
