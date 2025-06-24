package main.monitor;


import main.politicas.PoliticaInterface;
import main.red.RdP;
import main.threads.Transiciones;

import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Monitor implements MonitorInterface {
    private RdP red;
    private boolean[] sensibilizadas;
    private boolean[] enCola;
    private boolean[] disp;
    private int transicion;
    private Semaphore mutex;
    private Semaphore[] cola;
    private PoliticaInterface politica;

    public Monitor(RdP r, PoliticaInterface p) {
        red = r;
        mutex =  new Semaphore(1);
        cola = new Semaphore[r.getTransicionesSensibilizadas().length];
        for(int i = 0; i < r.getTransicionesSensibilizadas().length; i++){
            cola[i] = new Semaphore(0);
        }
        politica = p;
        sensibilizadas =  new boolean[r.getTransicionesSensibilizadas().length];
        enCola = new boolean[r.getTransicionesSensibilizadas().length];
        disp = new boolean[r.getTransicionesSensibilizadas().length];
    }

    private boolean disponibles() {
        for (int i = 0; i < sensibilizadas.length; i++) {
            disp[i] = sensibilizadas[i] && cola[i].hasQueuedThreads();
        }
        for (boolean b : disp) {
            if (b) return true;
        }
        return false;
    }

    @Override
    public boolean fireTransition(int t) {
        boolean k = false;
        while (!k) {
            try {
                mutex.acquire();
                sensibilizadas = red.getTransicionesSensibilizadas();
                if (sensibilizadas[t]) {
                    k = red.disparar(t);
                    sensibilizadas = red.getTransicionesSensibilizadas();
//                     Wake up a waiting thread if any transition is now enabled
                    boolean hay = disponibles();
                    if(hay) {
                        transicion = politica.elegirTransicion(disp);
                        if (transicion != -1) {
                            cola[transicion].release();
                            break; // Wake up the thread waiting for this transition
                        }
                    }
                } else {
                    mutex.release();
                    cola[t].acquire(); // Wait until this transition is enabled
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                if (mutex.availablePermits() == 0) {
                    mutex.release();
                }
            }
        }
        return true;
    }
}
