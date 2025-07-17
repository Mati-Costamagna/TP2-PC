package main.red;

public class RdP {

    private int[] marcado;
    private final int[][] matrizIncidencia;
    private final boolean[] transicionesSensibilizadas;
    private final long[] tiempoSensibilizacion;
    private final long[] alpha;
    private final long[] beta;

    public RdP(int[][] matrizI, int[] marcadoInicial, long[] alpha, long[] beta) {
        this.marcado = marcadoInicial;
        this.matrizIncidencia = matrizI;
        this.alpha = alpha;
        this.beta = beta;
        this.tiempoSensibilizacion = new long[matrizI.length];
        transicionesSensibilizadas = new boolean[matrizI.length];
        setTransicionesSensibilizadas();

    }

    private synchronized void setTransicionesSensibilizadas() {

        for (int i = 0; i < this.matrizIncidencia.length; i++) { // columnas
            for (int j = 0; j < this.matrizIncidencia[i].length; j++) { // filas
                if (matrizIncidencia[j][i] == -1) { // Verifica sensibilizacion por token
                    if (marcado[j] < 1) {
                        transicionesSensibilizadas[i] = false;
                        tiempoSensibilizacion[i] = 0;
                        break;
                    }
                    tiempoSensibilizacion[i] = transicionesSensibilizadas[i] && tiempoSensibilizacion[i] != 0
                            ? tiempoSensibilizacion[i]
                            : System.currentTimeMillis(); // Actualizar tiempo de sensibilización si es necesario
                    transicionesSensibilizadas[i] = true;
                }
            }
        }
    }

    private boolean invariantesPlaza() {
        boolean invariante1 = (marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8]
                + marcado[9] + marcado[10] + marcado[11]) == 3;
        boolean invariante2 = ((marcado[1] + marcado[2]) == 1);
        boolean invariante3 = ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9]
                + marcado[10]) == 1);
        if (!invariante1) {
            System.out.println("Invariante 1 no se cumple");
            return false;
        } else if (!invariante2) {
            System.out.println("Invariante 2 no se cumple");
            return false;
        } else if (!invariante3) {
            System.out.println("Invariante 3 no se cumple");
            return false;
        }
        return true;
    }

    public boolean[] getTransicionesSensibilizadas() {
        return transicionesSensibilizadas;
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

    private boolean actualizarMarcado(int t){
        int[] marcadoAnterior = marcado.clone();
        for (int i = 0; i < this.matrizIncidencia.length; i++) {
            marcado[i] += matrizIncidencia[i][t];
            if (marcado[i] < 0) {
                System.out.println("Marcado negativo en la plaza " + i + ", revertiendo el marcado.");
                marcado = marcadoAnterior.clone(); // Revertir el marcado si se vuelve negativo
                return false; // No se dispara la transición
            }
        }
        if (!invariantesPlaza()) {
            marcado = marcadoAnterior.clone(); // Revertir el marcado si no se cumple la invariante de plaza
            System.out.println("No se cumple la invariante de plaza, revirtiendo el marcado.");
            return false; // No se cumple la invariante de plaza, no se dispara la transición
        } else {
            setTransicionesSensibilizadas();
            return true;
        }
    }

    public boolean disparar(int t) {
        if(!transicionesSensibilizadas[t]) {
            System.out.println("Transición " + t + " no está sensibilizada.");
            return false; // No se dispara la transición si no está sensibilizada
        }else {
            return actualizarMarcado(t);
        }
    }
}
