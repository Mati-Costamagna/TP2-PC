package main.monitor;

public class SensibilizadoConTiempo {
    private  boolean[] transicionesLevantadas;
    private long[] hilosADormir;

    public SensibilizadoConTiempo(int size) {
        this.transicionesLevantadas = new boolean[size];
        this.hilosADormir = new long[size];
    }

    private long tiempoADormir(int transition){
        return hilosADormir[transition];
    }

    public boolean estaLevantado(int transition){
        return transicionesLevantadas[transition];
    }

    public void dormir(int transition){
        try{
            Thread.sleep(tiempoADormir(transition));
            transicionesLevantadas[transition] = true;
        }catch (InterruptedException e){
            System.out.println("Hilo interrumpido al intentar dormir");
        }
    }

    public boolean hayAlguienLevantado(){
        for (boolean transicionesLevantada : transicionesLevantadas) {
            if (transicionesLevantada) {
                return true;
            }
        }
        return false;
    }

    public void entroAlMonitor(int transition){
        transicionesLevantadas[transition] = false;
    }

    public void setDormir(int transition, long tiempo){
        hilosADormir[transition] = tiempo;
    }

    public boolean tieneQueDormir(int transition){
        return tiempoADormir(transition) > 0;
    }
}
