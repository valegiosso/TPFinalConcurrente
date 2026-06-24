package monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Politica priorizada: da preferencia al flujo de pago de alto riesgo
 * (transiciones T4 y T5, que usan ambos recursos P7 y P8 simultaneamente).
 *
 * Si hay transiciones del flujo de alto riesgo habilitadas con hilos esperando,
 * elige una de esas. Si no, elige aleatoriamente entre el resto.
 */
public class PoliticaPriorizada implements Politica {

    // Transiciones del flujo de alto riesgo (T4 y T5)
    // TODO: Ajustar estos indices si la red cambia
    private final int[] transicionesPrioritarias;
    private final Random random = new Random();

    public PoliticaPriorizada(int[] transicionesPrioritarias) {
        this.transicionesPrioritarias = transicionesPrioritarias;
    }

    @Override
    public int decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) {
        // Primero buscar entre las prioritarias
        List<Integer> prioritarias = new ArrayList<>();
        for (int t : transicionesPrioritarias) {
            if (t < habilitadas.length && habilitadas[t] && conHilosEsperando[t]) {
                prioritarias.add(t);
            }
        }

        if (!prioritarias.isEmpty()) {
            return prioritarias.get(random.nextInt(prioritarias.size()));
        }

        // Si no hay prioritarias, elegir aleatoriamente entre las demas
        List<Integer> candidatas = new ArrayList<>();
        for (int i = 0; i < habilitadas.length; i++) {
            if (habilitadas[i] && conHilosEsperando[i]) {
                candidatas.add(i);
            }
        }

        if (candidatas.isEmpty()) {
            return -1;
        }

        return candidatas.get(random.nextInt(candidatas.size()));
    }
}
