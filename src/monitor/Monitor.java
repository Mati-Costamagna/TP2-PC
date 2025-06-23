package main.monitor;


import main.politicas.PoliticaInterface;
import main.red.RdP;
import main.threads.Transiciones;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private RdP red;
    private boolean[] sensibilizadas;
    private boolean[] enCola;
    private boolean[] disp;
    private int transicion;
    private Semaphore mutex;
    private boolean k;
    private Semaphore[] cola;
    private PoliticaInterface politica;

    public Monitor(RdP r, PoliticaInterface p) {
        red = r;
        mutex =  new Semaphore(1);
        cola = new Semaphore[r.getTransicionesSensibilizadas().length];
        for(int i = 0; i < r.getTransicionesSensibilizadas().length; i++){
            cola[i] = new Semaphore(1);
        }
        politica = p;
    }

    private boolean[] quienesEstan(){
        for(int i = 0; i < cola.length; i++){
            enCola[i] = cola[i].availablePermits() == 0;
        }
        return enCola;
    }

    private boolean disponibles() {
        for (int i = 0; i < sensibilizadas.length; i++) {
            disp[i] = sensibilizadas[i] && enCola[i] ;
        }
        for (boolean b : disp) {
            if (b) return true;
        }
        return false;
    }

    @Override
    public synchronized boolean fireTransition(int t) {
        try{
            mutex.acquire();
            k = true;
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        while(k) {
            k = red.disparar(t);
            if (k) {       //k == true
                sensibilizadas = red.getTransicionesSensibilizadas();
                enCola = quienesEstan();
                if(disponibles()) {
                    transicion = politica.elegirTransicion(disp);
                    cola[transicion].release();
                    Thread.currentThread().notify();
                }
                else{
                    k = false;
                }
            } else {    //k == false
                mutex.release();
                try {
                    cola[t].acquire();
                    Thread.currentThread().wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        }
        mutex.release();
        return true;
    }

}
