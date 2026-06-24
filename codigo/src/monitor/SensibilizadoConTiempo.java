package monitor;

/**
 * Maneja la semantica temporal de una transicion.
 * Cada transicion temporal tiene una ventana [alfa, beta] en milisegundos.
 * Si alfa y beta son 0, la transicion no es temporal (inmediata).
 */
public class SensibilizadoConTiempo {

    private long timeStamp;
    private final long alfa;  // limite inferior de la ventana temporal (ms)
    private final long beta;  // limite superior de la ventana temporal (ms)
    private boolean esperando;

    public SensibilizadoConTiempo(long alfa, long beta) {
        this.alfa = alfa;
        this.beta = beta;
        this.timeStamp = 0;
        this.esperando = false;
    }

    /**
     * Evalua si el momento actual esta dentro de la ventana temporal.
     * Retorna true si alfa <= (ahora - timeStamp) <= beta.
     * Si la transicion no es temporal (alfa == 0 && beta == 0), siempre retorna true.
     */
    public boolean testVentanaTiempo() {
        if (alfa == 0 && beta == 0) {
            return true; // transicion inmediata
        }
        long ahora = System.currentTimeMillis();
        long transcurrido = ahora - timeStamp;
        return transcurrido >= alfa && transcurrido <= beta;
    }

    /**
     * Retorna true si el momento actual esta ANTES de la ventana temporal.
     * Es decir, (ahora - timeStamp) < alfa.
     */
    public boolean antesDeLaVentana() {
        if (alfa == 0 && beta == 0) {
            return false;
        }
        long ahora = System.currentTimeMillis();
        long transcurrido = ahora - timeStamp;
        return transcurrido < alfa;
    }

    /**
     * Calcula cuantos ms faltan para que se abra la ventana.
     */
    public long tiempoRestante() {
        long ahora = System.currentTimeMillis();
        long restante = (timeStamp + alfa) - ahora;
        return Math.max(restante, 0);
    }

    public void setNuevoTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public void setEsperando(boolean esp) {
        this.esperando = esp;
    }

    public void resetEsperando() {
        this.esperando = false;
    }

    public boolean isEsperando() {
        return esperando;
    }

    public long getAlfa() {
        return alfa;
    }

    public long getBeta() {
        return beta;
    }
}
