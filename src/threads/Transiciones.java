package main.threads;
import main.monitor.Monitor;
import main.politicas.PoliticaInterface;

import java.util.Random;

public class Transiciones extends Thread {
    Monitor monitor;
    private final Random rand = new Random();
    private int[] invarianteT;

    public Transiciones(Monitor m, int[] t) {
        monitor = m;
        invarianteT = t;
    }

    @Override
    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            for(int transicion : invarianteT) {
                boolean disparo = monitor.fireTransition(transicion);
                if (disparo) {
                    System.out.println("Transicion " +  transicion + " disparada por " + Thread.currentThread().getName());
                } else {
                    System.out.println("Transicion " +  transicion + " no disparada por " + Thread.currentThread().getName());
                }
            }
        }
    }
}
