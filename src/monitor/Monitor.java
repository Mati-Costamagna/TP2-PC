package monitor;

import monitor.MonitorInterface;

public class Monitor implements MonitorInterface {
    private final int[] marcado;
    private final boolean[][] pre;
    private final boolean[][] post;

    public Monitor(int[] marcadoInicial, boolean[][] pre, boolean[][] post) {
        this.marcado = marcadoInicial;
        this.pre = pre;
        this.post = post;
    }

    @Override
    public synchronized boolean fireTransition(int t) {
        if (sensibilizada(t)) {
            for (int i = 0; i < marcado.length; i++) {
                if (pre[i][t]) marcado[i]--;
            }
            for (int i = 0; i < marcado.length; i++) {
                if (post[i][t]) marcado[i]++;
            }
            return true;
        }
        return false;
    }

    private boolean sensibilizada(int t) {
        for (int i = 0; i < marcado.length; i++) {
            if (pre[i][t] && marcado[i] == 0) return false;
        }
        return true;
    }
}
