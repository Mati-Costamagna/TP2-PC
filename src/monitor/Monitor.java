package main.monitor;

import main.politicas.PoliticaInterface;
import main.red.RdP;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private final RdP red;
    private final Mutex mutex;
    private final Semaphore[] colaCondicion;
    private final PoliticaInterface politica;

    public Monitor(RdP r, Mutex m, PoliticaInterface p) {
        red = r;
        mutex = m;
        politica = p;
        colaCondicion = new Semaphore[r.getTransicionesSensibilizadas().length];
        for(int i = 0; i < r.getTransicionesSensibilizadas().length; i++){
            colaCondicion[i] = new Semaphore(0);
        }
    }

    private boolean[] quienesEstan(){
        boolean[] quienesEstan = new boolean[colaCondicion.length];
        for (int i = 0; i < colaCondicion.length; i++) {
            quienesEstan[i] = colaCondicion[i].hasQueuedThreads();
        }
        return quienesEstan;
    }

    private boolean[] sensibilizadas(){
        boolean[] sensibilizadas = new boolean[red.getTransicionesSensibilizadas().length];
        for (int i = 0; i < red.getTransicionesSensibilizadas().length; i++) {
            sensibilizadas[i] = red.getTransicionesSensibilizadas()[i];
        }
        return sensibilizadas;
    }

    private boolean[] disponibles() {
        boolean[] disponiblesParaDisparar = new boolean[sensibilizadas().length];
        for (int i = 0; i < sensibilizadas().length; i++) {
            disponiblesParaDisparar[i] = sensibilizadas()[i] && quienesEstan()[i];
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

    private void colaDeEntrada(){
        mutex.acquire();
    }

    private void enviarAColaCondicion(int transition) throws InterruptedException{
        colaCondicion[transition].acquire();
    }

    private void sacarDeColaCondicion(int transition) {
        colaCondicion[transition].release();
    }

    private synchronized void despertarCandidato() {
        boolean[] disponibles = disponibles();
        int candidato = politica.elegirTransicion(disponibles);

        if (candidato != -1) {
            sacarDeColaCondicion(candidato);
        }
    }

    @Override
    public boolean fireTransition(int transition) {
        colaDeEntrada();
        //System.out.println("Entro hilo " + Thread.currentThread().getName() + " al monitor");
        while (true) {
            boolean fired = red.disparar(transition);
            if (fired) {
                //System.out.println("Transicion " + transition + " disparada por " + Thread.currentThread().getName());
                despertarCandidato();
                mutex.release();
                //System.out.println("Hilo " + Thread.currentThread().getName() + " saliendo del monitor");
                return true;
            } else {
                //System.out.println("Hilo " + Thread.currentThread().getName() + " esperando en la cola de condicion de la transicion " + transition);
                mutex.release();
                try {
                    enviarAColaCondicion(transition);
                }catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    return false;
                }
                colaDeEntrada(); // Re-acquire mutex after being woken up
            }
        }
    }
}
