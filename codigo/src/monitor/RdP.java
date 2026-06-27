package monitor;

/**
 * Define internamente la estructura de la red (matriz, marcado inicial,
 * tiempos de transicion) y maneja los contadores de invariantes de
 * transicion para saber cuando detener la ejecucion.
 */
public class RdP {

    private static final int TRANSICION_ENTRADA = 0;
    private static final int TRANSICION_SALIDA = 9;
    public static final int MAX_INVARIANTES = 200;

    private int contadorEntrada = 0;
    private int contadorSalida = 0;

    private final int[][] matIncidencia;
    private final int[] marcadoActual;
    private final VectorSensibilizadas vectorSensibilizadas;
    private final Logger logger;

    public RdP(Logger logger) {
        this.logger = logger;

        // Matriz de incidencia W = Post - Pre (datos extraidos de PIPE)

        this.matIncidencia = new int[][] {
                /* P0 */ { -1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
                /* P1 */ { 1, -1, 0, 0, -1, 0, -1, 0, 0, 0 },
                /* P2 */ { 0, 1, -1, 0, 0, 0, 0, 0, 0, 0 },
                /* P3 */ { 0, 0, 1, -1, 0, 0, 0, 0, 0, 0 },
                /* P4 */ { 0, 0, 0, 0, 1, -1, 0, 0, 0, 0 },
                /* P5 */ { 0, 0, 0, 0, 0, 0, 1, -1, 0, 0 },
                /* P6 */ { 0, 0, 0, 0, 0, 0, 0, 1, -1, 0 },
                /* P7 */ { 0, -1, 0, 1, -1, 1, 0, 0, 0, 0 },
                /* P8 */ { 0, 0, 0, 0, -1, 1, -1, 0, 1, 0 },
                /* P9 */ { 0, 0, 0, 1, 0, 1, 0, 0, 1, -1 },
        };

        this.marcadoActual = new int[] { 3, 0, 0, 0, 0, 0, 0, 1, 1, 0 };

        // Tiempos de transicion [alfa, beta] en milisegundos
        // Transiciones inmediatas: [0, 0]
        int cantTransiciones = 10;
        SensibilizadoConTiempo[] tiempos = new SensibilizadoConTiempo[cantTransiciones];
        tiempos[0] = new SensibilizadoConTiempo(0, 0);
        tiempos[1] = new SensibilizadoConTiempo(0, 0);
        tiempos[2] = new SensibilizadoConTiempo(100, Long.MAX_VALUE);
        tiempos[3] = new SensibilizadoConTiempo(100, Long.MAX_VALUE);
        tiempos[4] = new SensibilizadoConTiempo(0, 0);
        tiempos[5] = new SensibilizadoConTiempo(150, Long.MAX_VALUE);
        tiempos[6] = new SensibilizadoConTiempo(0, 0);
        tiempos[7] = new SensibilizadoConTiempo(120, Long.MAX_VALUE);
        tiempos[8] = new SensibilizadoConTiempo(120, Long.MAX_VALUE);
        tiempos[9] = new SensibilizadoConTiempo(0, 0);

        this.vectorSensibilizadas = new VectorSensibilizadas(cantTransiciones, tiempos);
        this.vectorSensibilizadas.update(this.marcadoActual, this.matIncidencia);
    }

    /**
     * Intenta disparar la transicion indicada.
     *
     * Retorna:
     * true -> la transicion se disparo exitosamente, el marcado cambio
     * false -> la transicion no estaba sensibilizada o no cumplia la ventana
     * temporal
     */
    public boolean disparar(int transition) {
        if (!vectorSensibilizadas.estaSensibilizado(transition)) {
            return false;
        }

        // ejecutar el disparo: M = M + I(:,transition)
        for (int i = 0; i < marcadoActual.length; i++) {
            marcadoActual[i] = marcadoActual[i] + matIncidencia[i][transition];
        }

        // resetear el estado "esperando" de la transicion disparada
        vectorSensibilizadas.getTiempo(transition).resetEsperando();

        // recalcular sensibilizacion para todas las transiciones
        vectorSensibilizadas.update(marcadoActual, matIncidencia);

        // registrar en el log
        if (logger != null) {
            logger.escribirDisparo(transition);
        }

        // actualizar contadores de invariantes
        if (transition == TRANSICION_ENTRADA)
            contadorEntrada++;
        if (transition == TRANSICION_SALIDA)
            contadorSalida++;

        // verificar invariantes de plaza
        if (!verificarInvariantePlazas()) {
            System.err.println("ERROR: Invariante de plaza violado despues de disparar T" + transition);
        }

        return true;
    }

    public boolean verificarInvariantePlazas() {
        int inv1 = marcadoActual[2] + marcadoActual[3] + marcadoActual[4] + marcadoActual[7];
        int inv2 = marcadoActual[4] + marcadoActual[5] + marcadoActual[6] + marcadoActual[8];
        int inv3 = marcadoActual[0] + marcadoActual[1] + marcadoActual[2] + marcadoActual[3]
                + marcadoActual[4] + marcadoActual[5] + marcadoActual[6] + marcadoActual[9];

        return (inv1 == 1) && (inv2 == 1) && (inv3 == 3);
    }

    public boolean isFinalizada() {
        return contadorSalida >= MAX_INVARIANTES;
    }

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

    public int[] getMarcadoActual() {
        return marcadoActual;
    }
}
