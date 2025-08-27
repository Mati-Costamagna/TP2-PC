package main;

import main.monitor.ColaCondicion;
import main.monitor.Monitor;
import main.monitor.Mutex;
import main.monitor.SensibilizadoConTiempo;
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
                {2, 3, 4},
                {5, 6},
                {7, 8, 9, 10},
                {11}
        };

        //long[] alpha = {0,30,0,30,30,0,30,0,30,30,30,0};
        long[] alpha = {0,75,0,75,75,0,75,0,75,75,75,0};
        //long[] alpha = {0,0,0,0,0,0,0,0,0,0,0,0};
        long[] beta = {Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE};
        long[][] cis = {alpha, beta};

        // Inicialización de la Red de Petri, la política y el monitor
        Mutex mutex = new Mutex();
        ColaCondicion colaCondicion = new ColaCondicion(matrizI[0].length);
        SensibilizadoConTiempo sensibilizadoConTiempo = new SensibilizadoConTiempo(matrizI[0].length);
        RdP red = new RdP(matrizI, marcadoInicial, cis, sensibilizadoConTiempo);
        PoliticaInterface politica = new PoliticaAleatoria(); // PoliticaAleatoria() o PoliticaPrioritaria()
        Monitor monitor = new Monitor(red, mutex, politica, colaCondicion, sensibilizadoConTiempo);

        // Inicialización del Logger y su hilo
        Logger logger = new Logger(politica);
        logger.start();

        Thread[] transicionesThreads = new Thread[segmentos.length];
        for (int i = 0; i < transicionesThreads.length; i++) {
            transicionesThreads[i] = new Transiciones(monitor, segmentos[i], logger, sensibilizadoConTiempo);
            transicionesThreads[i].start();
        }

        System.out.println("Esperando a que el logger termine de procesar.");
        while(!logger.alcanzoCantMaxInvariantes()) {
            // Espera activa hasta que se alcancen las condiciones de parada
            Thread.sleep(100);
        }

        System.out.println("Finalizando logger");
        logger.finalizarLogger();
        System.out.println("Finalizando logger...");

        // Indicar a la cola de condición que se terminó la ejecución para que libere hilos en espera
        colaCondicion.setTerminado();

        // Esperar a que terminen su ejeccución los hilos de transiciones
        for (Thread t : transicionesThreads) {
            t.join();
        }

        System.out.println("Hilos finalizados. Esperando al logger...");
        logger.join();
        System.out.println("\n--- Ejecución terminada. ---");
    }
}