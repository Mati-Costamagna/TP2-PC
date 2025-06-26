package main.red;

import java.sql.Time;
import java.util.Arrays;

public class RdP {

    private int[] marcado;
    private final int[][] matrizIncidencia;
    private final boolean[] transicionesSensibilizadas;
    private final long[] alpha;
    private final long[] beta;
    private final long[] timestamps;
    private final long[] tiemposEspera;
    private final boolean[] hilosEnEspera;

    public RdP(int[][] matrizI, int[] marcadoInicial,long[]alpha,long[] beta) {
        this.marcado = marcadoInicial;
        this.matrizIncidencia = matrizI;
        this.alpha = alpha;
        this.beta = beta;
        this.timestamps = new long[alpha.length];
        this.transicionesSensibilizadas = new boolean[matrizI.length];
        this.hilosEnEspera = new boolean[matrizI.length];
        this.tiemposEspera = new long[matrizI.length];
        setTransicionesSensibilizadas();
    }

    private synchronized void setTransicionesSensibilizadas() {
        boolean [] sensibilizadasViejas = transicionesSensibilizadas;
        for (int t = 0; t < this.matrizIncidencia[0].length; t++) {
            for (int p = 0; p < this.matrizIncidencia.length; p++) {
                if (matrizIncidencia[p][t] == -1) {
                    if (marcado[p] < 1) {
                        transicionesSensibilizadas[t] = false;
                        timestamps[t] = 0; // Resetear timestamp si no hay marcado suficiente
                        break;
                    }
                    this.transicionesSensibilizadas[t] = true;
                }
            }

            // Si la transición se acaba de sensibilizar (flanco ascendente), se setea el timestamp
            if (this.transicionesSensibilizadas[t] && !sensibilizadasViejas[t]) {
                this.timestamps[t] = System.currentTimeMillis();
            }
        }
    }

    private boolean invariantesPlaza() {
        boolean invariante1 =(marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8] + marcado[9] + marcado[10] + marcado[11]) == 3;
        boolean invariante2 =((marcado[1] + marcado[2]) == 1);
        boolean invariante3 =((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9] + marcado[10]) == 1);
        if (!invariante1) {
            System.out.println("Invariante 1 no se cumple");
            return false;
        }
        else if (!invariante2) {
            System.out.println("Invariante 2 no se cumple");
            return false;
        }
        else if (!invariante3){
            System.out.println("Invariante 3 no se cumple");
            return false;
        }
        return true;
    }

    private long tiempoSleep(int transition) {
        if (alpha[transition] == 0) { // Transición no temporal
            return 0;
        }

        long now = System.currentTimeMillis();
        long timestamp = timestamps[transition];

        if (now < timestamp + alpha[transition]) { // Antes de la ventana
            return (timestamp + alpha[transition]) - now;
        }

//        if (now > timestamp + beta[transition]) { // Después de la ventana (demasiado tarde)
//            System.out.println("Transicion " + transition + " pasada de tiempo. BETA");
//            return -1; // Indica que se pasó la ventana
//        }

        return 0; // Dentro de la ventana, listo para disparar
    }

    private void setEspera(int t, boolean estado) {
        hilosEnEspera[t] = estado;
        tiemposEspera[t] = tiempoSleep(t);
    }

    private boolean antesDeLaVentana(int t) {
        return (tiempoSleep(t) > 0);
    }

    public boolean disparar(int t) {
        int[] marcadoAnterior = marcado.clone();
        for (int i = 0; i < this.matrizIncidencia.length; i++) {
            marcado[i] = marcado[i] + matrizIncidencia[i][t];
        }
        if (!invariantesPlaza()) {
            marcado = marcadoAnterior.clone();
            System.out.println("No se cumple la invariante de plaza, revirtiendo el marcado.");
            return false;
        }
        setTransicionesSensibilizadas();
        setEspera(t,false);
        return true;
    }

    public boolean[] getTransicionesSensibilizadas() {
        return transicionesSensibilizadas;
    }

    private boolean testVentanaTiempo(int t) {
        return (tiempoSleep(t) == 0);
    }

    private boolean esperando(int t){
        for (int i = 0; i < hilosEnEspera.length; i++) {
            if (i != t && hilosEnEspera[i]) {
                return true;
            }
        }
        return false;
    }

    public boolean estaSensibilizado(int t){
            if (testVentanaTiempo(t) && transicionesSensibilizadas[t]) {
                return !esperando(t);
            } else {
                if (antesDeLaVentana(t) && transicionesSensibilizadas[t]) setEspera(t, true);
                return false;
            }
    }

    public void dormirHilo(int t){
        try {
            Thread.sleep(tiemposEspera[t]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}