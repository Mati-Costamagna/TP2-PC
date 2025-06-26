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
            disponiblesParaDisparar[i] = red.getTransicionesSensibilizadas()[i] && (colaCondicion[i].availablePermits() == 0);
        }
        return  disponiblesParaDisparar;
    }

    private boolean hayDisponibles(boolean[] disponibles) {
        boolean hayDisponibles = false;
        for (boolean c : disponibles) {
            if (c) {
                hayDisponibles = true;
                break;
            }
        }
        return hayDisponibles;
    }

    @Override
    public boolean fireTransition(int transition) {
            try {
                mutex.acquire();
                boolean kMonitor = true;
                while (kMonitor) {
                    if (red.disparar(transition)) {
                        boolean[] disponiblesParaDisparar = disponibles();
                        if (hayDisponibles(disponiblesParaDisparar)) {
                            int candidato = politica.elegirTransicion(disponiblesParaDisparar);
                            colaCondicion[candidato].release(); //activo hilo candidato
                            kMonitor = false;
                        }else {
                            kMonitor = true;
                        }
                    } else {
                        mutex.release(); //doy permiso para que entre otro
                        colaCondicion[transition].acquire(); // Espera aqui hasta que la transicion este sensibilizada
                        //mutex.acquire();
                        //kMonitor = true;
                    }
                }
                mutex.release();
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
    }
}