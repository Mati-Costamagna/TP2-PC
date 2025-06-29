package main;

import main.monitor.Monitor;
import main.politicas.*;
import main.red.RdP;
import main.threads.Transiciones;
import main.threads.Logger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        long inicio = System.currentTimeMillis();
        // --- CONFIGURACIÓN DE LA RED ---
        int[] marcadoInicial = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};
        int[][] matrizI = {
                // T0  T1  T2  T3  T4  T5  T6  T7  T8  T9 T10 T11
                {-1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},  // P0
                {1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},  // P1
                {-1, 1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},  // P2
                {0,  1, -1,  0,  0, -1,  0, -1,  0,  0,  0,  0},  // P3
                {0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0,  0},  // P4
                {0,  0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0},  // P5
                {0,  0, -1,  0,  1, -1,  1, -1,  0,  0,  1,  0},  // P6
                {0,  0,  0,  0,  0,  1, -1,  0,  0,  0,  0,  0},  // P7
                {0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0,  0},  // P8
                {0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0},  // P9
                {0,  0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0},  // P10
                {0,  0,  0,  0,  1,  0,  1,  0,  0,  0,  1, -1},  // P11
        };

        // Configuracion de tiempos (en milisegundos)
        // Transiciones temporales: T1, T3, T4, T6, T8, T9, T10
        long[] alpha = {0,75,0,75,75,0,75,0,75,75,75,0};
        long[] beta = new long[marcadoInicial.length];

        // Tiempos Beta (máximos)
        for(int i=0; i < beta.length; i++) {
            beta[i] = Long.MAX_VALUE;
        }

        int[][] segmentos = {{0, 1},
                {5, 6},
                {2, 3, 4},
                {7, 8, 9, 10},
                {11}
        };

        RdP red = new RdP(matrizI, marcadoInicial, alpha, beta);
        PoliticaInterface politica = new PoliticaAleatoria(); // o PoliticaPrioritaria()
        Monitor monitor = new Monitor(red, politica);

        Logger logger = new Logger(politica,inicio);
        logger.start();

        Thread[] transicionesThreads = new Thread[segmentos.length];
        for (int i = 0; i < transicionesThreads.length; i++) {
            transicionesThreads[i] = new Transiciones(monitor, segmentos[i], logger);
            transicionesThreads[i].start();
        }

        System.out.println("Esperando a que el logger termine de procesar.");
        while(!logger.alcanzoCantMaxInvariantes()) {
            Thread.sleep(1000);
        }
        logger.finalizarLogger();

        System.out.println("Condición de parada alcanzada. Interrumpiendo hilos de transición.");

        for (Thread t : transicionesThreads) {
            t.interrupt();
        }
        for (Thread t : transicionesThreads) {
            t.join();
        }

        logger.join();
        System.out.println("\n--- Ejecución terminada. ---");
    }
}