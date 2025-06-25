package main.threads;

import main.politicas.PoliticaInterface;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Logger extends Thread {
    private final AtomicBoolean finalizar = new AtomicBoolean(false);
    private final BlockingQueue<String> transiciones = new LinkedBlockingQueue<>(); // Cola para mantener los mensajes de registro de transiciones entrantes
    private BufferedWriter writer;
    private final String politica;

    // Invariantes a monitorear
    private final List<String> complejidadSimple = Arrays.asList("0", "1", "5", "6", "11");
    private final List<String> complejidadMedia = Arrays.asList("0", "1", "2", "3", "4", "11");
    private final List<String> complejidadAlta = Arrays.asList("0", "1", "7", "8", "9", "10", "11");

    private final AtomicInteger contSimple = new AtomicInteger(0);
    private final AtomicInteger contMedia = new AtomicInteger(0);
    private final AtomicInteger contAlta = new AtomicInteger(0);


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
        return (contSimple.get() + contMedia.get() + contAlta.get()) >= 200;
    }

    @Override
    public void run() {
        // El tamaño del buffer debe ser lo suficientemente grande para que entre la secuencia más larga que estamos verificando.
        // La secuencia más larga es complejidadAlta con una longitud de 7.
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
                    e.printStackTrace();
                    Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                    continue; // Reevaluar la condición del bucle para vaciar los elementos restantes de la cola
                }

                writer.write(transicion + " ");

                // Añadir al búfer y mantener el tamaño de la ventana deslizante.
                buffer.add(transicion);
                if (buffer.size() > 7) {
                    buffer.removeFirst();
                }

                // Verificar las ocurrencias de las secuencias deseadas
                if (buffer.size() >= complejidadSimple.size()) {
                    if (buffer.subList(buffer.size() - complejidadSimple.size(), buffer.size()).equals(complejidadSimple)) {
                        contSimple.incrementAndGet();
                        System.out.println("Invariante Complejidad Simple detectado. Cuenta: " + contSimple.get());
                    }
                }
                if (buffer.size() >= complejidadMedia.size()) {
                    if (buffer.subList(buffer.size() - complejidadMedia.size(), buffer.size()).equals(complejidadMedia)) {
                        contMedia.incrementAndGet();
                        System.out.println("Invariante Complejidad Media detectado. Cuenta: " + contMedia.get());
                    }
                }
                if (buffer.size() >= complejidadAlta.size()) {
                    if (buffer.subList(buffer.size() - complejidadAlta.size(), buffer.size()).equals(complejidadAlta)) {
                        contAlta.incrementAndGet();
                        System.out.println("Invariante Complejidad Alta detectado. Cuenta: " + contAlta.get());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo el logger: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Escribir los conteos finales de solo las secuencias deseadas en el archivo y cerrar el logger
            try {
                writer.write("\n" + "Politica implementada: " + politica + "\n");
                writer.write("--- Cuenta final Invariantes ---\n");
                writer.write("Complejidad Simple: " + contSimple.get() + "\n");
                writer.write("Complejidad Media: " + contMedia.get() + "\n");
                writer.write("Complejidad Alta: " + contAlta.get() + "\n");
                writer.write("Invariantes Totales: " + (contSimple.get() + contMedia.get() + contAlta.get()) + "\n");
                writer.close();
                System.out.println("Logger 'log_estadisticas.txt' cerrado. Cuenta final escrita.");
            } catch (IOException e) {
                System.err.println("Error cerrando logger o escribiendo cuenta final: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
