package main.threads;
import main.monitor.Monitor;


import java.util.Random;

public class Transiciones extends Thread {
    Monitor monitor;
    private final Random rand = new Random();
    private int[] transiciones;
    private Logger logger;

    public Transiciones(Monitor m, int[] t, Logger l) {
        monitor = m;
        transiciones = t;
        logger = l;
    }

    @Override
    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            for(int transicion : transiciones) {
                boolean disparo = monitor.fireTransition(transicion);
                if (disparo) {
                    System.out.println("Transicion " +  transicion + " disparada por " + Thread.currentThread().getName());
                    logger.logTransicion(transicion);
                } else {
                    System.out.println("Transicion " +  transicion + " no disparada por " + Thread.currentThread().getName());
                }
            }
        }
    }
}
