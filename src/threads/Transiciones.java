package main.threads;
import main.monitor.Monitor;
import main.monitor.SensibilizadoConTiempo;

public class Transiciones extends Thread {
    private final Monitor monitor;
    private final int[] transiciones;
    private final Logger logger;
    private final SensibilizadoConTiempo sensibilizadoConTiempo;

    public Transiciones(Monitor m, int[] t, Logger l, SensibilizadoConTiempo s) {
        monitor = m;
        transiciones = t;
        logger = l;
        sensibilizadoConTiempo = s;
    }

    @Override
    public void run(){
        while(!logger.isFinalizado() ){
            for(int transicion : transiciones) {
                boolean disparo = false;
                while (!disparo && !Thread.currentThread().isInterrupted()) {
                    disparo = monitor.fireTransition(transicion);
                    if (disparo) {
                        logger.logTransicion(transicion);
                    }else if (sensibilizadoConTiempo.tieneQueDormir(transicion)) {
                        sensibilizadoConTiempo.dormir(transicion);
                    }else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }
}
