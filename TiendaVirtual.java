import java.util.HashMap;
import java.util.Scanner;

/**
 * TIENDA VIRTUAL - Backend por terminal
 *
 * Materia: Estructura de Datos
 *
 * La tienda guarda todo en diccionarios (HashMap), que son estructuras
 * de tipo LLAVE -> VALOR. Se usan dos:
 *
 *   categorias : llave = codigo de la categoria (String)
 *                valor = nombre de la categoria (String)
 *
 *   productos  : llave = codigo del producto (String)
 *                valor = objeto Producto con todos sus datos
 *
 * Se eligio el diccionario porque la operacion mas frecuente de una tienda
 * es "dame el producto con este codigo", y el HashMap la resuelve de forma
 * directa sin recorrer la coleccion.
 */
public class TiendaVirtual {

    /**
     * Guarda los datos de un producto.
     * Es una clase interna estatica para que todo quede en un solo archivo.
     */
    static class Producto {
        String codigo;
        String nombre;
        double precio;
        int stock;
        String codigoCategoria;

        Producto(String codigo, String nombre, double precio, int stock, String codigoCategoria) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.precio = precio;
            this.stock = stock;
            this.codigoCategoria = codigoCategoria;
        }
    }

    // Los dos diccionarios de la tienda
    static HashMap<String, String> categorias = new HashMap<>();
    static HashMap<String, Producto> productos = new HashMap<>();

    static Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {
        cargarDatosDePrueba();

        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            opcion = leerEntero("Elige una opción: ");

            if (opcion == 1) {
                agregarCategoria();
            } else if (opcion == 2) {
                agregarProducto();
            } else if (opcion == 3) {
                menuBuscar();
            } else if (opcion == 4) {
                venderProducto();
            } else if (opcion == 5) {
                listarTodo();
            } else if (opcion == 0) {
                System.out.println("\nGracias por usar la tienda. Hasta luego.");
            } else {
                System.out.println("\nEsa opción no existe. Intenta de nuevo.");
            }
        }
    }

    static void mostrarMenu() {
        System.out.println("\n========================================");
        System.out.println("           TIENDA VIRTUAL");
        System.out.println("========================================");
        System.out.println("1. Agregar categoría");
        System.out.println("2. Agregar producto");
        System.out.println("3. Buscar productos");
        System.out.println("4. Vender producto");
        System.out.println("5. Listar todo");
        System.out.println("0. Salir");
        System.out.println("========================================");
    }

    // ====================================================================
    // AGREGAR
    // ====================================================================

    static void agregarCategoria() {
        System.out.println("\n--- AGREGAR CATEGORÍA ---");
        String codigo = leerTexto("Código de la categoría: ").toUpperCase();

        // containsKey pregunta si esa llave ya existe en el diccionario.
        // No hay que recorrer nada: el HashMap responde de forma directa.
        if (categorias.containsKey(codigo)) {
            System.out.println("Ya existe una categoría con ese código.");
            return;
        }

        String nombre = leerTexto("Nombre de la categoría: ");

        // put guarda el par llave -> valor
        categorias.put(codigo, nombre);
        System.out.println("Categoría agregada: " + codigo + " - " + nombre);
    }

    static void agregarProducto() {
        System.out.println("\n--- AGREGAR PRODUCTO ---");

        if (categorias.isEmpty()) {
            System.out.println("Primero tienes que crear una categoría.");
            return;
        }

        String codigo = leerTexto("Código del producto: ").toUpperCase();
        if (productos.containsKey(codigo)) {
            System.out.println("Ya existe un producto con ese código.");
            return;
        }

        String nombre = leerTexto("Nombre del producto: ");
        double precio = leerDecimal("Precio: ");
        if (precio <= 0) {
            System.out.println("El precio tiene que ser mayor que cero.");
            return;
        }

        int stock = leerEntero("Cantidad en stock: ");
        if (stock < 0) {
            System.out.println("El stock no puede ser negativo.");
            return;
        }

        System.out.println("\nCategorías disponibles:");
        for (String cod : categorias.keySet()) {
            System.out.println("  " + cod + " - " + categorias.get(cod));
        }

        String codCategoria = leerTexto("Código de la categoría: ").toUpperCase();
        if (!categorias.containsKey(codCategoria)) {
            System.out.println("Esa categoría no existe.");
            return;
        }

        Producto p = new Producto(codigo, nombre, precio, stock, codCategoria);
        productos.put(codigo, p);
        System.out.println("Producto agregado correctamente.");
    }

    // ====================================================================
    // BUSCAR
    //
    // Esta es la parte importante de la materia. Hay dos formas muy
    // distintas de buscar dentro de un diccionario, y cuestan diferente:
    //
    //  A) BUSCAR POR LA LLAVE  ->  productos.get(codigo)
    //     El HashMap calcula la posición del dato a partir de la llave
    //     (eso se llama función hash) y va directo a buscarlo.
    //     No importa si hay 10 productos o 10.000: el tiempo es el mismo.
    //     Costo: O(1), tiempo constante.
    //
    //  B) BUSCAR POR OTRO CAMPO  ->  recorrer productos.values()
    //     El nombre y la categoría NO son la llave, así que el diccionario
    //     no puede ir directo. Toca revisar producto por producto y
    //     comparar. Si hay 10.000 productos, se revisan los 10.000.
    //     Costo: O(n), crece con la cantidad de datos.
    //
    // Conclusión: se elige como llave el campo por el que más se busca.
    // ====================================================================

    static void menuBuscar() {
        System.out.println("\n--- BUSCAR PRODUCTOS ---");
        System.out.println("1. Por código   (búsqueda directa por llave)");
        System.out.println("2. Por nombre   (recorre todo el diccionario)");
        System.out.println("3. Por categoría (recorre todo el diccionario)");
        int op = leerEntero("Elige cómo buscar: ");

        if (op == 1) {
            buscarPorCodigo();
        } else if (op == 2) {
            buscarPorNombre();
        } else if (op == 3) {
            buscarPorCategoria();
        } else {
            System.out.println("Esa opción no existe.");
        }
    }

    /**
     * BÚSQUEDA POR LLAVE - Costo O(1)
     *
     * El código del producto es la llave del diccionario, entonces se usa
     * get() y el HashMap devuelve el producto de forma directa.
     * Si la llave no existe, get() devuelve null.
     */
    static void buscarPorCodigo() {
        String codigo = leerTexto("Código a buscar: ").toUpperCase();

        Producto p = productos.get(codigo);

        if (p == null) {
            System.out.println("No hay ningún producto con ese código.");
        } else {
            System.out.println("\nProducto encontrado:");
            mostrarProducto(p);
        }
    }

    /**
     * BÚSQUEDA POR NOMBRE - Costo O(n)
     *
     * El nombre no es la llave, así que hay que recorrer todos los valores
     * del diccionario y comparar uno por uno.
     *
     * Se usa contains() para que encuentre coincidencias parciales: si se
     * busca "tecla" también aparece "Teclado mecánico". Se pasa todo a
     * minúsculas para que no importe si se escribió con mayúsculas.
     *
     * Se cuentan las comparaciones para dejar ver que aquí sí se revisa
     * todo el diccionario, a diferencia de la búsqueda por código.
     */
    static void buscarPorNombre() {
        String texto = leerTexto("Nombre o parte del nombre: ").toLowerCase();

        int encontrados = 0;
        int comparaciones = 0;

        // values() entrega todos los productos guardados
        for (Producto p : productos.values()) {
            comparaciones++;
            if (p.nombre.toLowerCase().contains(texto)) {
                if (encontrados == 0) {
                    System.out.println("\nResultados:");
                }
                mostrarProducto(p);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("No se encontró ningún producto con ese nombre.");
        }
        System.out.println("(Se revisaron " + comparaciones + " productos del diccionario)");
    }

    /**
     * BÚSQUEDA POR CATEGORÍA - Costo O(n)
     *
     * Igual que la anterior: la categoría no es la llave del diccionario
     * de productos, entonces toca recorrerlo completo y comparar el campo
     * codigoCategoria de cada producto.
     */
    static void buscarPorCategoria() {
        if (categorias.isEmpty()) {
            System.out.println("Todavía no hay categorías.");
            return;
        }

        System.out.println("\nCategorías disponibles:");
        for (String cod : categorias.keySet()) {
            System.out.println("  " + cod + " - " + categorias.get(cod));
        }

        String codCategoria = leerTexto("Código de la categoría: ").toUpperCase();

        if (!categorias.containsKey(codCategoria)) {
            System.out.println("Esa categoría no existe.");
            return;
        }

        int encontrados = 0;
        int comparaciones = 0;

        for (Producto p : productos.values()) {
            comparaciones++;
            if (p.codigoCategoria.equals(codCategoria)) {
                if (encontrados == 0) {
                    System.out.println("\nProductos de " + categorias.get(codCategoria) + ":");
                }
                mostrarProducto(p);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("Esa categoría no tiene productos.");
        }
        System.out.println("(Se revisaron " + comparaciones + " productos del diccionario)");
    }

    // ====================================================================
    // VENDER
    // ====================================================================

    /**
     * Para vender se busca el producto por su llave, o sea con get().
     * Es una búsqueda O(1), la más rápida, y por eso conviene que el
     * código sea la llave: vender es de las cosas que más se repiten.
     */
    static void venderProducto() {
        System.out.println("\n--- VENDER PRODUCTO ---");

        if (productos.isEmpty()) {
            System.out.println("Todavía no hay productos.");
            return;
        }

        String codigo = leerTexto("Código del producto: ").toUpperCase();

        Producto p = productos.get(codigo);
        if (p == null) {
            System.out.println("No hay ningún producto con ese código.");
            return;
        }

        System.out.println("Producto: " + p.nombre);
        System.out.println("Precio:   $" + p.precio);
        System.out.println("Stock:    " + p.stock);

        if (p.stock == 0) {
            System.out.println("No hay stock de este producto.");
            return;
        }

        int cantidad = leerEntero("¿Cuántos vas a vender? ");

        if (cantidad <= 0) {
            System.out.println("La cantidad tiene que ser mayor que cero.");
            return;
        }
        if (cantidad > p.stock) {
            System.out.println("No alcanza el stock. Solo quedan " + p.stock + ".");
            return;
        }

        // Se descuenta del stock. Como p apunta al objeto que está dentro
        // del diccionario, el cambio queda guardado sin volver a hacer put.
        p.stock = p.stock - cantidad;

        double total = p.precio * cantidad;

        System.out.println("\n--- VENTA REALIZADA ---");
        System.out.println("Producto:  " + p.nombre);
        System.out.println("Cantidad:  " + cantidad);
        System.out.println("Total:     $" + total);
        System.out.println("Stock que queda: " + p.stock);
    }

    // ====================================================================
    // LISTAR
    // ====================================================================

    static void listarTodo() {
        System.out.println("\n--- CATEGORÍAS (" + categorias.size() + ") ---");
        if (categorias.isEmpty()) {
            System.out.println("No hay categorías.");
        } else {
            for (String cod : categorias.keySet()) {
                System.out.println("  " + cod + " - " + categorias.get(cod));
            }
        }

        System.out.println("\n--- PRODUCTOS (" + productos.size() + ") ---");
        if (productos.isEmpty()) {
            System.out.println("No hay productos.");
        } else {
            for (Producto p : productos.values()) {
                mostrarProducto(p);
            }
        }
    }

    static void mostrarProducto(Producto p) {
        String nombreCategoria = categorias.get(p.codigoCategoria);
        System.out.println("  [" + p.codigo + "] " + p.nombre
                + " | $" + p.precio
                + " | stock: " + p.stock
                + " | " + nombreCategoria);
    }

    // ====================================================================
    // DATOS DE PRUEBA
    // ====================================================================

    static void cargarDatosDePrueba() {
        categorias.put("TEC", "Tecnología");
        categorias.put("HOG", "Hogar");

        productos.put("P001", new Producto("P001", "Teclado mecánico", 45.50, 10, "TEC"));
        productos.put("P002", new Producto("P002", "Mouse inalámbrico", 18.00, 25, "TEC"));
        productos.put("P003", new Producto("P003", "Lámpara de escritorio", 22.75, 8, "HOG"));
    }

    // ====================================================================
    // LECTURA DE DATOS POR TECLADO
    // ====================================================================

    static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().trim();
    }

    /**
     * Se lee la línea completa y después se convierte a número.
     * Si el usuario escribe letras, se avisa y se vuelve a preguntar,
     * así el programa no se cae.
     */
    static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = teclado.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Tienes que escribir un número entero.");
            }
        }
    }

    static double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = teclado.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(linea);
            } catch (NumberFormatException e) {
                System.out.println("Tienes que escribir un número.");
            }
        }
    }
}
