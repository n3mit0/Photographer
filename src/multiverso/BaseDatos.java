package multiverso;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class BaseDatos {

    private TablaHash recuperador;
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    // Ruta del archivo (sin comillas adicionales)
    private static final String FILE_PATH = "C:\\Users\\Nemito\\Documents\\"
            + "NetBeansProjects\\JavaApplication11\\build\\classes\\multiverso"
            + "\\BasedeDatos.txt";

    public BaseDatos() {
        try {
            getItems();
        } catch (IOException e) {
            System.out.println("Error al cargar la base de datos: " + e.getMessage());
        }
    }

    private void getItems() throws IOException {
        // Inicializamos la tabla hash que almacenará los ítems
        this.recuperador = new TablaHash();
        File archivo = new File(FILE_PATH);
        if (!archivo.exists()) {
            System.out.println("Archivo no encontrado: " + FILE_PATH);
            return;
        }

        // Se lee línea por línea el archivo en modo lectura (incluso si se va haciendo append)
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Se ignoran líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] partes = linea.split(":");
                if (partes.length < 4) {
                    System.out.println("Formato incorrecto en la línea: " + linea);
                    continue;
                }

                // Se leen los datos comunes a todos los ítems
                String tipo = partes[0].trim();
                String nombre = partes[1].trim();
                int cantidad = Integer.parseInt(partes[2].trim());
                boolean equip = Boolean.parseBoolean(partes[3].trim());
                Item item = null;

                // Dependiendo del tipo, se crean los objetos correspondientes
                if (tipo.equalsIgnoreCase("comida") && partes.length >= 7) {
                    int tiempo = Integer.parseInt(partes[4].trim());
                    String obtain = partes[5].trim();
                    String estado = partes[6].trim();
                    String categoria = (partes.length > 7) ? partes[7].trim() : "Desconocida";
                    // Se utiliza el constructor que espera un arreglo de ListaEnlazada para las recetas
                    item = new Comida(tiempo, new ListaEnlazada[]{new ListaEnlazada()},
                            nombre, cantidad, equip, estado, categoria, obtain);
                } else if (tipo.equalsIgnoreCase("weapon") && partes.length >= 7) {
                    int damage = Integer.parseInt(partes[4].trim());
                    int durabilidad = Integer.parseInt(partes[5].trim());
                    int rango = Integer.parseInt(partes[6].trim());
                    // Se utiliza el constructor que recibe la cantidad y se asigna una descripción por defecto
                    item = new weapon(damage, durabilidad, rango, nombre, cantidad, "Arma sin descripción");
                } else if (tipo.equalsIgnoreCase("craftable")) {
                    // Se utiliza el constructor que recibe un arreglo vacío de ítems requeridos y una descripción por defecto
                    item = new Craft_able(new Item[]{}, nombre, cantidad, equip, "Objeto craftable sin descripción");
                } else {
                    System.out.println("Tipo desconocido o datos insuficientes: " + tipo);
                }

                // Si se creó correctamente el ítem, se inserta en la tabla hash
                if (item != null) {
                    recuperador.insertar(item);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error procesando el archivo: " + e.getMessage());
        }
    }

    public void guardarItems() throws IOException {
        // Se abre el archivo en modo sobrescribir (append=false)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            // Se obtiene el arreglo de listas de la tabla hash
            ListaEnlazada[] tabla = recuperador.getTabla();
            for (int i = 0; i < tabla.length; i++) {
                // Se recorre cada nodo de la lista enlazada de cada índice
                Nodo actual = tabla[i].getCabeza();
                while (actual != null) {
                    if (actual.dato instanceof Item) {
                        Item item = (Item) actual.dato;
                        String linea = "";
                        if (item instanceof Comida) {
                            Comida comida = (Comida) item;
                            // Formato: comida:nombre:cantidad:equip:tiempo:descripcion:estado:categoria
                            linea = "comida:"
                                    + comida.getNombre() + ":"
                                    + comida.getCantidad() + ":"
                                    + comida.isEquip() + ":"
                                    + comida.getTiempo() + ":"
                                    + comida.getDescripcion() + ":"
                                    + comida.getEstado() + ":"
                                    + comida.getCategoria();
                        } else if (item instanceof weapon) {
                            weapon arma = (weapon) item;
                            // Formato: weapon:nombre:cantidad:equip:damage:durabilidad:rango:descripcion
                            linea = "weapon:"
                                    + arma.getNombre() + ":"
                                    + arma.getCantidad() + ":"
                                    + arma.isEquip() + ":"
                                    + arma.getDamage() + ":"
                                    + arma.getDurabilidad() + ":"
                                    + arma.getRango() + ":"
                                    + arma.getDescripcion();
                        } else if (item instanceof Craft_able) {
                            Craft_able craft = (Craft_able) item;
                            // Formato: craftable:nombre:cantidad:equip
                            linea = "craftable:"
                                    + craft.getNombre() + ":"
                                    + craft.getCantidad() + ":"
                                    + craft.isEquip();
                        }
                        writer.write(linea);
                        writer.newLine();
                    }
                    actual = actual.siguiente;
                }
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo: " + e.getMessage());
        }

        // Uso del CountDownLatch para la sincronización, similar al ejemplo
        countDownLatch.countDown();
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            System.out.println("Error en la espera del latch: " + ex.getMessage());
        }
    }

}

