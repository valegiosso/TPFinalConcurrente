package monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Politica aleatoria: selecciona al azar entre las transiciones
 * habilitadas que tienen hilos esperando.
 */
public class PoliticaAleatoria implements Politica {

    private final String name;
    private final Random random = new Random();

    public PoliticaAleatoria(String name) {
        this.name = name;
    }

    @Override
    public int decidirTransicion(boolean[] m) {
        List<Integer> candidatas = new ArrayList<>();

        for (int i = 0; i < m.length; i++) {
            if (m[i]) {
                candidatas.add(i);
            }
        }

        if (candidatas.isEmpty()) {
            return -1;
        }

        return candidatas.get(random.nextInt(candidatas.size()));
    }
    @Override
    public String getName() {
        return name;
    }
}
