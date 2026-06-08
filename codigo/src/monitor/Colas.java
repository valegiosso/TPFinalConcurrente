package monitor;

import java.util.concurrent.Semaphore;

public class Colas {
    private final Semaphore[] semaforos;

    public Colas(int cantidadTransiciones) {
        semaforos = new Semaphore[cantidadTransiciones];
        for (int i = 0; i < cantidadTransiciones; i++) {
            semaforos[i] = new Semaphore(0, true);
        }
    }

    public void acquire(int transition) {
        try {
            semaforos[transition].acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void release(int transition) {
        semaforos[transition].release();
    }

    public boolean[] quienesEstan() {
        boolean[] waiting = new boolean[semaforos.length];
        for (int i = 0; i < semaforos.length; i++) {
            waiting[i] = semaforos[i].hasQueuedThreads();
        }
        return waiting;
    }
}
