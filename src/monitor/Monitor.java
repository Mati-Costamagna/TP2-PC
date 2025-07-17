package main.monitor;

import main.politicas.PoliticaInterface;
import main.red.RdP;

import java.util.Arrays;
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

    private boolean[] disponibles() { // ROTO
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
        try {
            mutex.acquire();
            System.out.println(Thread.currentThread().getName() + " ha entrado al monitor");
        } catch (InterruptedException e) {
            System.out.println("El hilo " + Thread.currentThread().getName() + " se interrumpio en el monitor");
            System.exit(1);
        }
    }

    private void enviarAColaCondicion(int transition) {
        try {
            colaCondicion[transition].acquire();
        } catch (Exception e) {
            System.out.println("Error al enviar a la cola de condicion la transicion " + transition);
        }
    }

    @Override
    public boolean fireTransition(int transition) {
            colaDeEntrada();
            if(!sensibilizadas()[transition]) {
                System.out.println(Thread.currentThread().getName() + " no puede disparar la transicion " + transition + " porque no esta sensibilizada.");
                mutex.release();
                return false;
            }else {
                boolean k = true;
                while (k) {
                    k = red.disparar(transition);
                    if (k) {
                        System.out.println(Thread.currentThread().getName() + " ha disparado la transicion " + transition);
                        if (hayDisponibles()) {
                            int candidato = politica.elegirTransicion(disponibles());
                            System.out.println("Transicion candidata elegida: " + candidato + ". Activando hilo en cola de condicion");
                            colaCondicion[candidato].release();
                            return true;
                        } else {
                            k = false;
                        }
                    } else {
                        System.out.println(Thread.currentThread().getName() + " ha salido del monitor. La transicion " + transition + " no se ha disparado.");
                        mutex.release();
                        System.out.println("Transicion " + transition + " no disparada, yendo a cola de condicion");
                        enviarAColaCondicion(transition);
                    }
                }
                System.out.println(Thread.currentThread().getName() + " ha salido del monitor");
                mutex.release();
                return true;
            }
    }
}
