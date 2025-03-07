package multiverso;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

public class BaseDatos {

    private TablaHash recuperador;
    private static final Path FILE_PATH = Paths.get(System.getProperty("user.dir"), "BasedeDatos.txt");

    public BaseDatos() {
        this.recuperador = new TablaHash();
        try {
            getItems();
        } catch (IOException e) {
            System.err.println("Error al cargar la base de datos: " + e.getMessage());
        }
    }

    private void getItems() throws IOException {
        if (!Files.exists(FILE_PATH)) {
            System.out.println("Archivo no encontrado: " + FILE_PATH);
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                procesarLinea(linea);
            }
        }
    }

    private void procesarLinea(String linea) {
        String[] partes = linea.split(":");
        if (partes.length < 4) {
            System.err.println("Formato incorrecto en la línea: " + linea);
            return;
        }

        try {
            String tipo = partes[0].trim().toLowerCase();
            String nombre = partes[1].trim();
            int cantidad = Integer.parseInt(partes[2].trim());
            boolean equip = Boolean.parseBoolean(partes[3].trim());

            Map<String, Function<String[], Item>> creadores = Map.of(
                "comida", p -> {
                    if (p.length < 7) {
                        System.err.println("Datos insuficientes para comida: " + linea);
                        return null;
                    }
                    return new Comida(
                        Integer.parseInt(p[4].trim()),
                        new ListaEnlazada[]{new ListaEnlazada()},
                        p[1].trim(), cantidad, equip, p[6].trim(),
                        (p.length > 7) ? p[7].trim() : "Desconocida",
                        p[5].trim()
                    );
                },
                "weapon", p -> {
                    if (p.length < 7) {
                        System.err.println("Datos insuficientes para weapon: " + linea);
                        return null;
                    }
                    return new Weapon(
                        Integer.parseInt(p[4].trim()),
                        Integer.parseInt(p[5].trim()),
                        Integer.parseInt(p[6].trim()), nombre, cantidad,
                        "Arma sin descripción"
                    );
                },
                "craftable", p -> new Craftable(new Item[]{}, nombre, cantidad, equip, "Objeto craftable sin descripción")
            );

            Item item = creadores.getOrDefault(tipo, p -> {
                System.err.println("Tipo desconocido: " + tipo);
                return null;
            }).apply(partes);

            if (item != null) recuperador.insertar(item);

        } catch (NumberFormatException e) {
            System.err.println("Error de conversión en línea: " + linea);
        }
    }

    public void guardarItems() {
        try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
            for (ListaEnlazada lista : recuperador.getTabla()) {
                Nodo actual = lista.getCabeza();
                while (actual != null) {
                    if (actual.dato instanceof Item item) {
                        writer.write(item.toString());
                        writer.newLine();
                    }
                    actual = actual.siguiente;
                }
            }
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }
}

abstract class Item {
    String nombre;
    int cantidad;
    boolean equip;
    String descripcion;

    public Item(String nombre, int cantidad, boolean equip, String descripcion) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.equip = equip;
        this.descripcion = descripcion;
    }

    @Override
    public abstract String toString();
}

class Weapon extends Item {
    int damage, durabilidad, rango;

    public Weapon(int damage, int durabilidad, int rango, String nombre, int cantidad, String descripcion) {
        super(nombre, cantidad, true, descripcion);
        this.damage = damage;
        this.durabilidad = durabilidad;
        this.rango = rango;
    }

    @Override
    public String toString() {
        return String.format("weapon:%s:%d:%b:%d:%d:%d:%s", nombre, cantidad, equip, damage, durabilidad, rango, descripcion);
    }
}

class Craftable extends Item {
    Item[] requerido;

    public Craftable(Item[] requerido, String nombre, int cantidad, boolean equip, String descripcion) {
        super(nombre, cantidad, equip, descripcion);
        this.requerido = requerido;
    }

    @Override
    public String toString() {
        return String.format("craftable:%s:%d:%b", nombre, cantidad, equip);
    }
}

class Comida extends Item {
    private int tiempo;
    private ListaEnlazada[] recetas;
    private String estado, categoria;

    public Comida(int tiempo, ListaEnlazada[] recetas, String nombre, int cantidad, boolean equip, String estado, String categoria, String descripcion) {
        super(nombre, cantidad, equip, descripcion);
        this.tiempo = tiempo;
        this.recetas = recetas;
        this.estado = estado;
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return String.format("comida:%s:%d:%b:%d:%s:%s:%s", nombre, cantidad, equip, tiempo, descripcion, estado, categoria);
    }
}

class Nodo {
    Object dato;
    Nodo siguiente;
    public Nodo(Object dato) { this.dato = dato; this.siguiente = null; }
}

class ListaEnlazada {
    private Nodo cabeza;
    public void insertar(Object dato) { Nodo nuevo = new Nodo(dato); nuevo.siguiente = cabeza; cabeza = nuevo; }
    public Nodo getCabeza() { return cabeza; }
}

class TablaHash {
    private static final int TAMANO = 10;
    private final ListaEnlazada[] tabla;

    public TablaHash() {
        tabla = new ListaEnlazada[TAMANO];
        Arrays.setAll(tabla, i -> new ListaEnlazada());
    }

    private int hash(String clave) { return Math.abs(clave.hashCode()) % TAMANO; }

    public void insertar(Item item) { tabla[hash(item.nombre)].insertar(item); }

    public Item buscar(String nombre) {
        for (Nodo n = tabla[hash(nombre)].getCabeza(); n != null; n = n.siguiente)
            if (n.dato instanceof Item i && i.nombre.equals(nombre)) return i;
        return null;
    }

    public void eliminar(String nombre) { /* Método para eliminar un ítem */ }
    public ListaEnlazada[] getTabla() { return tabla; }
}
