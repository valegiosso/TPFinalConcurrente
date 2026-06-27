package monitor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * registra los disparos de transiciones en un archivo de log
 */
public class Logger {

    private PrintWriter writer;

    public Logger(String archivoPath) {
        try {
            this.writer = new PrintWriter(new FileWriter(archivoPath), true);
        } catch (IOException e) {
            System.err.println("Error al crear archivo de log: " + e.getMessage());
        }
    }

    public synchronized void escribirDisparo(int transition) {
        if (writer != null) {
            writer.println("T" + transition + " " + System.currentTimeMillis());
        }
    }

    public void cerrarLog() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}
