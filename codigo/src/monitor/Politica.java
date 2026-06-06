package monitor;

/**
 * Interfaz para la estrategia de resolucion de conflictos.
 * Permite intercambiar la politica sin modificar el Monitor.
 */
public interface Politica {

    /**
     * Dado un conjunto de transiciones habilitadas y cuales tienen hilos esperando,
     * decide cual transicion debe ser despertada.
     *
     * @param habilitadas       arreglo donde habilitadas[i] = true si la transicion i esta sensibilizada
     * @param conHilosEsperando arreglo donde conHilosEsperando[i] = true si hay hilos esperando en la condicion i
     * @return indice de la transicion a despertar, o -1 si no hay ninguna candidata
     */
    int decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando);
}
