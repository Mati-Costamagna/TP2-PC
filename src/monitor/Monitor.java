package main.monitor;

import main.politicas.PoliticaInterface;
import main.red.RdP;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private final RdP red;
    private final Mutex mutex;
    private final ColaCondicion colaCondicion;
    private final PoliticaInterface politica;
    private final SensibilizadoConTiempo sensibilizadoConTiempo;

    public Monitor(RdP r, Mutex m, PoliticaInterface p, ColaCondicion c, SensibilizadoConTiempo s) {
        red = r;
        mutex = m;
        politica = p;
        colaCondicion = c;
        sensibilizadoConTiempo = s;
    }


    private boolean[] sensibilizadas(){
        return red.getTransicionesSensibilizadas();
    }

    private boolean[] disponibles() {
        boolean[] disponiblesParaDisparar = sensibilizadas().clone();
        for (int i = 0; i < disponiblesParaDisparar.length; i++) {
            disponiblesParaDisparar[i] = disponiblesParaDisparar[i] && colaCondicion.hasQueuedThreads(i);
        }
        return  disponiblesParaDisparar;
    }

    private boolean hayDisponibles() {
        for (boolean disponible : disponibles()) {
            if (disponible) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean fireTransition(int transition) {
        if((sensibilizadoConTiempo.hayAlguienLevantado() && sensibilizadoConTiempo.estaLevantado(transition))
                || !sensibilizadoConTiempo.hayAlguienLevantado()) {
            mutex.acquire();
            sensibilizadoConTiempo.entroAlMonitor(transition);
            System.out.println("Entro hilo " + Thread.currentThread().getName() + " al monitor");
            while (true) {
                boolean fired = red.disparar(transition);
                if (fired) {
                    System.out.println("Transicion " + transition + " disparada por " + Thread.currentThread().getName());
                    if (hayDisponibles()) {
                        int candidato = politica.elegirTransicion(disponibles());
                        System.out.println("Levantando " + candidato);
                        colaCondicion.sacarDeColaCondicion(candidato);
                    }
                    break;
                } else {
                    System.out.println("Hilo " + Thread.currentThread().getName() + " esperando en la cola de condicion de la transicion " + transition);
                    mutex.release();
                    if (sensibilizadoConTiempo.tieneQueDormir(transition) > 0) {
                        sensibilizadoConTiempo.dormir(transition, sensibilizadoConTiempo.tieneQueDormir(transition));
                    } else {
                        colaCondicion.enviarAColaCondicion(transition);
                        mutex.acquire();
                        System.out.println("Entro hilo " + Thread.currentThread().getName() + " al monitor");
                    }

                }
            }
            System.out.println("Sale hilo " + Thread.currentThread().getName() + " del monitor");
            mutex.release();
            return true;
        }else{
            return false;
        }
    }
}
