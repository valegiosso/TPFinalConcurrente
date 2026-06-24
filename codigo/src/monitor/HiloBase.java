package monitor;

/**
 * Hilo de ejecucion generico del sistema.
 * Cada hilo recibe un conjunto de transiciones asignadas que intenta
 * disparar secuencialmente en bucle, hasta que el monitor lo detenga.
 *
 * La especializacion de cada hilo (generador, tarjetas, alto riesgo,
 * transferencias, salida) no esta en la clase sino en el array de
 * transiciones que se le pasa al construirlo en Main.
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
