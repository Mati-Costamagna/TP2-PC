package main.red;

import main.monitor.SensibilizadoConTiempo;

public class RdP {

    private int[] marcado;
    private final int[][] matrizIncidencia;
    private final SensibilizadoConTiempo sensibilizadoConTiempo;
    private final boolean[] transicionesSensibilizadas;
    private final long[] tiempoSensibilizacion;
    private final long[] alpha;
    private final long[] beta;

    public RdP(int[][] matrizI, int[] marcadoInicial, long[][] cis, SensibilizadoConTiempo s) {
        this.marcado = marcadoInicial.clone();
        this.matrizIncidencia = matrizI.clone();
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

    private boolean testVentanaTiempo(int transition) {
        return (System.currentTimeMillis() - this.tiempoSensibilizacion[transition] >= alpha[transition])
                && (System.currentTimeMillis() - this.tiempoSensibilizacion[transition] <= beta[transition]);
    }

    private long getTimeToWait(int transition) {
        long tiempoTranscurrido = System.currentTimeMillis() - this.tiempoSensibilizacion[transition];
        long tiempoRestante = alpha[transition] - tiempoTranscurrido;
        return Math.max(0, tiempoRestante);
    }

    private void setEsperando(int transition, long tiempoEspera) {
        sensibilizadoConTiempo.setDormir(transition, tiempoEspera);
    }

    private boolean estaSensibilizadaEnTiempo(int transition) {
        if(transicionesSensibilizadas[transition]) {
            if (testVentanaTiempo(transition)) {
                return true;
            } else {
                long tiempoEspera = getTimeToWait(transition);
                if (tiempoEspera > 0) {
                    System.out.println("Hilo " + Thread.currentThread().getName() + " yendo a dormir por " + getTimeToWait(transition) + " ms fuera del monitor");
                    setEsperando(transition, tiempoEspera);
                }
                return false;
            }
        }else{
            return false; // No está sensibilizada si no cumple la condición de marcado
        }
    }

    public boolean[] getTransicionesSensibilizadas() {
        return transicionesSensibilizadas;
    }

    public boolean disparar(int transition) {
        if(!estaSensibilizadaEnTiempo(transition)){
            return false; // No se dispara la transición si no está sensibilizada
        }else {
            int[] marcadoAnterior = marcado.clone();
            for (int i = 0; i < this.matrizIncidencia.length; i++) {
                marcado[i] += matrizIncidencia[i][transition];
                if (marcado[i] < 0) {
                    marcado = marcadoAnterior.clone(); // Revertir el marcado si se vuelve negativo
                    return false; // No se dispara la transición
                }
            }
            if (!invariantesPlaza()) {
                marcado = marcadoAnterior.clone(); // Revertir el marcado si no se cumple la invariante de plaza
                return false; // No se cumple la invariante de plaza, no se dispara la transición
            } else {
                setTransicionesSensibilizadas();
                return true;
            }
        }
    }

}