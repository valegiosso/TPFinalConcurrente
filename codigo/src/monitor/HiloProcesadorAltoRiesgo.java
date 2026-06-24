package monitor;

/**
 * Hilo encargado del flujo de pago de alto riesgo.
 * Plaza: P4, usando ambos recursos P7 (gateway) y P8 (antifraude) simultaneamente.
 *
 * Transiciones asignadas: T4, T5 (ruteo a alto riesgo, scoring + captura).
 */
public class HiloProcesadorAltoRiesgo extends HiloBase {

    public HiloProcesadorAltoRiesgo(MonitorInterface monitor, int[] transiciones) {
        super(monitor, transiciones);
    }
}
