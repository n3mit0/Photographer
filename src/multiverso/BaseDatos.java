package multiverso;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

public class BaseDatos {

    private final TablaHash recuperador;
    private static final Path FILE_PATH = Paths.get(System.getProperty("user.dir"), "BasedeDatos.txt");
    private final ReentrantLock lock = new ReentrantLock();

    public BaseDatos() {
        recuperador = new TablaHash();
        getItems();
    }

    // Método para guardar (append) un item en el archivo, uno por línea
    private void appendItem(Item item) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH.toFile(), true))) {
            writer.write(item.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar el item en el archivo: " + e.getMessage());
        }
    }

// Método para procesar cada línea, crear el objeto correspondiente y guardarlo
    private void processLine(String linea) {
        if (linea.trim().isEmpty()) {
            return;
        }

        String[] partes = linea.split(":");
        if (partes.length < 4) {
            System.err.println("Formato incorrecto en la línea: " + linea);
            return;
        }

        try {
            String tipo = partes[0].trim().toLowerCase();
            String nombre = partes[1].trim();
            int cantidad = Integer.parseInt(partes[2].trim());
            Item item = null;

            switch (tipo) {
                case "comida":
                    // Se requiere al menos 8 partes para tener: tipo, nombre, cantidad, algo, tiempo, algo, estado y categoría
                    if (partes.length >= 8) {
                        int tiempo = Integer.parseInt(partes[4].trim());
                        String estado = partes[6].trim();
                        String categoria = partes[7].trim();
                        // Se agrega una descripción por defecto para cumplir con el constructor
                        item = new Comida(tiempo, new ListaEnlazada[]{new ListaEnlazada()},
                                nombre, cantidad, estado, categoria, "Comida sin descripción");
                    } else {
                        System.err.println("Datos insuficientes para comida en línea: " + linea);
                    }
                    break;
                case "weapon":
                    if (partes.length >= 7) {
                        int damage = Integer.parseInt(partes[4].trim());
                        int durabilidad = Integer.parseInt(partes[5].trim());
                        int rango = Integer.parseInt(partes[6].trim());
                        item = new Weapon(damage, durabilidad, rango, nombre, cantidad, "Arma sin descripción");
                    } else {
                        System.err.println("Datos insuficientes para weapon en línea: " + linea);
                    }
                    break;
                case "craftable":
                    item = new Craftable(new Item[]{}, nombre, cantidad, "Objeto craftable sin descripción");
                    break;
                default:
                    System.err.println("Tipo desconocido o datos insuficientes: " + tipo);
            }

            if (item != null) {
                // Inserta el objeto en la estructura (por ejemplo, una tabla hash)
                recuperador.insertar(item);
                // Guarda el objeto en el archivo (append, uno por línea)
                appendItem(item);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error de conversión en línea: " + linea + " - " + e.getMessage());
        }
    }

// Método para leer el archivo y procesar cada línea
    private void getItems() {
        File archivo = FILE_PATH.toFile();
        if (!archivo.exists()) {
            System.err.println("Archivo no encontrado: " + FILE_PATH);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                processLine(linea);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

}

abstract class Item {

    String nombre;
    int cantidad;
    boolean equip;
    String descripcion;

    public Item(String nombre, int cantidad, String descripcion) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
    }

    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    @Override
    public abstract String toString();
}

class Weapon extends Item {

    private final int damage, durabilidad, rango;

    public Weapon(int damage, int durabilidad, int rango, String nombre, int can, String des) {
        super(nombre, can, des);
        this.damage = damage;
        this.durabilidad = durabilidad;
        this.rango = rango;
        this.equip = true;
    }

    @Override
    public String toString() {
        return "weapon:" + nombre + ":" + cantidad + ":" + equip + ":" + damage + ":" + durabilidad + ":" + rango + ":" + descripcion;
    }
}

class Craftable extends Item {

    private final Item[] requerido;

    public Craftable(Item[] requerido, String nombre, int can, String des) {
        super(nombre, can, des);
        this.requerido = requerido;
        this.equip = false;
    }

    @Override
    public String toString() {
        return "craftable:" + nombre + ":" + cantidad + ":" + equip;
    }
}

class Comida extends Item {

    private final int tiempo;
    private final ListaEnlazada[] recetas;
    private final String estado, categoria;

    public Comida(int tiempo, ListaEnlazada[] recetas, String nombre, int can, String estado, String categ, String des) {
        super(nombre, can, des);
        this.tiempo = tiempo;
        this.recetas = recetas;
        this.estado = estado;
        this.categoria = categ;
        this.equip = false;
    }

    @Override
    public String toString() {
        return "comida:" + nombre + ":" + cantidad + ":" + equip + ":" + tiempo + ":" + descripcion + ":" + estado + ":" + categoria;
    }
}

class Nodo {

    Object dato;
    Nodo siguiente;

    public Nodo(Object dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}

class ListaEnlazada {

    private Nodo cabeza;

    public void insertar(Object dato) {
        Nodo nuevo = new Nodo(dato);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
    }

    public Nodo getCabeza() {
        return cabeza;
    }
}

class TablaHash {

    private static final int TAMANO = 10;
    private final ListaEnlazada[] tabla;

    public TablaHash() {
        tabla = new ListaEnlazada[TAMANO];
        for (int i = 0; i < TAMANO; i++) {
            tabla[i] = new ListaEnlazada();
        }
    }

    private int hash(int clave) {
        return (clave & Integer.MAX_VALUE) % TAMANO;
    }

    public void insertar(Object dato) {
        int indice = hash(dato.hashCode());
        tabla[indice].insertar(dato);
    }

    public ListaEnlazada[] getTabla() {
        return tabla;
    }
}
