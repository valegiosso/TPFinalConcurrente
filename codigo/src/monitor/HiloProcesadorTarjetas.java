package monitor;

/**
 * Hilo encargado del flujo de pago con tarjeta de credito/debito.
 * Plazas: P2 -> P3, usando recurso P7 (gateway de tarjetas).
 *
 * Transiciones asignadas: T1, T2, T3 (ruteo a tarjeta, autorizacion, captura).
 */
public class HiloProcesadorTarjetas extends HiloBase {

    public HiloProcesadorTarjetas(MonitorInterface monitor, int[] transiciones) {
        super(monitor, transiciones);
    }
}
