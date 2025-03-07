package multiverso;

// BaseDatos.java
import java.util.Comparator;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantLock;

class BaseDatos {

    private final TablaHash recuperador;
    private static final Path FILE_PATH = Paths.get(System.getProperty("user.dir"), "BasedeDatos.txt");
    private final ReentrantLock lock = new ReentrantLock();

    public BaseDatos() {
        recuperador = new TablaHash();
        getItems();
    }

    // Método para exponer la tabla hash
    public TablaHash getRecuperador() {
        return recuperador;
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
                    // Se requiere al menos 8 partes: comida:nombre:cantidad:tiempo:descripcion:estado:categoria
                    if (partes.length >= 7) {
                        if (partes.length >= 7) {
                            int tiempo = Integer.parseInt(partes[3].trim());
                            String descripcion = partes[4].trim();
                            String estado = partes[5].trim();
                            String categoria = partes[6].trim();
                            // Se crea una lista enlazada por defecto para las recetas
                            item = new Comida(tiempo, new ListaEnlazada[]{ new ListaEnlazada() },
                                    nombre, cantidad, estado, categoria, descripcion);
                        }
                    } else {
                        System.err.println("Datos insuficientes para comida en línea: " + linea);
                    }
                    break;
                case "weapon":
                    // Se requiere: weapon:nombre:cantidad:damage:durabilidad:rango:descripcion
                    if (partes.length >= 7) {
                        int damage = Integer.parseInt(partes[3].trim());
                        int durabilidad = Integer.parseInt(partes[4].trim());
                        int rango = Integer.parseInt(partes[5].trim());
                        String descripcion = partes[6].trim();
                        item = new Weapon(damage, durabilidad, rango, nombre, cantidad, descripcion);
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
                // Inserta el objeto en la tabla hash y lo guarda en el archivo
                recuperador.insertar(item);
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
// Item.java (incluye las clases Item y sus subclases)
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
        return "weapon:" + nombre + ":" + cantidad + ":" + damage + ":" + durabilidad + ":" + rango + ":" + descripcion;
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

    // Método para obtener la prioridad basado en la categoría
    public int getPrioridad() {
        switch (categoria) {
            case "Rojo":
                return 3;
            case "Amarillo":
                return 2;
            case "Verde":
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return "comida:" + nombre + ":" + cantidad + ":" + tiempo + ":" + descripcion + ":" + estado + ":" + categoria;
    }
}

// TablaHash.java y ListaEnlazada.java
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

// ArbolBinario.java (estructura genérica de árbol binario)


class NodoArbolGenerico<T> {
    T dato;
    NodoArbolGenerico<T> izquierda, derecha;

    public NodoArbolGenerico(T dato) {
        this.dato = dato;
        this.izquierda = this.derecha = null;
    }
}

class ArbolBinario<T> {

    private NodoArbolGenerico<T> raiz;
    private final Comparator<T> comparator;

    public ArbolBinario(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void insertar(T dato) {
        raiz = insertarRec(raiz, dato);
    }

    private NodoArbolGenerico<T> insertarRec(NodoArbolGenerico<T> nodo, T dato) {
        if (nodo == null) {
            return new NodoArbolGenerico<>(dato);
        }
        if (comparator.compare(dato, nodo.dato) < 0) {
            nodo.izquierda = insertarRec(nodo.izquierda, dato);
        } else {
            nodo.derecha = insertarRec(nodo.derecha, dato);
        }
        return nodo;
    }

    public void inOrden() {
        inOrdenRec(raiz);
        System.out.println();
    }

    private void inOrdenRec(NodoArbolGenerico<T> nodo) {
        if (nodo != null) {
            inOrdenRec(nodo.izquierda);
            System.out.println(nodo.dato);
            inOrdenRec(nodo.derecha);
        }
    }
}

// OrganizadorArboles.java
class OrganizadorArboles {

    private final ArbolBinario<Comida> arbolComida;
    private final ArbolBinario<Weapon> arbolWeapon;
    private final ArbolBinario<Craftable> arbolCraftable;

    public OrganizadorArboles(BaseDatos baseDatos) {
        // Para Comida se ordena según la prioridad
        arbolComida = new ArbolBinario<>((c1, c2) -> Integer.compare(c1.getPrioridad(), c2.getPrioridad()));
        // Para Weapon y Craftable se ordena alfabéticamente por nombre
        arbolWeapon = new ArbolBinario<>((w1, w2) -> w1.nombre.compareTo(w2.nombre));
        arbolCraftable = new ArbolBinario<>((c1, c2) -> c1.nombre.compareTo(c2.nombre));

        // Se recorre la tabla hash para clasificar cada item
        TablaHash tablaHash = baseDatos.getRecuperador();
        ListaEnlazada[] tabla = tablaHash.getTabla();
        for (ListaEnlazada lista : tabla) {
            Nodo nodo = lista.getCabeza();
            while (nodo != null) {
                Object dato = nodo.dato;
                if (dato instanceof Comida) {
                    arbolComida.insertar((Comida) dato);
                } else if (dato instanceof Weapon) {
                    arbolWeapon.insertar((Weapon) dato);
                } else if (dato instanceof Craftable) {
                    arbolCraftable.insertar((Craftable) dato);
                }
                nodo = nodo.siguiente;
            }
        }
    }

    public void mostrarArboles() {
        System.out.println("=== Árbol de Comida (ordenado por prioridad) ===");
        arbolComida.inOrden();
        System.out.println("=== Árbol de Weapon (ordenado por nombre) ===");
        arbolWeapon.inOrden();
        System.out.println("=== Árbol de Craftable (ordenado por nombre) ===");
        arbolCraftable.inOrden();
    }
}


