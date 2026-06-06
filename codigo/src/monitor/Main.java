package monitor;

/**
 * Clase principal que inicializa y ejecuta el sistema de procesamiento
 * de transacciones de pago modelado con Red de Petri.
 *
 * Configura las matrices (Pre, Post), el marcado inicial, los tiempos,
 * la politica, y arranca los hilos.
 */
public class Main {

    // Total de invariantes (transacciones completas) a procesar
    private static final int MAX_INVARIANTES = 200;

    // Transiciones: T0, T1, T2, T3, T4, T5, T6, T7, T8, T9
    // Plazas:       P0, P1, P2, P3, P4, P5, P6, P7, P8, P9

    public static void main(String[] args) {

        // ====================================================================
        // 1. DEFINIR LAS MATRICES DE LA RED (datos extraidos de PIPE/bitacora)
        // ====================================================================

        // Matriz Pre (I-): lo que cada transicion CONSUME de cada plaza
        //              T0  T1  T2  T3  T4  T5  T6  T7  T8  T9
        int[][] pre = {
            /* P0 */ {  1,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /* P1 */ {  0,  1,  0,  0,  1,  0,  1,  0,  0,  0 },
            /* P2 */ {  0,  0,  1,  0,  0,  0,  0,  0,  0,  0 },
            /* P3 */ {  0,  0,  0,  1,  0,  0,  0,  0,  0,  0 },
            /* P4 */ {  0,  0,  0,  0,  0,  1,  0,  0,  0,  0 },
            /* P5 */ {  0,  0,  0,  0,  0,  0,  0,  1,  0,  0 },
            /* P6 */ {  0,  0,  0,  0,  0,  0,  0,  0,  1,  0 },
            /* P7 */ {  0,  1,  0,  0,  1,  0,  0,  0,  0,  0 },
            /* P8 */ {  0,  0,  0,  0,  1,  0,  1,  0,  0,  0 },
            /* P9 */ {  0,  0,  0,  0,  0,  0,  0,  0,  0,  1 },
        };

        // Matriz Post (I+): lo que cada transicion PRODUCE en cada plaza
        //               T0  T1  T2  T3  T4  T5  T6  T7  T8  T9
        int[][] post = {
            /* P0 */ {  0,  0,  0,  0,  0,  0,  0,  0,  0,  1 },
            /* P1 */ {  1,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
            /* P2 */ {  0,  1,  0,  0,  0,  0,  0,  0,  0,  0 },
            /* P3 */ {  0,  0,  1,  0,  0,  0,  0,  0,  0,  0 },
            /* P4 */ {  0,  0,  0,  0,  1,  0,  0,  0,  0,  0 },
            /* P5 */ {  0,  0,  0,  0,  0,  0,  1,  0,  0,  0 },
            /* P6 */ {  0,  0,  0,  0,  0,  0,  0,  1,  0,  0 },
            /* P7 */ {  0,  0,  0,  1,  0,  1,  0,  0,  0,  0 },
            /* P8 */ {  0,  0,  0,  0,  0,  1,  0,  0,  1,  0 },
            /* P9 */ {  0,  0,  0,  1,  0,  1,  0,  0,  1,  0 },
        };

        // Marcado inicial
        //              P0  P1  P2  P3  P4  P5  P6  P7  P8  P9
        int[] m0 = {     3,  0,  0,  0,  0,  0,  0,  1,  1,  0 };

        // ====================================================================
        // 2. DEFINIR TIEMPOS DE LAS TRANSICIONES TEMPORALES
        // ====================================================================
        // Transiciones temporales: T2, T3, T5, T7, T8
        // [alfa, beta] en milisegundos. Transiciones inmediatas: [0, 0]
        // TODO: Ajustar los valores para que 200 invariantes tarden entre 20-40 seg

        int cantTransiciones = 10;
        SensibilizadoConTiempo[] tiempos = new SensibilizadoConTiempo[cantTransiciones];

        tiempos[0] = new SensibilizadoConTiempo(0, 0);       // T0: inmediata
        tiempos[1] = new SensibilizadoConTiempo(0, 0);       // T1: inmediata
        tiempos[2] = new SensibilizadoConTiempo(0, 0);       // T2: temporal - TODO
        tiempos[3] = new SensibilizadoConTiempo(0, 0);       // T3: temporal - TODO
        tiempos[4] = new SensibilizadoConTiempo(0, 0);       // T4: inmediata
        tiempos[5] = new SensibilizadoConTiempo(0, 0);       // T5: temporal - TODO
        tiempos[6] = new SensibilizadoConTiempo(0, 0);       // T6: inmediata
        tiempos[7] = new SensibilizadoConTiempo(0, 0);       // T7: temporal - TODO
        tiempos[8] = new SensibilizadoConTiempo(0, 0);       // T8: temporal - TODO
        tiempos[9] = new SensibilizadoConTiempo(0, 0);       // T9: inmediata

        // ====================================================================
        // 3. CREAR COMPONENTES DE LA RED
        // ====================================================================

        Matrizi matrizPre = new Matrizi(pre);
        Matrizi matrizPost = new Matrizi(post);
        VectorDeEstado estadoInicial = new VectorDeEstado(m0);
        VectorSensibilizadas vectorSensibilizadas = new VectorSensibilizadas(cantTransiciones, tiempos);

        RdP rdp = new RdP(matrizPre, matrizPost, estadoInicial, vectorSensibilizadas);

        // ====================================================================
        // 4. CREAR POLITICA
        // ====================================================================
        // Descomentar una u otra para probar:

        Politica politica = new PoliticaAleatoria();
        // Politica politica = new PoliticaPriorizada(new int[]{4, 5}); // prioriza alto riesgo

        // ====================================================================
        // 5. CREAR MONITOR Y LOGGER
        // ====================================================================

        Logger logger = new Logger("log_disparos.txt");

        // T9 es la transicion de salida (deposita en P9 y luego vuelve token a P0)
        Monitor monitor = new Monitor(rdp, politica, logger, MAX_INVARIANTES, 9);

        // ====================================================================
        // 6. CREAR E INICIAR HILOS
        // ====================================================================
        // Asignacion de transiciones segun invariantes de transicion:
        //   T-Inv 1: T0, T1, T2, T3, T9 (flujo tarjeta)
        //   T-Inv 2: T0, T4, T5, T9      (flujo alto riesgo)
        //   T-Inv 3: T0, T6, T7, T8, T9  (flujo transferencia)
        //
        // Conflicto en T0 (admision): un hilo generador se encarga de T0.
        // T9 es el join final: un hilo de salida se encarga de T9.
        // Cada flujo tiene su hilo dedicado para sus transiciones post-conflicto.

        Thread hiloGenerador = new Thread(
            new HiloGenerador(monitor, new int[]{0}), "HiloGenerador");

        Thread hiloTarjetas = new Thread(
            new HiloProcesadorTarjetas(monitor, new int[]{1, 2, 3}), "HiloTarjetas");

        Thread hiloAltoRiesgo = new Thread(
            new HiloProcesadorAltoRiesgo(monitor, new int[]{4, 5}), "HiloAltoRiesgo");

        Thread hiloTransferencias = new Thread(
            new HiloProcesadorTransferencias(monitor, new int[]{6, 7, 8}), "HiloTransferencias");

        // Hilo de salida que dispara T9 (deposita en buffer de salida)
        Thread hiloSalida = new Thread(
            new HiloGenerador(monitor, new int[]{9}), "HiloSalida");

        // Registrar tiempo de inicio
        long tiempoInicio = System.currentTimeMillis();

        // Arrancar todos los hilos
        hiloGenerador.start();
        hiloTarjetas.start();
        hiloAltoRiesgo.start();
        hiloTransferencias.start();
        hiloSalida.start();

        // ====================================================================
        // 7. ESPERAR FINALIZACION
        // ====================================================================

        try {
            hiloGenerador.join();
            hiloTarjetas.join();
            hiloAltoRiesgo.join();
            hiloTransferencias.join();
            hiloSalida.join();
        } catch (InterruptedException e) {
            System.err.println("Error esperando finalizacion de hilos: " + e.getMessage());
        }

        // ====================================================================
        // 8. REPORTES FINALES
        // ====================================================================

        long tiempoFin = System.currentTimeMillis();
        long duracion = tiempoFin - tiempoInicio;

        logger.cerrarLog();

        System.out.println("========================================");
        System.out.println("  EJECUCION FINALIZADA");
        System.out.println("========================================");
        System.out.println("Invariantes completados: " + monitor.getContadorInvariantes());
        System.out.println("Tiempo de ejecucion: " + duracion + " ms");
        System.out.println("Log guardado en: log_disparos.txt");
        System.out.println("========================================");
    }
}