//items objetos guardados
abstract class Item {

    String tipo;
    String nombre;
    int cantidad;
    boolean equip;
    String descripcion;

    public Item(String nombre, String des) {
        this.nombre = nombre;
        this.cantidad = 0;
        this.equip = false;
        this.descripcion = des;
    }

    public Item(String nombre, int can, String des) {
        this.nombre = nombre;
        this.cantidad = can;
        this.descripcion = des;
    }
}

class Comida extends Item {

    private int tiempo; // descomposicion
    private ListaEnlazada[] recetas;
    private String efects;
    private String estado; //color del estado de la comida
    private String categoria;

    // Constructor que usa el de la superclase
    public Comida(int tiempo, String nombre, String categ, String des, String status, ListaEnlazada[] recetas) {
        super(nombre, des);
        this.tiempo = tiempo;
        this.recetas = recetas;
        this.categoria = categ;
        this.equip = false;
    }

    public Comida(int tiempo, ListaEnlazada[] recetas, String nombre, int can, boolean equ, String estado, String categ, String des) {
        super(nombre, can, des);
        this.tiempo = tiempo;
        this.recetas = recetas;
        this.estado = estado;
        this.categoria = categ;
        this.equip = false;
    }

    public void actStatus(String newstatus) {
        this.estado = newstatus;
    }
    
    public int getTiempo() {
        return tiempo;
    }

    public ListaEnlazada[] getRecetas() {
        return recetas;
    }

    public String getEfects() {
        return efects;
    }

    public String getEstado() {
        return estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isEquip() {
        return equip;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

class weapon extends Item {

    int damage;
    int durabilidad;
    int rango;

    public weapon(int damage, int durabilidad, int rango, String nombre, String des) {
        super(nombre, des);
        this.damage = damage;
        this.durabilidad = durabilidad;
        this.rango = rango;
        this.equip = true;
    }

    public weapon(int damage, int durabilidad, int rango, String nombre, int can, String des) {
        super(nombre, can, des);
        this.damage = damage;
        this.durabilidad = durabilidad;
        this.rango = rango;
        this.equip = true;
    }

    public int getDamage() {
        return damage;
    }

    public int getDurabilidad() {
        return durabilidad;
    }

    public int getRango() {
        return rango;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isEquip() {
        return equip;
    }

    public String getDescripcion() {
        return descripcion;
    }

}

class Craft_able extends Item {

    Item[] requerido;

    public Craft_able(Item[] requerido, String nombre, String des) {
        super(nombre, des);
        this.requerido = requerido;
        this.equip = false;
    }

    public Craft_able(Item[] requerido, String nombre, int can, boolean equ, String des) {
        super(nombre, can, des);
        this.requerido = requerido;
        this.equip = false;
    }

    public Item[] getRequerido() {
        return requerido;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isEquip() {
        return equip;
    }

    public String getDescripcion() {
        return descripcion;
    }
    
}

//NODOS
class Nodo {

    Object dato;
    Nodo siguiente;

    public Nodo(Object dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}

//LISTA ENLAZADA
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

    public void mostrar() {
        Nodo actual = cabeza;
        while (actual != null) {
            System.out.print(actual.dato + " -> ");
            actual = actual.siguiente;
        }
        System.out.println("null");
    }
}

//TABLA HASH
class TablaHash {

    private static final int TAMANO = 10;
    private ListaEnlazada[] tabla;

    public TablaHash() {
        tabla = new ListaEnlazada[TAMANO];
        for (int i = 0; i < TAMANO; i++) {
            tabla[i] = new ListaEnlazada();
        }
    }

    // Método hash que utiliza la clave para calcular el índice.
    private int hash(int clave) {
        return Math.abs(clave) % TAMANO;
    }

    // Método para insertar cualquier objeto usando su hashCode().
    public void insertar(Object dato) {
        int clave = dato.hashCode();
        int indice = hash(clave);
        tabla[indice].insertar(dato);
    }

    public void mostrar() {
        for (int i = 0; i < TAMANO; i++) {
            System.out.print("Índice " + i + ": ");
            tabla[i].mostrar();
        }
    }

    public ListaEnlazada[] getTabla() {
        return tabla;
    }
}
