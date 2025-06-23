package main;

import main.monitor.Monitor;
import main.politicas.*;
import main.red.RdP;
import main.threads.Transiciones;

public class Main {
    private static final int[]marcadoInicial = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};
    private static final int [][] matrizI = {{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, -1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {-1,1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0},
            {0, 1,  -1, 0,  0,  -1, 0,  -1, 0,  0,  0,  0},
            {0, 1,  -1, 0,  0,  -1, 0,  -1, 0,  0,  0,  0},
            {0, 0,  0,  1,  -1, 0,  0,  0,  0,  0,  0,  0},
            {0, 0,  -1, 0,  1,  -1, 1,  -1, 0,  0,  1,  0},
            {0, 0,  0,  0,  0,  1,  -1, 0,  0,  0,  0,  0},
            {0, 0,  0,  0,  0,  0,  0,  1,  -1, 0,  0,  0},
            {0, 0,  0,  0,  0,  0,  0,  0,  1,  -1, 0,  0},
            {0, 0,  0,  0,  0,  0,  0,  0,  0,  1,  -1, 0},
            {0, 0,  0,  0,  1,  0,  1,  0,  0,  0,  1,  -1},
    };
    private static final int[][] invariantes = {{0, 1, 5, 6, 11},
                                                {0, 1, 2, 3, 4, 11},
                                                {0, 1, 7, 8, 9, 10, 11}
                                                };

    public static void main(String[] args) {

    RdP red = new RdP(matrizI,marcadoInicial);
    PoliticaInterface politica = new PoliticaAleatoria();
    Monitor monitor = new Monitor(red, politica);
    Thread[] transicionesThreads = new Thread[3];
    for (int i = 0; i < transicionesThreads.length; i++) {
        transicionesThreads[i] = new Transiciones(monitor, invariantes[i]);
        transicionesThreads[i].start();
    }

    try{
        for(Thread t: transicionesThreads) t.join();
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }

    }
}
