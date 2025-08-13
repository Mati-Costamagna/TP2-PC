package main.threads;
import main.monitor.Monitor;
import main.monitor.SensibilizadoConTiempo;


import java.util.Random;

public class Transiciones extends Thread {
    Monitor monitor;
    private final Random rand = new Random();
    private int[] transiciones;
    private Logger logger;
    private SensibilizadoConTiempo sensibilizadoConTiempo;

    public Transiciones(Monitor m, int[] t, Logger l, SensibilizadoConTiempo s) {
        monitor = m;
        transiciones = t;
        logger = l;
        sensibilizadoConTiempo = s;
    }

    @Override
    public void run(){
        while(!logger.isFinalizado()){
            for(int transicion : transiciones) {
                boolean disparo = false;
                while (!disparo && !Thread.currentThread().isInterrupted()) {
                    disparo = monitor.fireTransition(transicion);
                    if (disparo) {
                        //System.out.println("Transicion " +  transicion + " disparada por " + Thread.currentThread().getName());
                        logger.logTransicion(transicion);
                    }else if (sensibilizadoConTiempo.tieneQueDormir(transicion)) {
                        //try { Thread.sleep(sensibilizadoConTiempo.tieneQueDormir(transicion)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        sensibilizadoConTiempo.dormir(transicion);
                    }
                    try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
        }
    }
}
