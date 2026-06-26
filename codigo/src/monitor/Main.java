package monitor;

/**
 * Clase principal que inicializa y ejecuta el sistema de procesamiento
 * de transacciones de pago modelado con Red de Petri.
 *
 * Configura la politica de conflictos y arranca los hilos.
 * La estructura de la red (matriz de incidencia, marcado inicial y tiempos)
 * esta definida internamente en la clase RdP.
 */
public class Main {

        // Total de invariantes: ver RdP.MAX_INVARIANTES

        // Transiciones: T0, T1, T2, T3, T4, T5, T6, T7, T8, T9
        // Plazas: P0, P1, P2, P3, P4, P5, P6, P7, P8, P9

        public static void main(String[] args) {

                // ====================================================================
                // 1. CREAR RED DE PETRI Y LOGGER
                // ====================================================================

                Logger logger = new Logger("log_disparos.txt");
                RdP rdp = new RdP(logger);

                // ====================================================================
                // 2. CREAR POLITICA
                // ====================================================================
                // Descomentar una u otra para probar:

                // Politica politica = new PoliticaAleatoria("Politica Aleatoria");
                Politica politica = new PoliticaPriorizada("Politica Priorizada", new int[] { 4, 5 }); // prioriza alto
                                                                                                       // riesgo

                // ====================================================================
                // 3. CREAR MONITOR
                // ====================================================================

                Monitor monitor = new Monitor(rdp, politica);

                // ====================================================================
                // 4. CREAR E INICIAR HILOS
                // ====================================================================
                // Asignacion de transiciones segun invariantes de transicion:
                // T-Inv 1: T0, T1, T2, T3, T9 (flujo tarjeta)
                // T-Inv 2: T0, T4, T5, T9 (flujo alto riesgo)
                // T-Inv 3: T0, T6, T7, T8, T9 (flujo transferencia)
                //
                // Conflicto en T0 (admision): un hilo generador se encarga de T0.
                // T9 es el join final: un hilo de salida se encarga de T9.
                // Cada flujo tiene su hilo dedicado para sus transiciones post-conflicto.

                // Todos los hilos usan la misma clase generica HiloBase; lo que los
                // diferencia es el conjunto de transiciones que tienen asignado.
                Thread hiloGenerador = new Thread(
                                new HiloBase(monitor, new int[] { 0 }), "HiloGenerador");

                Thread hiloTarjetas = new Thread(
                                new HiloBase(monitor, new int[] { 1, 2, 3 }), "HiloTarjetas");

                Thread hiloAltoRiesgo = new Thread(
                                new HiloBase(monitor, new int[] { 4, 5 }), "HiloAltoRiesgo");

                Thread hiloTransferencias = new Thread(
                                new HiloBase(monitor, new int[] { 6, 7, 8 }), "HiloTransferencias");

                // Hilo de salida que dispara T9 (deposita en buffer de salida)
                Thread hiloSalida = new Thread(
                                new HiloBase(monitor, new int[] { 9 }), "HiloSalida");

                // Registrar tiempo de inicio
                long tiempoInicio = System.currentTimeMillis();

                // Arrancar todos los hilos
                hiloGenerador.start();
                hiloTarjetas.start();
                hiloAltoRiesgo.start();
                hiloTransferencias.start();
                hiloSalida.start();

                // ====================================================================
                // 5. ESPERAR FINALIZACION
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
                // 6. REPORTES FINALES
                // ====================================================================

                long tiempoFin = System.currentTimeMillis();
                long duracion = tiempoFin - tiempoInicio;

                logger.cerrarLog();

                System.out.println("========================================");
                System.out.println("  EJECUCION FINALIZADA");
                System.out.println("========================================");
                System.out.println("Invariantes completados: " + RdP.MAX_INVARIANTES);
                System.out.println("Politica utilizada: " + politica.getName());
                System.out.println("Tiempo de ejecucion: " + duracion + " ms");
                System.out.println("Log guardado en: log_disparos.txt");
                System.out.println("========================================");
        }
}
