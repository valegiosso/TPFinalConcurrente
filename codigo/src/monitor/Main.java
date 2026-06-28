package monitor;

/**
 * inicializa y ejecuta el sistema de procesamiento de transacciones de pago
 * modelado con Red de Petri.
 * configura la politica de conflictos y arranca los hilos.
 */
public class Main {
        public static void main(String[] args) {

                // 1. CREAR RED DE PETRI Y LOGGER
                // Detecta automaticamente la ruta para guardar el log siempre dentro de la
                // carpeta codigo
                String logPath = "log_disparos.txt";
                if (new java.io.File("TPFinalConcurrente/codigo").exists()) {
                        logPath = "TPFinalConcurrente/codigo/log_disparos.txt";
                } else if (new java.io.File("codigo").exists()) {
                        logPath = "codigo/log_disparos.txt";
                }
                Logger logger = new Logger(logPath);
                RdP rdp = new RdP(logger);

                // 2. CREAR POLITICA

                // descomentar una u otra para probar:

                Politica politica = new PoliticaAleatoria("Politica Aleatoria");
                // Politica politica = new PoliticaPriorizada("Politica Priorizada", new
                // int[] { 4, 5 });
                // prioriza alto riesgo

                // 3. CREAR MONITOR

                Monitor monitor = new Monitor(rdp, politica);

                // 4. CREAR E INICIAR HILOS
                // Hilo dedicado para el Logger
                Thread hiloLogger = new Thread(logger, "HiloLogger");
                hiloLogger.start();

                // asignacion de transiciones segun invariantes de transicion:
                // todos los hilos usan la misma clase generica HiloBase; lo que los diferencia
                // es el conjunto de transiciones que tienen asignado.
                Thread hiloGenerador = new Thread(new HiloBase(monitor, new int[] { 0 }), "HiloGenerador");
                Thread hiloTarjetas = new Thread(new HiloBase(monitor, new int[] { 1, 2, 3 }), "HiloTarjetas");
                Thread hiloAltoRiesgo = new Thread(new HiloBase(monitor, new int[] { 4, 5 }), "HiloAltoRiesgo");
                Thread hiloTransferencias = new Thread(new HiloBase(monitor, new int[] { 6, 7, 8 }),
                                "HiloTransferencias");
                Thread hiloSalida = new Thread(new HiloBase(monitor, new int[] { 9 }), "HiloSalida");

                // Registrar tiempo de inicio
                long tiempoInicio = System.currentTimeMillis();

                // Arrancar todos los hilos
                hiloGenerador.start();
                hiloTarjetas.start();
                hiloAltoRiesgo.start();
                hiloTransferencias.start();
                hiloSalida.start();

                // 5. finalizacion
                try {
                        hiloGenerador.join();
                        hiloTarjetas.join();
                        hiloAltoRiesgo.join();
                        hiloTransferencias.join();
                        hiloSalida.join();
                } catch (InterruptedException e) {
                        System.err.println("Error esperando finalizacion de hilos: " + e.getMessage());
                }

                // Interrumpir y esperar la finalizacion del Logger para vaciar la cola y cerrar
                // el archivo
                hiloLogger.interrupt();
                try {
                        hiloLogger.join();
                } catch (InterruptedException e) {
                        System.err.println("Error esperando finalizacion del hilo Logger: " + e.getMessage());
                }

                long tiempoFin = System.currentTimeMillis();
                long duracion = tiempoFin - tiempoInicio;

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
