package main.red;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RdP {

    private int[] marcado;
    private final int[][] matrizIncidencia;
    private final boolean[] transicionesSensibilizadas;
    private final Object lock = new Object();

    public RdP(int[][] matrizI, int[] marcadoInicial) {
        this.marcado = marcadoInicial;
        this.matrizIncidencia = matrizI;
        transicionesSensibilizadas = new boolean[matrizI.length];
        setTransicionesSensibilizadas();
    }

    private void setTransicionesSensibilizadas() {
        synchronized (lock) {
            for (int t = 0; t < matrizIncidencia[0].length; t++) { //transiciones
                boolean sensibilizada = true;
                for (int p = 0; p < matrizIncidencia.length; p++) { //plazas
                    if (matrizIncidencia[p][t] < 0 && (marcado[p] + matrizIncidencia[p][t] < 0)) {
                        sensibilizada = false;
                        break;
                    }
                }
                transicionesSensibilizadas[t] = sensibilizada;
            }
        }
    }

    private boolean invariantesPlaza() {
        if ((marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8] + marcado[9] + marcado[10] + marcado[11]) != 3) {
            System.out.println("Invariante 1");
            return false;
        } else if (((marcado[1] + marcado[2]) != 1)) {
            System.out.println("Invariante 2");
            return false;
        } else if ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9] + marcado[10]) != 1){
            System.out.println("Invariante 3");
            return false;
        }
        return (marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8] + marcado[9] + marcado[10] + marcado[11]) == 3 &&
                ((marcado[1] + marcado[2]) == 1) &&
                ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9] + marcado[10]) == 1);
    }

    public boolean[] getTransicionesSensibilizadas() {
        synchronized (lock) {
            return transicionesSensibilizadas;
        }
    }


    public boolean disparar(int t) {
        if(!transicionesSensibilizadas[t]) {
            //System.out.println("La transición " + t + " no está sensibilizada.");
            return false; // No se dispara la transición si no está sensibilizada
        }else {
            int[] marcadoAnterior = marcado.clone();
            for (int i = 0; i < this.matrizIncidencia.length; i++) {
                marcado[i] += matrizIncidencia[i][t];
                if (marcado[i] < 0) {
                    //System.out.println("Marcado negativo en la plaza " + i + ", revertiendo el marcado.");
                    marcado = marcadoAnterior.clone(); // Revertir el marcado si se vuelve negativo
                    return false; // No se dispara la transición
                }
            }
            if (!invariantesPlaza()) {
                marcado = marcadoAnterior.clone(); // Revertir el marcado si no se cumple la invariante de plaza
                //System.out.println("No se cumple la invariante de plaza, revirtiendo el marcado.");
                return false; // No se cumple la invariante de plaza, no se dispara la transición
            } else {
                setTransicionesSensibilizadas();
                return true;
            }
        }
    }
}
