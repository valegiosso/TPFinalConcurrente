package monitor;

/**
 * interfaz publica del Monitor de concurrencia.
 * unico punto de acceso para los hilos que ejecutan la red de Petri.
 */
public interface MonitorInterface {
    boolean fireTransition(int transition);
}
