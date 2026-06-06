package monitor;

/**
 * Hilo encargado de la admision de transacciones.
 * Dispara T0 (arribo -> admision) y luego T9 no — este es el generador
 * que empuja transacciones al sistema disparando la transicion de entrada.
 *
 * Transiciones asignadas: las previas al conflicto de ruteo.
 */
public class HiloGenerador extends HiloBase {

    public HiloGenerador(MonitorInterface monitor, int[] transiciones) {
        super(monitor, transiciones);
    }
}
