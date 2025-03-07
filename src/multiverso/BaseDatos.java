package multiverso;

import java.io.*;

public class BaseDatos {

    private TablaHash recuperador;
    private static final String FILE_PATH = "C:\\Users\\Nemito\\Documents\\"
            + "NetBeansProjects\\JavaApplication11\\build\\classes\\multiverso"
            + "\\BasedeDatos.txt";

    public BaseDatos() {
        this.recuperador = new TablaHash();
        try {
            getItems();
        } catch (IOException e) {
            System.out.println("Error al cargar la base de datos: " 
                    + e.getMessage());
        }
    }

    private void getItems() throws IOException {
        File archivo = new File(FILE_PATH);
        if (!archivo.exists()) {
            System.out.println("Archivo no encontrado: " + FILE_PATH);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split(":");
                if (partes.length < 4) {
                    System.out.println("Formato incorrecto en la línea: " 
                            + linea);
                    continue;
                }

                try {
                    String tipo = partes[0].trim();
                    String nombre = partes[1].trim();
                    int cantidad = Integer.parseInt(partes[2].trim());
                    boolean equip = Boolean.parseBoolean(partes[3].trim());
                    Item item = null;

                    switch (tipo.toLowerCase()) {
                        case "comida":
                            if (partes.length < 7) {
                                System.out.println(
                                        "Datos insuficientes para comida: " 
                                                + linea);
                                continue;
                            }
                            int tiempo = Integer.parseInt(partes[4].trim());
                            String obtain = partes[5].trim();
                            String estado = partes[6].trim();
                            String categoria = (partes.length > 7) ? 
                                    partes[7].trim() : "Desconocida";
                            item = new Comida(tiempo, 
                                    new ListaEnlazada[]{new ListaEnlazada()},
                                    nombre, cantidad, equip, estado, 
                                    categoria, obtain);
                            break;
                        case "weapon":
                            if (partes.length < 7) {
                                System.out.println("Datos "
                                        + "insuficientes para weapon: " + linea);
                                continue;
                            }
                            int damage = Integer.parseInt(partes[4].trim());
                            int durabilidad = Integer.parseInt(partes[5].trim());
                            int rango = Integer.parseInt(partes[6].trim());
                            item = new Weapon(damage, durabilidad, 
                                    rango, nombre, cantidad, "Arma sin "
                                            + "descripción");
                            break;
                        case "craftable":
                            item = new Craftable(new Item[]{}, 
                                    nombre, cantidad, equip, "Objeto "
                                            + "craftable sin descripción");
                            break;
                        default:
                            System.out.println("Tipo desconocido: " + tipo);
                    }

                    if (item != null) {
                        recuperador.insertar(item);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error de conversión en línea: " + linea);
                }
            }
        }
    }

    public void guardarItems() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, false))) {
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
            System.out.println("Error al escribir en el archivo: " 
                    + e.getMessage());
        }
    }
}

// Implementación de la clase Item y sus subclases
abstract class Item {
    String nombre;
    int cantidad;
    boolean equip;
    String descripcion;

    public Item(String nombre, int cantidad, boolean equip, String descripcion){
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

    public Weapon(int damage, int durabilidad, int rango, String nombre, 
            int cantidad, String descripcion) {
        super(nombre, cantidad, true, descripcion);
        this.damage = damage;
        this.durabilidad = durabilidad;
        this.rango = rango;
    }

    @Override
    public String toString() {
        return "weapon:" + nombre + ":" + cantidad + ":" + equip +
                ":" + damage + ":" + durabilidad + ":" + rango 
                + ":" + descripcion;
    }
}

class Craftable extends Item {
    Item[] requerido;

    public Craftable(Item[] requerido, String nombre, int cantidad, 
            boolean equip, String descripcion) {
        super(nombre, cantidad, equip, descripcion);
        this.requerido = requerido;
    }

    @Override
    public String toString() {
        return "craftable:" + nombre + ":" + cantidad + ":" + equip;
    }
}

class Comida extends Item {
    private int tiempo;
    private ListaEnlazada[] recetas;
    private String estado, categoria;

    public Comida(int tiempo, ListaEnlazada[] recetas, String nombre, 
            int cantidad, boolean equip, String estado, String categoria,
            String descripcion) {
        super(nombre, cantidad, equip, descripcion);
        this.tiempo = tiempo;
        this.recetas = recetas;
        this.estado = estado;
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "comida:" + nombre + ":" + cantidad + ":" + equip + ":" +
                tiempo + ":" + descripcion + ":" + estado + ":" + categoria;
    }
}

// Implementación de la ListaEnlazada, Nodo y TablaHash
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

    private int hash(String clave) {
        return Math.abs(clave.hashCode()) % TAMANO;
    }

    public void insertar(Item item) {
        int indice = hash(item.nombre);
        tabla[indice].insertar(item);
    }

    public ListaEnlazada[] getTabla() {
        return tabla;
    }
}