package main.monitor;

import java.util.concurrent.Semaphore;

public class Mutex {
    private final Semaphore mutex;

    public Mutex(){
        mutex = new Semaphore(1, true);
    }

    public void acquire(){
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            System.err.println("Mutex acquisition interrupted: " + e.getMessage());
        }
    }

    public void release(){
        mutex.release();
    }
}
