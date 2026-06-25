package monitor;

/**
 * Interfaz para el control de ejecucion del sistema.
 * Permite al Monitor delegar la logica de finalizacion y bloqueo
 * sin conocer la topologia de la red que ejecuta.
 *
 * De esta manera, el Monitor permanece agnostico a la red:
 * no contiene referencias a transiciones puntuales.
 */
public interface ControlDeEjecucion {

    /**
     * Notifica que una transicion se disparo exitosamente.
     * La implementacion decide internamente que hacer (contar, etc).
     *
     * @param transition indice de la transicion disparada
     */
    void notificarDisparo(int transition);

    /**
     * Retorna true si el sistema debe detenerse.
     */
    boolean debeFinalizar();

    /**
     * Retorna true si la transicion indicada debe bloquearse
     * en este momento (ej: limitar admisiones).
     *
     * @param transition indice de la transicion a evaluar
     */
    boolean bloquearTransicion(int transition);
}
