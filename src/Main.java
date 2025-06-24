package main;

import main.monitor.Monitor;
import main.politicas.*;
import main.red.RdP;
import main.threads.Transiciones;
import main.threads.Logger;

public class Main {
    public static void main(String[] args) {

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

        RdP red = new RdP(matrizI,marcadoInicial);
        PoliticaInterface politica = new PoliticaAleatoria();
        Monitor monitor = new Monitor(red, politica);
        Thread[] transicionesThreads = new Thread[5];
        Logger logger = new Logger();
        logger.start();

        for (int i = 0; i < transicionesThreads.length; i++) {
            transicionesThreads[i] = new Transiciones(monitor, modoProcesamiento[i], logger);
            transicionesThreads[i].start();
        }

        logger.signalStop();
        try {
            logger.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//      try{
//          for(Thread t: transicionesThreads) t.join();
//      } catch (InterruptedException e) {
//          throw new RuntimeException(e);
//      }
    }
}
