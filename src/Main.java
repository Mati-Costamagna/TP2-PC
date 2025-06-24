package main;

import main.monitor.Monitor;
import main.politicas.*;
import main.red.RdP;
import main.threads.Transiciones;
import main.threads.Logger;


public class Main {
    public static void main(String[] args) throws InterruptedException {

        int[] marcadoInicial = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};
        int [][] matrizI = {{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, -1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
                {-1,1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
                {0, 1,  -1, 0,  0,  -1, 0,  -1, 0,  0,  0,  0},
                {0, 0,  1,  -1,  0,  0, 0,  0,  0,  0,  0,  0},
                {0, 0,  0,  1,  -1, 0,  0,  0,  0,  0,  0,  0},
                {0, 0,  -1, 0,  1,  -1, 1,  -1, 0,  0,  1,  0},
                {0, 0,  0,  0,  0,  1,  -1, 0,  0,  0,  0,  0},
                {0, 0,  0,  0,  0,  0,  0,  1,  -1, 0,  0,  0},
                {0, 0,  0,  0,  0,  0,  0,  0,  1,  -1, 0,  0},
                {0, 0,  0,  0,  0,  0,  0,  0,  0,  1,  -1, 0},
                {0, 0,  0,  0,  1,  0,  1,  0,  0,  0,  1,  -1},
        };

        int[][] modoProcesamiento = {{0, 1},
                {5, 6},
                {2, 3, 4},
                {7, 8, 9, 10},
                {11}
        };

        // Inicialización de la Red de Petri, la política y el monitor
        RdP red = new RdP(matrizI,marcadoInicial);
        PoliticaInterface politica = new PoliticaAleatoria(); // Puedes cambiar a PoliticaPrioritaria() si lo deseas
        Monitor monitor = new Monitor(red, politica);

        // Inicialización del Logger y su hilo
        Logger logger = new Logger();
        logger.start(); // Inicia el hilo del logger

        // Creación y arranque de los hilos de transiciones
        Thread[] transicionesThreads = new Thread[modoProcesamiento.length];
        for (int i = 0; i < transicionesThreads.length; i++) {
            transicionesThreads[i] = new Transiciones(monitor, modoProcesamiento[i], logger);
            transicionesThreads[i].start();
        }

        // Dar un tiempo al logger para procesar todos los elementos de la cola
        System.out.println("Esperando a que el logger termine de procesar...");
        Thread.sleep(1000); // Espera 1 segundo para asegurar que la cola se procesa
        // Señaliza al logger para que se detenga
        logger.signalStop();
        // Espera a que el hilo del logger termine
        logger.join();
        // Interrumpe y espera a que los hilos de transiciones terminen
        for (Thread t : transicionesThreads) {
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Hilo de transición interrumpido al finalizar: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restaura el estado de interrupción
            }
        }
        System.out.println("\n--- Ejecución terminada. Revisa 'log_estadisticas.txt' para los resultados ---");
    }
}