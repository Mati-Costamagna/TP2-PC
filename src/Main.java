package main;

import main.monitor.Monitor;
import main.politicas.*;
import main.red.RdP;
import main.threads.Transiciones;
import main.threads.Logger;


public class Main {
    public static void main(String[] args) throws InterruptedException {

        int[] marcadoInicial = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};
        int[][] matrizI = {
                // T0  T1  T2  T3  T4  T5  T6  T7  T8  T9 T10 T11
                {-1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
                {1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
                {-1, 1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
                {0,  1, -1,  0,  0, -1,  0, -1,  0,  0,  0,  0},
                {0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0,  0},
                {0,  0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0},
                {0,  0, -1,  0,  1, -1,  1, -1,  0,  0,  1,  0},
                {0,  0,  0,  0,  0,  1, -1,  0,  0,  0,  0,  0},
                {0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0,  0},
                {0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0},
                {0,  0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0},
                {0,  0,  0,  0,  1,  0,  1,  0,  0,  0,  1, -1},
        };

        int[][] segmentos = {{0, 1},
                {5, 6},
                {2, 3, 4},
                {7, 8, 9, 10},
                {11}
        };

        // Inicialización de la Red de Petri, la política y el monitor
        long[] alpha = {0,0,0,0,0,0,0,0,0,0,0,0};
        //long[] alpha = {0,10,0,10,10,0,10,0,10,10,10,0};
        long[] beta = {Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE};
         RdP red = new RdP(matrizI,marcadoInicial, alpha, beta);
        PoliticaInterface politica = new PoliticaPrioritaria(); // PoliticaAleatoria() o PoliticaPrioritaria()
        Monitor monitor = new Monitor(red, politica);

        // Inicialización del Logger y su hilo
        Logger logger = new Logger(politica);
        logger.start(); // Inicia el hilo del logger

        // Creación y start de los hilos de transiciones
        Thread[] transicionesThreads = new Thread[segmentos.length];
        for (int i = 0; i < transicionesThreads.length; i++) {
            transicionesThreads[i] = new Transiciones(monitor, segmentos[i], logger);
            transicionesThreads[i].start();
        }

        System.out.println("Esperando a que el logger termine de procesar...");
        while(!logger.alcanzoCantMaxInvariantes()) {
            // Espera activa hasta que se alcancen las condiciones de parada
            Thread.sleep(1000);
        }
        logger.finalizarLogger();

        // Interrumpe y espera a que los hilos de transiciones terminen
        for (Thread t : transicionesThreads) {
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Hilo de transición interrumpido al finalizar: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        logger.join();
        System.out.println("\n--- Ejecución terminada. Revisa 'log_estadisticas.txt' para los resultados ---");
    }
}