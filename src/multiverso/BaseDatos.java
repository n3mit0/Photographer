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
        try {
            getItems();
        } catch (IOException e) {
            System.err.println("Error al cargar la base de datos: " + e.getMessage());
        }
    }

    private void getItems() throws IOException {
        File archivo = FILE_PATH.toFile();
        if (!archivo.exists()) {
            System.err.println("Archivo no encontrado: " + FILE_PATH);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(":");
                if (partes.length < 4) {
                    System.err.println("Formato incorrecto en la línea: " + linea);
                    continue;
                }

                try {
                    String tipo = partes[0].trim().toLowerCase();
                    String nombre = partes[1].trim();
                    int cantidad = Integer.parseInt(partes[2].trim());
                    boolean equip = Boolean.parseBoolean(partes[3].trim());
                    Item item = null;

                    switch (tipo) {
                        case "comida":
                            if (partes.length >= 7) {
                                int tiempo = Integer.parseInt(partes[4].trim());
                                String obtain = partes[5].trim();
                                String estado = partes[6].trim();
                                String categoria = (partes.length > 7) ? partes[7].trim() : "Desconocida";
                                item = new Comida(tiempo, new ListaEnlazada[]{new ListaEnlazada()},
                                        nombre, cantidad, equip, estado, categoria, obtain);
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
                        recuperador.insertar(item);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error de conversión en línea: " + linea + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    public void guardarItems() {
        lock.lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH.toFile(), false))) {
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
        } finally {
            lock.unlock();
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

    public Comida(int tiempo, ListaEnlazada[] recetas, String nombre, int can, boolean equip, String estado, String categ, String des) {
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
