package monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Politica aleatoria: selecciona al azar entre las transiciones
 * habilitadas que tienen hilos esperando.
 */
public class PoliticaAleatoria implements Politica {

    private final Random random = new Random();

    @Override
    public int decidirTransicion(boolean[] habilitadas, boolean[] conHilosEsperando) {
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
