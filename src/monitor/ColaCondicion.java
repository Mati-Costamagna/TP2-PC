package main.monitor;

import java.util.concurrent.Semaphore;

public class ColaCondicion {
    private final Semaphore[] semaphores;
    private volatile boolean terminado = false;

    public ColaCondicion(int size) {
        semaphores = new Semaphore[size];
        for (int i = 0; i < size; i++) {
            semaphores[i] = new Semaphore(0);
        }
    }

    private void liberarTodos() {
        for (int i = 0; i < semaphores.length; i++) {
            int waiting = semaphores[i].getQueueLength();
            if (waiting > 0) {
                semaphores[i].release(waiting);
            }
        }
    }

    public void setTerminado() {
        terminado = true;
        liberarTodos();
    }

    public void sacarDeColaCondicion(int index) {
        if (index >= 0 && index < semaphores.length) {
            semaphores[index].release();
        }
    }

    public void enviarAColaCondicion(int index) {
        if (index >= 0 && index < semaphores.length) {
            try{
                semaphores[index].acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.err.println("Semaphore acquisition interrupted: " + e.getMessage());
            }
        } else {
            throw new IndexOutOfBoundsException("Index out of bounds for condition queue.");
        }
    }

    public boolean hasQueuedThreads(int index) {
        if (index >= 0 && index < semaphores.length) {
            return semaphores[index].hasQueuedThreads();
        } else {
            throw new IndexOutOfBoundsException("Index out of bounds for condition queue.");
        }
    }

    public boolean isTerminado(){
        return terminado;
    }

}
