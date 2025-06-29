package main.monitor;

import java.util.concurrent.Semaphore;

import main.politicas.PoliticaInterface;
import main.red.RdP;

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
            disponiblesParaDisparar[i] = red.getTransicionesSensibilizadas()[i]
                    && (colaCondicion[i].getQueueLength() > 0);
        }
        return disponiblesParaDisparar;
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
        try {
            mutex.acquire();

            // Verificar si la transición está sensibilizada
            if (!red.getTransicionesSensibilizadas()[transition]) {
                return false;
            }

            // Intentar disparar la transición
            boolean disparoExitoso = red.disparar(transition);

            if (disparoExitoso) {
                // Si el disparo fue exitoso, liberar otra transición si hay disponibles
                if (hayDisponibles()) {
                    colaCondicion[politica.elegirTransicion(disponibles())].release();
                }
                return true;
            } else {
                // Si no se pudo disparar, liberar el mutex y retornar false
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }

    public boolean isTransitionWaitingForTime(int transition) {
        try {
            mutex.acquire();
            return red.getTransicionesSensibilizadas()[transition] && !red.testVentanaTiempo(transition);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }

    public long getTimeToWait(int transition) {
        try {
            mutex.acquire();
            if (red.getTransicionesSensibilizadas()[transition]) {
                return red.getTimeToWait(transition);
            }
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        } finally {
            mutex.release();
        }
    }
}
