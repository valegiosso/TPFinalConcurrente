package monitor;

/**
 * Interfaz publica del Monitor de concurrencia.
 * Unico punto de acceso para los hilos que ejecutan la red de Petri.
 */
public interface MonitorInterface {
    boolean fireTransition(int transition);
}
