package monitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Encapsula el mecanismo de exclusion mutua y sincronizacion
 * usando ReentrantLock y un arreglo de Condition (una por transicion).
 */
public class Mutex {

    private final ReentrantLock lock;
    private final Condition[] conds;

    public Mutex(int cantidadTransiciones) {
        this.lock = new ReentrantLock(true); // fair lock
        this.conds = new Condition[cantidadTransiciones];
        for (int i = 0; i < cantidadTransiciones; i++) {
            conds[i] = lock.newCondition();
        }
    }

    public void acquire() {
        lock.lock();
    }

    public void release() {
        lock.unlock();
    }

    /**
     * El hilo se bloquea esperando en la condicion asociada a la transicion.
     */
    public void await(int transition) throws InterruptedException {
        conds[transition].await();
    }

    /**
     * Despierta a UN hilo que este esperando en la condicion de la transicion.
     */
    public void signal(int transition) {
        conds[transition].signal();
    }

    /**
     * Despierta a TODOS los hilos en TODAS las condiciones.
     * Se usa para la finalizacion limpia del sistema.
     */
    public void signalAll() {
        for (Condition cond : conds) {
            cond.signalAll();
        }
    }

    /**
     * Retorna true si hay algun hilo esperando en la condicion de la transicion.
     */
    public boolean hasWaiters(int transition) {
        return lock.hasWaiters(conds[transition]);
    }
}
