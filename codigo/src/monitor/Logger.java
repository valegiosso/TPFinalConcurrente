package monitor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Registra los disparos de transiciones en un archivo de log de forma
 * asincrónica.
 */
public class Logger implements Runnable {

    private final BlockingQueue<Integer> queue;
    private PrintWriter writer;

    public Logger(String archivoPath) {

        this.queue = new LinkedBlockingQueue<>();
        try {
            this.writer = new PrintWriter(new FileWriter(archivoPath), true);
        } catch (IOException e) {
            System.err.println("Error al crear archivo de log: " + e.getMessage());
        }
    }

    // No requiere synchronized porque queue.offer() es thread-safe y no bloqueante
    public void escribirDisparo(int transition) {
        queue.offer(transition);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Se bloquea de forma eficiente hasta que haya un disparo en la cola
                int transition = queue.take();
                if (writer != null) {
                    writer.println("T" + transition + " " + System.currentTimeMillis());
                }
            } catch (InterruptedException e) {
                // Salir del bucle al ser interrumpido
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Vaciar lo que quede en la cola antes de cerrar el archivo
        while (!queue.isEmpty()) {
            Integer transition = queue.poll();
            if (transition != null && writer != null) {
                writer.println("T" + transition + " " + System.currentTimeMillis());
            }
        }

        cerrarLog();
        System.out.println("[Logger]: Hilo de log finalizado ordenadamente.");
    }

    private void cerrarLog() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}
