package main.red;

import main.monitor.ColaCondicion;
import main.monitor.Mutex;
import main.monitor.SensibilizadoConTiempo;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RdP {

    private int[] marcado;
    private final int[][] matrizIncidencia;
    private final SensibilizadoConTiempo sensibilizadoConTiempo;
    private final Mutex mutex;
    private final boolean[] transicionesSensibilizadas;
    private final long[] tiempoSensibilizacion;
    private final long[] alpha;
    private final long[] beta;

    public RdP(int[][] matrizI, int[] marcadoInicial, long[][] cis, Mutex m, SensibilizadoConTiempo s) {
        this.marcado = marcadoInicial.clone();
        this.matrizIncidencia = matrizI.clone();
        this.mutex = m;
        this.alpha = cis[0];
        this.beta = cis[1];
        this.sensibilizadoConTiempo = s;
        this.tiempoSensibilizacion = new long[matrizI.length];
        this.transicionesSensibilizadas = new boolean[matrizI.length];
        setTransicionesSensibilizadas();
    }

    private void setTransicionesSensibilizadas() {
            for (int t = 0; t < matrizIncidencia[0].length; t++) { //transiciones
                boolean sensibilizada = true;
                for (int p = 0; p < matrizIncidencia.length; p++) { //plazas
                    if (matrizIncidencia[p][t] < 0 && (marcado[p] + matrizIncidencia[p][t] < 0)) {
                        sensibilizada = false;
                        break;
                    }
                }
                transicionesSensibilizadas[t] = sensibilizada;
                tiempoSensibilizacion[t] = transicionesSensibilizadas[t] && tiempoSensibilizacion[t] != 0
                        ? tiempoSensibilizacion[t]
                        : System.currentTimeMillis(); // Actualizar tiempo de sensibilización si es necesario
            }
    }

    private boolean invariantesPlaza() {
        if ((marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8] + marcado[9] + marcado[10] + marcado[11]) != 3) {
            return false;
        } else if (((marcado[1] + marcado[2]) != 1)) {
            return false;
        } else if ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9] + marcado[10]) != 1){
            return false;
        }
        return (marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8] + marcado[9] + marcado[10] + marcado[11]) == 3 &&
                ((marcado[1] + marcado[2]) == 1) &&
                ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9] + marcado[10]) == 1);
    }

    public boolean[] getTransicionesSensibilizadas() {
        //synchronized (lock) {
            return transicionesSensibilizadas;
        //}
    }

    public boolean testVentanaTiempo(int t) {
        return (System.currentTimeMillis() - this.tiempoSensibilizacion[t] >= alpha[t])
                && (System.currentTimeMillis() - this.tiempoSensibilizacion[t] <= beta[t]);
    }

    public boolean antesDeLaVentanaTiempo(int t) {
        return (System.currentTimeMillis() - this.tiempoSensibilizacion[t] < alpha[t]);
    }

    public long getTimeToWait(int t) {
        long tiempoTranscurrido = System.currentTimeMillis() - this.tiempoSensibilizacion[t];
        long tiempoRestante = alpha[t] - tiempoTranscurrido;
        return Math.max(0, tiempoRestante);
    }

    private void setEsperando(int t){
        sensibilizadoConTiempo.setDormir(t, getTimeToWait(t));
    }

    private boolean estaSensibilizada(int t) {
        if(transicionesSensibilizadas[t]) {
            if (testVentanaTiempo(t)) {
                return true;
            } else {
                boolean antes = antesDeLaVentanaTiempo(t);
                System.out.println("Sale hilo " + Thread.currentThread().getName() + " del monitor por estar fuera de la ventana");
                if (antes) {
                    System.out.println("Hilo " + Thread.currentThread().getName() + " yendo a dormir por " + getTimeToWait(t) + " ms fuera del monitor");
                    setEsperando(t);
                }
                return false;
            }
        }else{
            return false; // No está sensibilizada si no cumple la condición de marcado
        }
    }

    public boolean disparar(int t) {
        if(!estaSensibilizada(t)){
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
