package monitor;

/**
 * Representa la Red de Petri con su matriz de incidencia (W = Post - Pre),
 * el vector de estado (marcado) y el vector de sensibilizadas.
 *
 * Define internamente la estructura de la red (matriz, marcado inicial,
 * tiempos de transicion) y maneja los contadores de invariantes de
 * transicion para saber cuando detener la ejecucion.
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

    private final int[][] matIncidencia;
    private final int[] marcadoActual;
    private final VectorSensibilizadas vectorSensibilizadas;
    private final Logger logger;

    public RdP(Logger logger) {
        this.logger = logger;

        // Matriz de incidencia W = Post - Pre (datos extraidos de PIPE/bitacora)
        //                    T0  T1  T2  T3  T4  T5  T6  T7  T8  T9
        this.matIncidencia = new int[][] {
            /* P0 */ {  -1,  0,  0,  0,  0,  0,  0,  0,  0,  1 },
            /* P1 */ {   1, -1,  0,  0, -1,  0, -1,  0,  0,  0 },
            /* P2 */ {   0,  1, -1,  0,  0,  0,  0,  0,  0,  0 },
            /* P3 */ {   0,  0,  1, -1,  0,  0,  0,  0,  0,  0 },
            /* P4 */ {   0,  0,  0,  0,  1, -1,  0,  0,  0,  0 },
            /* P5 */ {   0,  0,  0,  0,  0,  0,  1, -1,  0,  0 },
            /* P6 */ {   0,  0,  0,  0,  0,  0,  0,  1, -1,  0 },
            /* P7 */ {   0, -1,  0,  1, -1,  1,  0,  0,  0,  0 },
            /* P8 */ {   0,  0,  0,  0, -1,  1, -1,  0,  1,  0 },
            /* P9 */ {   0,  0,  0,  1,  0,  1,  0,  0,  1, -1 },
        };

        // Marcado inicial
        //                  P0  P1  P2  P3  P4  P5  P6  P7  P8  P9
        this.marcadoActual = new int[] { 3, 0, 0, 0, 0, 0, 0, 1, 1, 0 };

        // Tiempos de transicion [alfa, beta] en milisegundos
        // Transiciones inmediatas: [0, 0]
        int cantTransiciones = 10;
        SensibilizadoConTiempo[] tiempos = new SensibilizadoConTiempo[cantTransiciones];
        tiempos[0] = new SensibilizadoConTiempo(0, 0);                   // T0: inmediata
        tiempos[1] = new SensibilizadoConTiempo(0, 0);                   // T1: inmediata
        tiempos[2] = new SensibilizadoConTiempo(100, Long.MAX_VALUE);    // T2: [100ms, inf]
        tiempos[3] = new SensibilizadoConTiempo(100, Long.MAX_VALUE);    // T3: [100ms, inf]
        tiempos[4] = new SensibilizadoConTiempo(0, 0);                   // T4: inmediata
        tiempos[5] = new SensibilizadoConTiempo(150, Long.MAX_VALUE);    // T5: [150ms, inf]
        tiempos[6] = new SensibilizadoConTiempo(0, 0);                   // T6: inmediata
        tiempos[7] = new SensibilizadoConTiempo(120, Long.MAX_VALUE);    // T7: [120ms, inf]
        tiempos[8] = new SensibilizadoConTiempo(120, Long.MAX_VALUE);    // T8: [120ms, inf]
        tiempos[9] = new SensibilizadoConTiempo(0, 0);                   // T9: inmediata

        this.vectorSensibilizadas = new VectorSensibilizadas(cantTransiciones, tiempos);

        // Calcular sensibilizacion inicial
        this.vectorSensibilizadas.update(this.marcadoActual, this.matIncidencia);
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

        // Ejecutar el disparo: M = M + W(:,t)
        for (int i = 0; i < marcadoActual.length; i++) {
            marcadoActual[i] = marcadoActual[i] + matIncidencia[i][transition];
        }

        // Resetear el estado "esperando" de la transicion disparada
        vectorSensibilizadas.getTiempo(transition).resetEsperando();

        // Recalcular sensibilizacion para todas las transiciones
        vectorSensibilizadas.update(marcadoActual, matIncidencia);

        // Registrar en el log
        if (logger != null) {
            logger.escribirDisparo(transition);
        }

        // Actualizar contadores de invariantes
        if (transition == TRANSICION_ENTRADA) contadorEntrada++;
        if (transition == TRANSICION_SALIDA)  contadorSalida++;

        // Verificar invariantes de plaza
        if (!verificarInvariantePlazas()) {
            System.err.println("ERROR: Invariante de plaza violado despues de disparar T" + transition);
        }

        return true;
    }

    /**
     * Verifica los invariantes de plaza de la red.
     * Ecuaciones extraidas del analisis PIPE:
     *   M(P2) + M(P3) + M(P4) + M(P7) = 1
     *   M(P4) + M(P5) + M(P6) + M(P8) = 1
     *   M(P0) + M(P1) + M(P2) + M(P3) + M(P4) + M(P5) + M(P6) + M(P9) = 3
     */
    public boolean verificarInvariantePlazas() {
        int inv1 = marcadoActual[2] + marcadoActual[3] + marcadoActual[4] + marcadoActual[7];
        int inv2 = marcadoActual[4] + marcadoActual[5] + marcadoActual[6] + marcadoActual[8];
        int inv3 = marcadoActual[0] + marcadoActual[1] + marcadoActual[2] + marcadoActual[3]
                 + marcadoActual[4] + marcadoActual[5] + marcadoActual[6] + marcadoActual[9];

        return (inv1 == 1) && (inv2 == 1) && (inv3 == 3);
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

    public int[] getMarcadoActual() {
        return marcadoActual;
    }
}
