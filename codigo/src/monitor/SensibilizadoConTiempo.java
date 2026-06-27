package monitor;

/**
 * maneja la semantica temporal de una transicion
 * cada transicion temporal tiene una ventana [alfa, beta] en milisegundos
 * si alfa y beta son 0, la transicion no es temporal (inmediata)
 */
public class SensibilizadoConTiempo {

    private long timeStamp;
    private final long alfa;
    private final long beta;
    private boolean esperando;

    public SensibilizadoConTiempo(long alfa, long beta) {
        this.alfa = alfa;
        this.beta = beta;
        this.timeStamp = 0;
        this.esperando = false;
    }

    /**
     * evalua si el momento actual esta dentro de la ventana temporal
     * retorna true si alfa <= (ahora - timeStamp) <= beta
     * si la transicion no es temporal (alfa == 0 && beta == 0), siempre retorna
     * true
     */
    public boolean testVentanaTiempo() {
        if (alfa == 0 && beta == 0) {
            return true;
        }
        long ahora = System.currentTimeMillis();
        long transcurrido = ahora - timeStamp;
        return transcurrido >= alfa && transcurrido <= beta;
    }

    /**
     * retorna true si el momento actual esta ANTES de la ventana temporal;
     * (ahora - timeStamp) < alfa
     */
    public boolean antesDeLaVentana() {
        if (alfa == 0 && beta == 0) {
            return false;
        }
        long ahora = System.currentTimeMillis();
        long transcurrido = ahora - timeStamp;
        return transcurrido < alfa;
    }

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
