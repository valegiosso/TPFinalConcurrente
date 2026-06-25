package monitor;

/**
 * Representa la Red de Petri con sus matrices Pre y Post,
 * el vector de estado (marcado) y el vector de sensibilizadas.
 *
 * Maneja internamente los contadores de invariantes de transicion
 * para saber cuando detener la ejecucion.
 */
public class RdP {

    // Transicion que admite nuevas transacciones al sistema (entrada)
    private static final int TRANSICION_ENTRADA = 0;
    // Transicion que marca un invariante completo (salida)
    private static final int TRANSICION_SALIDA  = 9;
    // Cantidad de invariantes de transicion a completar
    public  static final int MAX_INVARIANTES    = 200;

    // Contador de disparos de T0 (admisiones al sistema)
    private int contadorEntrada = 0;
    // Contador de disparos de T9 (invariantes completados)
    private int contadorSalida  = 0;

    private final Matrizi matrizPre;
    private final Matrizi matrizPost;
    private final VectorDeEstado vectorDeEstado;
    private final VectorSensibilizadas vectorSensibilizadas;
    private final Logger logger;

    public RdP(Matrizi matrizPre, Matrizi matrizPost, VectorDeEstado estadoInicial,
               VectorSensibilizadas vectorSensibilizadas, Logger logger) {
        this.matrizPre = matrizPre;
        this.matrizPost = matrizPost;
        this.vectorDeEstado = estadoInicial;
        this.vectorSensibilizadas = vectorSensibilizadas;
        this.logger = logger;

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

        // Registrar en el log
        if (logger != null) {
            logger.escribirDisparo(transition);
        }

        // Actualizar contadores de invariantes
        if (transition == TRANSICION_ENTRADA) contadorEntrada++;
        if (transition == TRANSICION_SALIDA)  contadorSalida++;

        // Verificar invariantes de plaza
        if (!vectorDeEstado.verificarInvariantePlazas()) {
            System.err.println("ERROR: Invariante de plaza violado despues de disparar T" + transition);
        }

        return true;
    }

    /**
     * Retorna true cuando ya se completaron los MAX_INVARIANTES invariantes de transicion.
     */
    public boolean isFinalizada() {
        return contadorSalida >= MAX_INVARIANTES;
    }

    /**
     * Retorna true si la transicion de entrada (T0) debe bloquearse porque ya se
     * admitieron tantas transacciones como invariantes maximos permitidos.
     */
    public boolean bloquearTransicion(int transition) {
        return transition == TRANSICION_ENTRADA && contadorEntrada >= MAX_INVARIANTES;
    }

    public int getCantidadTransiciones() {
        return vectorSensibilizadas.getCantidad();
    }

    public boolean estaSensibilizadoPeroAntes(int transition) {
        return vectorSensibilizadas.estaSensibilizadoPeroAntes(transition);
    }

    public long tiempoRestante(int transition) {
        return vectorSensibilizadas.tiempoRestante(transition);
    }

    public void setEsperando(int transition, boolean state) {
        vectorSensibilizadas.getTiempo(transition).setEsperando(state);
    }

    public boolean[] getSensibilizadasPorMarcado() {
        int cant = vectorSensibilizadas.getCantidad();
        boolean[] res = new boolean[cant];
        for (int i = 0; i < cant; i++) {
            res[i] = vectorSensibilizadas.sensibilizadaPorMarcado(i);
        }
        return res;
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
