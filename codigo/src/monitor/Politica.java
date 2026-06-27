package monitor;

/**
 * interfaz para la estrategia de resolucion de conflictos, permite intercambiar
 * la politica sin modificar el Monitor
 */
public interface Politica {

    /**
     * Dado el vector 'm' (resultado de hacer el AND entre las transiciones
     * habilitadas y
     * las que tienen hilos esperando), decide cual transicion debe ser despertada.
     *
     * @param m arreglo donde m[i] = true si la transicion i esta sensibilizada y
     *          tiene hilos esperando
     * @return indice de la transicion a despertar, o -1 si no hay ninguna candidata
     */
    int decidirTransicion(boolean[] m);

    String getName();
}
