package monitor;

/**
 * Representa la Red de Petri con sus matrices Pre y Post,
 * el vector de estado (marcado) y el vector de sensibilizadas.
 *
 * Esta clase es agnostica al significado de las plazas y transiciones;
 * solo opera con algebra matricial.
 */
public class RdP {

    private final Matrizi matrizPre;
    private final Matrizi matrizPost;
    private final VectorDeEstado vectorDeEstado;
    private final VectorSensibilizadas vectorSensibilizadas;

    public RdP(Matrizi matrizPre, Matrizi matrizPost, VectorDeEstado estadoInicial,
               VectorSensibilizadas vectorSensibilizadas) {
        this.matrizPre = matrizPre;
        this.matrizPost = matrizPost;
        this.vectorDeEstado = estadoInicial;
        this.vectorSensibilizadas = vectorSensibilizadas;

        // Calcular sensibilizacion inicial
        this.vectorSensibilizadas.update(this.vectorDeEstado, this.matrizPre);
    }

    /**
     * Intenta disparar la transicion indicada.
     *
     * Retorna:
     *   true  -> la transicion se disparo exitosamente, el marcado cambio.
     *   false -> la transicion no estaba sensibilizada o no cumplia la ventana temporal.
     */
    public boolean disparar(int transition) {
        if (!vectorSensibilizadas.estaSensibilizado(transition)) {
            return false;
        }

        // Ejecutar el disparo: M = M - Pre(:,t) + Post(:,t)
        int[] columnaPre = matrizPre.getColumna(transition);
        int[] columnaPost = matrizPost.getColumna(transition);

        vectorDeEstado.restarColumna(columnaPre);
        vectorDeEstado.sumarColumna(columnaPost);

        // Resetear el estado "esperando" de la transicion disparada
        vectorSensibilizadas.getTiempo(transition).resetEsperando();

        // Recalcular sensibilizacion para todas las transiciones
        vectorSensibilizadas.update(vectorDeEstado, matrizPre);

        return true;
    }

    public Matrizi getMatrizPre() {
        return matrizPre;
    }

    public Matrizi getMatrizPost() {
        return matrizPost;
    }

    public VectorDeEstado getEstadoActual() {
        return vectorDeEstado;
    }

    public VectorSensibilizadas getVectorSensibilizadas() {
        return vectorSensibilizadas;
    }
}
