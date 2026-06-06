package monitor;

/**
 * Mantiene el estado de sensibilizacion de todas las transiciones de la red.
 * Una transicion esta sensibilizada si todas sus plazas de entrada
 * tienen suficientes tokens (M >= Pre(:,t) componente a componente).
 */
public class VectorSensibilizadas {

    private boolean[] sensibilizadas;
    private SensibilizadoConTiempo[] tiempos;

    /**
     * @param cantidadTransiciones numero total de transiciones en la red
     * @param tiempos arreglo con la semantica temporal de cada transicion.
     *                Para transiciones inmediatas, usar alfa=0 y beta=0.
     */
    public VectorSensibilizadas(int cantidadTransiciones, SensibilizadoConTiempo[] tiempos) {
        this.sensibilizadas = new boolean[cantidadTransiciones];
        this.tiempos = tiempos;
    }

    /**
     * Retorna true si la transicion esta sensibilizada por marcado
     * Y ademas cumple la ventana temporal.
     */
    public boolean estaSensibilizado(int transition) {
        if (!sensibilizadas[transition]) {
            return false;
        }
        return tiempos[transition].testVentanaTiempo();
    }

    /**
     * Retorna true si la transicion esta sensibilizada por marcado
     * pero esta ANTES de su ventana temporal (hay que esperar).
     */
    public boolean estaSensibilizadoPeroAntes(int transition) {
        return sensibilizadas[transition] && tiempos[transition].antesDeLaVentana();
    }

    /**
     * Retorna el tiempo restante en ms para que se abra la ventana.
     */
    public long tiempoRestante(int transition) {
        return tiempos[transition].tiempoRestante();
    }

    /**
     * Actualiza el timestamp de la transicion cuando cambia su estado de sensibilizacion.
     */
    public void actualiceSensibilizadoT(int transition, boolean nuevoEstado) {
        boolean anterior = sensibilizadas[transition];
        sensibilizadas[transition] = nuevoEstado;

        // Si paso de no sensibilizada a sensibilizada, marcar nuevo timestamp
        if (!anterior && nuevoEstado) {
            tiempos[transition].setNuevoTimeStamp();
        }
    }

    /**
     * Recalcula que transiciones estan sensibilizadas segun el marcado actual
     * y la matriz Pre. Una transicion t esta sensibilizada si para toda plaza p:
     *   marcado[p] >= matrizPre[p][t]
     */
    public void update(VectorDeEstado estado, Matrizi matrizPre) {
        int cantTransiciones = sensibilizadas.length;
        int[] marcado = estado.getMarcado();

        for (int t = 0; t < cantTransiciones; t++) {
            int[] columnaPre = matrizPre.getColumna(t);
            boolean habilitada = true;
            for (int p = 0; p < marcado.length; p++) {
                if (marcado[p] < columnaPre[p]) {
                    habilitada = false;
                    break;
                }
            }
            actualiceSensibilizadoT(t, habilitada);
        }
    }

    /**
     * Retorna si la transicion esta sensibilizada solo por marcado (sin considerar tiempo).
     */
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
