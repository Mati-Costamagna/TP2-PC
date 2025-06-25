package main.monitor;

import main.politicas.PoliticaInterface;
import main.red.RdP;

import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private final RdP red;
    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore[] colaCondicion;
    private final PoliticaInterface politica;

    public Monitor(RdP r, PoliticaInterface p) {
        red = r;
        politica = p;
        colaCondicion = new Semaphore[r.getTransicionesSensibilizadas().length];
        for (int i = 0; i < r.getTransicionesSensibilizadas().length; i++) {
            colaCondicion[i] = new Semaphore(0);
        }
    }

    private boolean[] disponibles() {
        boolean[] disponiblesParaDisparar = new boolean[red.getTransicionesSensibilizadas().length];
        for (int i = 0; i < red.getTransicionesSensibilizadas().length; i++) {
            disponiblesParaDisparar[i] = red.getTransicionesSensibilizadas()[i] && colaCondicion[i].hasQueuedThreads();
        }
        return  disponiblesParaDisparar;
    }

    private boolean hayDisponibles() {
        boolean hayDisponibles = false;
        for (boolean c : disponibles()) {
            if (c) {
                hayDisponibles = true;
                break;
            }
        }
        return hayDisponibles;
    }

    @Override
    public boolean fireTransition(int transition) {
        boolean kMonitor = false;
        while (!kMonitor) {
            try {
                mutex.acquire();
                boolean kRed = red.estaSensibilizado(transition);
                if (kRed) {
                    kMonitor = red.disparar(transition);
                    if(kMonitor){
                        if(hayDisponibles()){
                            int candidato = politica.elegirTransicion(disponibles());
                            red.setLiberado(candidato);
                            colaCondicion[candidato].release();
                        }else{
                            kMonitor = false;
                        }
                    } else {
                        mutex.release();
                        colaCondicion[transition].acquire(); // Espera aqui hasta que la transicion este sensibilizada
                    }
                } else{
                    mutex.release();
                    red.dormir();
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