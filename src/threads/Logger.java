package main.threads;

import main.politicas.PoliticaInterface;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger extends Thread {
    private final AtomicBoolean finalizar = new AtomicBoolean(false);
    private final BlockingQueue<String> transiciones = new LinkedBlockingQueue<>(); // Cola para mantener los mensajes de registro de transiciones entrantes
    private BufferedWriter writer;
    private final String politica;

    // Invariantes a monitorear
    private final List<String> complejidadSimple = Arrays.asList("5", "6");
    private final List<String> complejidadMedia = Arrays.asList("2", "3", "4");
    private final List<String> complejidadAlta = Arrays.asList("7", "8", "9", "10");

    private int contSimple = 0;
    private int contMedia = 0;
    private int contAlta = 0;


    public Logger(PoliticaInterface p) {
        this.politica = p.getClass().getSimpleName();
        try {
            this.writer = new BufferedWriter(new FileWriter("log_estadisticas.txt"));
        } catch (IOException e) {
            System.err.println("Error al crear el archivo de registro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void logTransicion(int transicion) {
        if (!finalizar.get()) { // Solo registrar si no se ha indicado explícitamente que se detenga
            transiciones.offer(String.valueOf(transicion)); // Añadir a la cola sin bloqueo, convertir int a String
        }
    }

    public void finalizarLogger() {
        finalizar.set(true);
        this.interrupt(); // Interrumpir para desbloquear de transiciones.take() si está esperando
    }

    public boolean alcanzoCantMaxInvariantes() {
        // La condición de parada considera la suma de los conteos de todos los invariantes.
        return (contSimple + contMedia + contAlta) >= 200;
    }

    @Override
    public void run() {
        // El tamaño del buffer debe ser lo suficientemente grande para que entre la secuencia más larga que estamos verificando.
        // La secuencia más larga es complejidadAlta con una longitud de 4.
        LinkedList<String> buffer = new LinkedList<>(); // Búfer de ventana deslizante para verificaciones de invariantes
        try {
            // Repetir mientras no se haya alcanzado el conteo máximo de invariantes Y
            // (no se haya indicado explícitamente detenerse O todavía haya elementos en la cola para procesar).
            while (!alcanzoCantMaxInvariantes() && (!finalizar.get() || !transiciones.isEmpty())) {
                String transicion;
                try {
                    if (finalizar.get()) {
                        // Si se está deteniendo, usar poll() para recuperar elementos existentes sin bloqueo
                        transicion = transiciones.poll();
                        if (transicion == null) {
                            break;
                        }
                    } else {
                        // De lo contrario, bloquear hasta que llegue un nuevo mensaje de transición
                        transicion = transiciones.take();
                    }
                } catch (InterruptedException e) {
                    // Si se interrumpe, generalmente es una señal para detenerse. Establecer bandera y reinterrumpir.
                    finalizar.set(true);
                    Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                    continue; // Reevaluar la condición del bucle para vaciar los elementos restantes de la cola
                }

                writer.write(transicion + "\n");

                // Añadir al búfer y mantener el tamaño de la ventana deslizante.
                buffer.add(transicion);
                if (buffer.size() > 4) {
                    buffer.removeFirst();
                }

                // Verificar las ocurrencias de las secuencias deseadas
                if (buffer.size() >= complejidadSimple.size()) {
                    if (buffer.subList(buffer.size() - complejidadSimple.size(), buffer.size()).equals(complejidadSimple)) {
                        contSimple++;
                        System.out.println("Invariante Complejidad Simple detectado. Cuenta: " + contSimple);
                    }
                }
                if (buffer.size() >= complejidadMedia.size()) {
                    if (buffer.subList(buffer.size() - complejidadMedia.size(), buffer.size()).equals(complejidadMedia)) {
                        contMedia++;
                        System.out.println("Invariante Complejidad Media detectado. Cuenta: " + contMedia);
                    }
                }
                if (buffer.size() >= complejidadAlta.size()) {
                    if (buffer.subList(buffer.size() - complejidadAlta.size(), buffer.size()).equals(complejidadAlta)) {
                        contAlta++;
                        System.out.println("Invariante Complejidad Alta detectado. Cuenta: " + contAlta);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo el logger: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Asegurarse de que todos los elementos restantes en la cola se escriban en el archivo antes de cerrar
            while (!transiciones.isEmpty()) {
                try {
                    String remainingTrans = transiciones.poll();
                    if (remainingTrans != null) {
                        writer.write(remainingTrans + "\n");
                    }
                } catch (IOException e) {
                    System.err.println("Error escribiendo logs restantes: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // Escribir los conteos finales de solo las secuencias deseadas en el archivo y cerrar el logger
            try {
                writer.write("Politica implementada: " + politica + "\n");
                writer.write("--- Cuenta final Invariantes ---\n");
                writer.write("Complejidad Simple: " + contSimple + "\n");
                writer.write("Complejidad Media: " + contMedia + "\n");
                writer.write("Complejidad Alta: " + contAlta + "\n");
                writer.write("Invariantes Totales: " + (contSimple + contMedia + contAlta) + "\n");
                writer.close();
                System.out.println("Logger 'log_estadisticas.txt' cerrado. Cuenta final escrita.");
            } catch (IOException e) {
                System.err.println("Error cerrando logger o escribiendo cuenta final: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
