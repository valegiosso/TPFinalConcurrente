package monitor;

/**
 * Implementacion concreta del control de ejecucion para el sistema PSP.
 * Conoce que la transicion de entrada es la que ingresa transacciones
 * y la de salida es la que completa los invariantes.
 */
public class ControlPSP implements ControlDeEjecucion {

    private final int transicionEntrada;
    private final int transicionSalida;
    private final int maxInvariantes;
    private int contadorAdmitidas;
    private int contadorInvariantes;

    public ControlPSP(int transicionEntrada, int transicionSalida, int maxInvariantes) {
        this.transicionEntrada = transicionEntrada;
        this.transicionSalida = transicionSalida;
        this.maxInvariantes = maxInvariantes;
        this.contadorAdmitidas = 0;
        this.contadorInvariantes = 0;
    }

    @Override
    public synchronized void notificarDisparo(int transition) {
        if (transition == transicionEntrada) {
            contadorAdmitidas++;
        }
        if (transition == transicionSalida) {
            contadorInvariantes++;
        }
    }

    @Override
    public synchronized boolean debeFinalizar() {
        return contadorInvariantes >= maxInvariantes;
    }

    @Override
    public synchronized boolean bloquearTransicion(int transition) {
        return transition == transicionEntrada && contadorAdmitidas >= maxInvariantes;
    }
}
