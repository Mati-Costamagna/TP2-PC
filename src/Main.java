package main;

import monitor.Monitor;
import monitor.MonitorInterface;
import politicas.PoliticaAleatoria;
import politicas.PoliticaPrioritaria;
import politicas.Politica;
import workers.TransicionWorker;

public class Main {
    public static void main(String[] args) {
        int[] marcadoInicial = {
            1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0
        };

        boolean[][] pre = new boolean[12][11];
        boolean[][] post = new boolean[12][11];

        // Definiciones como arriba (omitir por brevedad, deben copiarse)

        Monitor monitor = new Monitor(marcadoInicial, pre, post);

        PoliticaAleatoria politicaAleatoria = new PoliticaAleatoria(); // o new PoliticaPrioritaria()

        // Simulamos 3 hilos: uno para cada tipo de procesamiento
        TransicionWorker simple = new TransicionWorker("Simple", new int[]{3, 6}, monitor, politicaAleatoria);
        TransicionWorker medio  = new TransicionWorker("Medio", new int[]{4, 5}, monitor, politicaAleatoria);
        TransicionWorker alto   = new TransicionWorker("Alto", new int[]{7, 8, 9, 10}, monitor, politicaAleatoria);

        simple.start();
        medio.start();
        alto.start();

        // Tiempo de ejecución (ej. 30s)
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Detenemos los hilos
        simple.interrupt();
        medio.interrupt();
        alto.interrupt();

        try {
            simple.join();
            medio.join();
            alto.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Simulación finalizada.");
    }
}
