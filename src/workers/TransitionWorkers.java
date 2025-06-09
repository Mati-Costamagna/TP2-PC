package workers;

import monitor.MonitorInterface;
import politicas.Politica;
import util.Logger;
import util.TransicionesTemporales;

public class TransicionWorker extends Thread {
    private final int[] transiciones; // transiciones que este hilo puede disparar
    private final MonitorInterface monitor;
    private final Politica politica;
    private final String nombre;

    public TransicionWorker(String nombre, int[] transiciones, MonitorInterface monitor, Politica politica) {
        this.nombre = nombre;
        this.transiciones = transiciones;
        this.monitor = monitor;
        this.politica = politica;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            int transicion = politica.elegirTransicion(transiciones);

            boolean disparada = monitor.fireTransition(transicion);

            if (disparada) {
                Logger.log(nombre + " disparó T" + transicion);
                esperarSiTemporal(transicion);
            }

            // Pequeña pausa para no saturar CPU
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void esperarSiTemporal(int transicion) {
        int delay = TransicionesTemporales.getDelay(transicion);
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
