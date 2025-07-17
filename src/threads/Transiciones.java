package main.threads;

import java.util.Random;

import main.monitor.Monitor;

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

//    @Override
//    public void run() {
//        while (!Thread.currentThread().isInterrupted()) {
//            boolean algunaTransicionDisparada = false;
//
//            for (int transicion : transiciones) {
//                boolean disparo = monitor.fireTransition(transicion);
//                if (disparo) {
//                    System.out
//                            .println("Transicion " + transicion + " disparada por " + Thread.currentThread().getName());
//                    logger.logTransicion(transicion);
//                    algunaTransicionDisparada = true;
//                } else {
//                    // Verificar si está esperando por tiempo
//                    if (monitor.isTransitionWaitingForTime(transicion)) {
//                        long tiempoEspera = monitor.getTimeToWait(transicion);
//                        if (tiempoEspera > 0) {
//                            try {
//                                System.out.println("Transicion " + transicion + " esperando " + tiempoEspera + "ms");
//                                Thread.sleep(tiempoEspera);
//                                // Después de esperar, intentar disparar de nuevo
//                                disparo = monitor.fireTransition(transicion);
//                                if (disparo) {
//                                    System.out.println("Transicion " + transicion + " disparada después de esperar por "
//                                            + Thread.currentThread().getName());
//                                    logger.logTransicion(transicion);
//                                    algunaTransicionDisparada = true;
//                                }
//                            } catch (InterruptedException e) {
//                                Thread.currentThread().interrupt();
//                                return;
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Si ninguna transición fue disparada, dormir un poco para evitar ciclo ocupado
//            if (!algunaTransicionDisparada) {
//                try {
//                    Thread.sleep(50); // Puedes ajustar este valor según necesidad
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    return;
//                }
//            }
//        }
//    }
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
