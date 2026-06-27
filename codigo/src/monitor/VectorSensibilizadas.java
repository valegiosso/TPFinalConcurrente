package monitor;

/**
 * mantiene el estado de sensibilizacion de todas las transiciones de la red.
 * una transicion esta sensibilizada si todas sus plazas de entrada
 * tienen suficientes tokens
 */
public class VectorSensibilizadas {

    private boolean[] sensibilizadas;
    private SensibilizadoConTiempo[] tiempos;

    /**
     * @param cantidadTransiciones numero total de transiciones en la red
     * @param tiempos              arreglo con la semantica temporal de cada
     *                             transicion
     *                             para transiciones inmediatas, usar alfa=0 y
     *                             beta=0
     */
    public VectorSensibilizadas(int cantidadTransiciones, SensibilizadoConTiempo[] tiempos) {
        this.sensibilizadas = new boolean[cantidadTransiciones];
        this.tiempos = tiempos;
    }

    /**
     * retorna true si la transicion esta sensibilizada por marcado y ademas cumple
     * la ventana temporal
     */
    public boolean estaSensibilizado(int transition) {
        if (!sensibilizadas[transition]) {
            return false;
        }

        SensibilizadoConTiempo st = tiempos[transition];
        if (st.testVentanaTiempo()) {
            // regla del diagrama: si esperando == true, falla (k=false)
            // esto evita que un hilo "robe" la transicion mientras otro duerme
            if (st.isEsperando()) {
                return false;
            }
            st.setNuevoTimeStamp();
            return true;
        }
        return false;
    }

    public boolean estaSensibilizadoPeroAntes(int transition) {
        return sensibilizadas[transition] && tiempos[transition].antesDeLaVentana();
    }

    public long tiempoRestante(int transition) {
        return tiempos[transition].tiempoRestante();
    }

    public void actualiceSensibilizadoT(int transition, boolean nuevoEstado) {
        boolean anterior = sensibilizadas[transition];
        sensibilizadas[transition] = nuevoEstado;

        // si paso de no sensibilizada a sensibilizada, marcar nuevo timestamp
        if (!anterior && nuevoEstado) {
            tiempos[transition].setNuevoTimeStamp();
        }
    }

    /**
     * Recalcula que transiciones estan sensibilizadas segun el marcado actual
     * y la matriz de incidencia. Una transicion t esta sensibilizada si para
     * toda plaza p: marcado[p] + matIncidencia[p][t] >= 0
     */
    public void update(int[] marcado, int[][] matIncidencia) {
        int cantTransiciones = sensibilizadas.length;

        for (int t = 0; t < cantTransiciones; t++) {
            boolean habilitada = true;
            for (int p = 0; p < marcado.length; p++) {
                if (marcado[p] + matIncidencia[p][t] < 0) {
                    habilitada = false;
                    break;
                }
            }
            actualiceSensibilizadoT(t, habilitada);
        }
    }

    public boolean sensibilizadaPorMarcado(int transition) {
        return sensibilizadas[transition];
    }

    public SensibilizadoConTiempo getTiempo(int transition) {
        return tiempos[transition];
    }

    public int getCantidad() {
        return sensibilizadas.length;
    }
}
