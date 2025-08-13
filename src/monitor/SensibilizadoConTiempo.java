package main.monitor;

public class SensibilizadoConTiempo {
    private boolean[] transicionesDormidas;
    private long[] hilosADormir;

    public SensibilizadoConTiempo(boolean[] transiciones) {
        this.transicionesDormidas = new boolean[transiciones.length];
        this.hilosADormir = new long[transiciones.length];
    }

    public boolean estaLevantado(int t){
        return transicionesDormidas[t];
    }

    public void dormir(int t, long tiempo){
        try{
            Thread.sleep(tiempo);
            transicionesDormidas[t] = true;
        }catch (InterruptedException e){
            System.out.println("Hilo interrumpido al intentar dormir");
        }
    }

    public boolean hayAlguienLevantado(){
        for (int i = 0; i < transicionesDormidas.length; i++) {
            if(transicionesDormidas[i]){
                return true;
            }
        }
        return false;
    }

    public void entroAlMonitor(int t){
        transicionesDormidas[t] = false;
    }

    public void setDormir(int t, long tiempo){
        hilosADormir[t] = tiempo;
    }

    public long tieneQueDormir(int t){
        return hilosADormir[t];
    }
}
