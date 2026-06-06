package monitor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Registra los disparos de transiciones en un archivo de log.
 * Formato intermedio: cada linea tiene el numero de transicion y timestamp.
 * Al final se puede parsear con regex para validar invariantes de transicion.
 */
public class Logger {

    private PrintWriter writer;
    private final String archivoPath;

    public Logger(String archivoPath) {
        this.archivoPath = archivoPath;
        try {
            this.writer = new PrintWriter(new FileWriter(archivoPath), true); // autoflush
        } catch (IOException e) {
            System.err.println("Error al crear archivo de log: " + e.getMessage());
        }
    }

    /**
     * Registra el disparo de una transicion.
     * Formato: "T<numero> <timestamp_ms>"
     */
    public synchronized void escribirDisparo(int transition) {
        if (writer != null) {
            writer.println("T" + transition + " " + System.currentTimeMillis());
        }
    }

    /**
     * Cierra el archivo de log.
     */
    public void cerrarLog() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}
