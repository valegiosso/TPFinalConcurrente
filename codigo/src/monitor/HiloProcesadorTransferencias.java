package monitor;

/**
 * Hilo encargado del flujo de transferencia bancaria.
 * Plazas: P5 -> P6, usando recurso P8 (motor antifraude) exclusivamente.
 *
 * Transiciones asignadas: T6, T7, T8 (ruteo a transferencia, validacion, ejecucion).
 */
public class HiloProcesadorTransferencias extends HiloBase {

    public HiloProcesadorTransferencias(MonitorInterface monitor, int[] transiciones) {
        super(monitor, transiciones);
    }
}
