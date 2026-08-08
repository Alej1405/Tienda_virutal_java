import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * ============================================================================
 * TIENDA VIRTUAL - Sistema de Gestión de Datos
 * Materia: Estructura de Datos
 * Fase 3: Procesamiento No Lineal y Optimización de Rutas
 * ============================================================================
 *
 * El sistema usa TRES estructuras de datos distintas, cada una elegida para
 * el trabajo que mejor sabe hacer:
 *
 * 1) DICCIONARIO (HashMap) - almacenamiento principal
 *    Estructura LLAVE -> VALOR.
 *      categorias : código de categoría -> nombre
 *      productos  : código de producto  -> objeto Producto
 *    Sirve para buscar un producto por su código en tiempo constante O(1),
 *    que es la operación más repetida de una tienda (buscar y vender).
 *
 * 2) ÁRBOL BINARIO DE BÚSQUEDA (ABB) - jerarquía ordenada
 *    Guarda los códigos de producto de forma ordenada. Permite listarlos
 *    alfabéticamente con el recorrido Inorden, algo que el diccionario no
 *    puede hacer porque no guarda ningún orden.
 *    Recorridos implementados: Inorden, Preorden y Postorden.
 *
 * 3) GRAFO (lista de adyacencia) - red de entregas
 *    Los vértices son zonas de la ciudad y las aristas son las vías que las
 *    conectan, con su distancia en kilómetros. Sobre este grafo se aplican:
 *      BFS      - recorre por niveles (zonas más cercanas primero)
 *      DFS      - recorre en profundidad (verifica conectividad)
 *      Dijkstra - calcula la ruta de menor distancia para una entrega
 *
 * La documentación del rendimiento de cada algoritmo está junto a su método
 * y resumida en el archivo README.md.
 * ============================================================================
 */
public class TiendaVirtual {

    // ========================================================================
    // ESTRUCTURAS DE APOYO
    // ========================================================================

    /** Datos de un producto de la tienda. */
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

    /**
     * Nodo del Árbol Binario de Búsqueda.
     * Cada nodo guarda un código de producto y tiene dos hijos:
     * a la izquierda los códigos menores y a la derecha los mayores.
     */
    static class NodoArbol {
        String codigo;
        NodoArbol izquierda;
        NodoArbol derecha;

        NodoArbol(String codigo) {
            this.codigo = codigo;
            this.izquierda = null;
            this.derecha = null;
        }
    }

    /**
     * Arista del grafo: una vía que llega a una zona con cierta distancia.
     * Si desde el CENTRO hay 8 km al NORTE, la arista se guarda en la lista
     * del CENTRO con destino NORTE y peso 8.
     */
    static class Arista {
        String destino;
        int distancia;

        Arista(String destino, int distancia) {
            this.destino = destino;
            this.distancia = distancia;
        }
    }

    // ========================================================================
    // DATOS DE LA TIENDA
    // ========================================================================

    /** Diccionario de categorías: código -> nombre. */
    static HashMap<String, String> categorias = new HashMap<>();

    /** Diccionario de productos: código -> Producto. */
    static HashMap<String, Producto> productos = new HashMap<>();

    /** Raíz del Árbol Binario de Búsqueda de códigos de producto. */
    static NodoArbol raizArbol = null;

    /**
     * Grafo de entregas como LISTA DE ADYACENCIA.
     * Cada zona guarda la lista de zonas vecinas con su distancia.
     * Se eligió lista de adyacencia y no matriz porque el grafo es disperso:
     * cada zona conecta con pocas zonas, no con todas.
     */
    static HashMap<String, ArrayList<Arista>> grafoEntregas = new HashMap<>();

    static Scanner teclado = new Scanner(System.in);

    // ========================================================================
    // PROGRAMA PRINCIPAL
    // ========================================================================

    public static void main(String[] args) {
        cargarDatosDePrueba();
        cargarRedDeEntregas();

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
            } else if (opcion == 6) {
                menuArbol();
            } else if (opcion == 7) {
                menuRedEntregas();
            } else if (opcion == 0) {
                System.out.println("\nGracias por usar la tienda. Hasta luego.");
            } else {
                System.out.println("\nEsa opción no existe. Intenta de nuevo.");
            }
        }
    }

    static void mostrarMenu() {
        System.out.println("\n============================================");
        System.out.println("              TIENDA VIRTUAL");
        System.out.println("============================================");
        System.out.println("  GESTIÓN");
        System.out.println("   1. Agregar categoría");
        System.out.println("   2. Agregar producto");
        System.out.println("   3. Buscar productos");
        System.out.println("   4. Vender producto");
        System.out.println("   5. Listar todo");
        System.out.println("  ESTRUCTURAS NO LINEALES");
        System.out.println("   6. Árbol de productos (recorridos)");
        System.out.println("   7. Red de entregas (BFS / DFS / Dijkstra)");
        System.out.println("   0. Salir");
        System.out.println("============================================");
    }

    // ========================================================================
    // GESTIÓN: AGREGAR
    // ========================================================================

    static void agregarCategoria() {
        System.out.println("\n--- AGREGAR CATEGORÍA ---");
        String codigo = leerTexto("Código de la categoría: ").toUpperCase();

        // containsKey pregunta si la llave existe. El diccionario responde
        // de forma directa, sin recorrer nada. Costo O(1).
        if (categorias.containsKey(codigo)) {
            System.out.println("Ya existe una categoría con ese código.");
            return;
        }

        String nombre = leerTexto("Nombre de la categoría: ");
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

        productos.put(codigo, new Producto(codigo, nombre, precio, stock, codCategoria));

        // El producto se guarda en las DOS estructuras: en el diccionario para
        // buscarlo rápido por código, y en el árbol para poder listarlo ordenado.
        raizArbol = insertarEnArbol(raizArbol, codigo);

        System.out.println("Producto agregado correctamente.");
        System.out.println("(Guardado en el diccionario y en el árbol binario)");
    }

    // ========================================================================
    // BÚSQUEDA DE PRODUCTOS
    //
    // Hay dos formas de buscar en un diccionario y cuestan muy distinto:
    //
    //  A) POR LA LLAVE -> productos.get(codigo)
    //     El HashMap calcula la posición del dato a partir de la llave con una
    //     función hash y va directo. Da igual si hay 10 o 10.000 productos.
    //     Costo: O(1), tiempo constante.
    //
    //  B) POR OTRO CAMPO -> recorrer productos.values()
    //     El nombre y la categoría no son la llave, así que hay que revisar
    //     producto por producto y comparar.
    //     Costo: O(n), crece con la cantidad de datos.
    //
    // Por eso se eligió el código como llave: es el campo por el que más se
    // busca y por el que se vende.
    // ========================================================================

    static void menuBuscar() {
        System.out.println("\n--- BUSCAR PRODUCTOS ---");
        System.out.println("1. Por código, en el diccionario   -> O(1)");
        System.out.println("2. Por código, en el árbol binario -> O(log n)");
        System.out.println("3. Por nombre                      -> O(n)");
        System.out.println("4. Por categoría                   -> O(n)");
        int op = leerEntero("Elige cómo buscar: ");

        if (op == 1) {
            buscarPorCodigoEnDiccionario();
        } else if (op == 2) {
            buscarPorCodigoEnArbol();
        } else if (op == 3) {
            buscarPorNombre();
        } else if (op == 4) {
            buscarPorCategoria();
        } else {
            System.out.println("Esa opción no existe.");
        }
    }

    /**
     * BÚSQUEDA POR LLAVE EN EL DICCIONARIO - Costo O(1)
     *
     * El código es la llave, entonces get() devuelve el producto de forma
     * directa. Si la llave no existe devuelve null.
     */
    static void buscarPorCodigoEnDiccionario() {
        String codigo = leerTexto("Código a buscar: ").toUpperCase();

        Producto p = productos.get(codigo);

        if (p == null) {
            System.out.println("No hay ningún producto con ese código.");
        } else {
            System.out.println("\nProducto encontrado:");
            mostrarProducto(p);
            System.out.println("(El diccionario fue directo al dato: 1 sola operación)");
        }
    }

    /**
     * BÚSQUEDA POR NOMBRE - Costo O(n)
     *
     * El nombre no es la llave, así que se recorren todos los valores y se
     * comparan uno por uno. Se usa contains() para aceptar coincidencias
     * parciales: buscar "tecla" encuentra "Teclado mecánico". Todo se pasa a
     * minúsculas para que no importe cómo se escribió.
     *
     * Se cuentan las comparaciones para dejar ver que aquí sí se revisa todo
     * el diccionario, a diferencia de la búsqueda por llave.
     */
    static void buscarPorNombre() {
        String texto = leerTexto("Nombre o parte del nombre: ").toLowerCase();

        int encontrados = 0;
        int comparaciones = 0;

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
        System.out.println("(Se revisaron " + comparaciones + " productos)");
    }

    /**
     * BÚSQUEDA POR CATEGORÍA - Costo O(n)
     *
     * Igual que la anterior: la categoría no es la llave del diccionario de
     * productos, así que toca recorrerlo completo comparando codigoCategoria.
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
        System.out.println("(Se revisaron " + comparaciones + " productos)");
    }

    // ========================================================================
    // ÁRBOL BINARIO DE BÚSQUEDA
    //
    // Un ABB guarda los datos ordenados: en cada nodo, todo lo que está a la
    // izquierda es menor y todo lo que está a la derecha es mayor.
    //
    // Ventaja sobre el diccionario: el diccionario NO guarda ningún orden, así
    // que para listar los productos alfabéticamente habría que ordenarlos cada
    // vez, con costo O(n log n). El árbol ya los tiene ordenados y el recorrido
    // Inorden los entrega en orden con solo O(n).
    //
    // Desventaja: buscar en el árbol cuesta O(log n) y en el diccionario O(1).
    // El árbol es más lento para buscar, pero es el único que mantiene orden.
    // ========================================================================

    /**
     * INSERTAR EN EL ÁRBOL - Costo O(log n) promedio, O(n) peor caso
     *
     * Se compara el código nuevo con el del nodo actual:
     *   - si es menor, baja por la izquierda
     *   - si es mayor, baja por la derecha
     *   - si es igual, no se inserta (no se repiten códigos)
     * Al llegar a un espacio vacío (null), ahí se crea el nodo.
     *
     * El método es recursivo: se llama a sí mismo con el subárbol que toca y
     * devuelve la raíz de ese subárbol ya modificada.
     */
    static NodoArbol insertarEnArbol(NodoArbol nodo, String codigo) {
        if (nodo == null) {
            return new NodoArbol(codigo);
        }

        int comparacion = codigo.compareTo(nodo.codigo);

        if (comparacion < 0) {
            nodo.izquierda = insertarEnArbol(nodo.izquierda, codigo);
        } else if (comparacion > 0) {
            nodo.derecha = insertarEnArbol(nodo.derecha, codigo);
        }
        // Si comparacion == 0 el código ya existe y no se hace nada.

        return nodo;
    }

    static void menuArbol() {
        System.out.println("\n--- ÁRBOL BINARIO DE PRODUCTOS ---");

        if (raizArbol == null) {
            System.out.println("El árbol está vacío.");
            return;
        }

        System.out.println("1. Recorrido INORDEN    (izquierda - raíz - derecha)");
        System.out.println("2. Recorrido PREORDEN   (raíz - izquierda - derecha)");
        System.out.println("3. Recorrido POSTORDEN  (izquierda - derecha - raíz)");
        System.out.println("4. Ver altura del árbol");
        int op = leerEntero("Elige una opción: ");

        if (op == 1) {
            System.out.println("\nINORDEN - devuelve los códigos ORDENADOS alfabéticamente:");
            ArrayList<String> lista = new ArrayList<>();
            recorridoInorden(raizArbol, lista);
            mostrarRecorrido(lista);
        } else if (op == 2) {
            System.out.println("\nPREORDEN - primero la raíz. Sirve para copiar el árbol:");
            ArrayList<String> lista = new ArrayList<>();
            recorridoPreorden(raizArbol, lista);
            mostrarRecorrido(lista);
        } else if (op == 3) {
            System.out.println("\nPOSTORDEN - la raíz al final. Sirve para borrar el árbol:");
            ArrayList<String> lista = new ArrayList<>();
            recorridoPostorden(raizArbol, lista);
            mostrarRecorrido(lista);
        } else if (op == 4) {
            int altura = calcularAltura(raizArbol);
            System.out.println("\nAltura del árbol: " + altura);
            System.out.println("Nodos en el árbol: " + productos.size());
            System.out.println("La altura marca cuántas comparaciones cuesta una búsqueda");
            System.out.println("en el peor caso. Mientras más balanceado, más baja la altura.");
        } else {
            System.out.println("Esa opción no existe.");
        }
    }

    /**
     * RECORRIDO INORDEN - Costo O(n)
     *
     * Orden: subárbol izquierdo, nodo actual, subárbol derecho.
     *
     * Es el recorrido más útil en un ABB porque devuelve los datos ORDENADOS
     * de menor a mayor. Como a la izquierda están los menores y a la derecha
     * los mayores, visitarlos en ese orden los saca ordenados.
     */
    static void recorridoInorden(NodoArbol nodo, ArrayList<String> resultado) {
        if (nodo == null) {
            return;
        }
        recorridoInorden(nodo.izquierda, resultado);
        resultado.add(nodo.codigo);
        recorridoInorden(nodo.derecha, resultado);
    }

    /**
     * RECORRIDO PREORDEN - Costo O(n)
     *
     * Orden: nodo actual, subárbol izquierdo, subárbol derecho.
     *
     * Se usa para copiar o guardar un árbol: si los nodos se insertan en el
     * orden que entrega el preorden, el árbol queda idéntico al original,
     * porque la raíz siempre viene antes que sus hijos.
     */
    static void recorridoPreorden(NodoArbol nodo, ArrayList<String> resultado) {
        if (nodo == null) {
            return;
        }
        resultado.add(nodo.codigo);
        recorridoPreorden(nodo.izquierda, resultado);
        recorridoPreorden(nodo.derecha, resultado);
    }

    /**
     * RECORRIDO POSTORDEN - Costo O(n)
     *
     * Orden: subárbol izquierdo, subárbol derecho, nodo actual.
     *
     * Se usa para borrar el árbol o liberar memoria: primero se eliminan los
     * hijos y al final el padre, así nunca se borra un nodo del que todavía
     * cuelga algo.
     */
    static void recorridoPostorden(NodoArbol nodo, ArrayList<String> resultado) {
        if (nodo == null) {
            return;
        }
        recorridoPostorden(nodo.izquierda, resultado);
        recorridoPostorden(nodo.derecha, resultado);
        resultado.add(nodo.codigo);
    }

    /**
     * BUSCAR EN EL ÁRBOL - Costo O(log n) promedio, O(n) peor caso
     *
     * En cada nodo se descarta la mitad del árbol: si el código buscado es
     * menor se baja a la izquierda y si es mayor a la derecha. Es la misma
     * idea de la búsqueda binaria.
     *
     * El peor caso O(n) pasa cuando el árbol queda degenerado, o sea cuando
     * los códigos se insertaron ya ordenados (P001, P002, P003...). Ahí cada
     * nodo tiene un solo hijo y el árbol se vuelve una lista.
     */
    static void buscarPorCodigoEnArbol() {
        String codigo = leerTexto("Código a buscar: ").toUpperCase();

        NodoArbol actual = raizArbol;
        int pasos = 0;
        boolean encontrado = false;

        while (actual != null) {
            pasos++;
            int comparacion = codigo.compareTo(actual.codigo);

            if (comparacion == 0) {
                encontrado = true;
                break;
            } else if (comparacion < 0) {
                actual = actual.izquierda;
            } else {
                actual = actual.derecha;
            }
        }

        if (encontrado) {
            System.out.println("\nProducto encontrado:");
            mostrarProducto(productos.get(codigo));
        } else {
            System.out.println("No hay ningún producto con ese código.");
        }
        System.out.println("(El árbol bajó " + pasos + " niveles para responder)");
    }

    /** Altura del árbol: cuántos niveles tiene. Costo O(n). */
    static int calcularAltura(NodoArbol nodo) {
        if (nodo == null) {
            return 0;
        }
        int alturaIzquierda = calcularAltura(nodo.izquierda);
        int alturaDerecha = calcularAltura(nodo.derecha);

        if (alturaIzquierda > alturaDerecha) {
            return alturaIzquierda + 1;
        } else {
            return alturaDerecha + 1;
        }
    }

    static void mostrarRecorrido(ArrayList<String> lista) {
        for (int i = 0; i < lista.size(); i++) {
            String codigo = lista.get(i);
            Producto p = productos.get(codigo);
            if (p != null) {
                System.out.println("  " + (i + 1) + ". [" + codigo + "] " + p.nombre);
            }
        }
        System.out.println("(" + lista.size() + " nodos visitados)");
    }

    // ========================================================================
    // GRAFO: RED DE ENTREGAS
    //
    // El grafo modela las zonas de la ciudad y las vías que las conectan.
    // Es NO DIRIGIDO (las vías son de doble sentido) y PONDERADO (cada vía
    // tiene una distancia en kilómetros).
    //
    // Se guarda como LISTA DE ADYACENCIA: un diccionario donde cada zona
    // apunta a la lista de sus zonas vecinas. Se prefirió sobre la matriz de
    // adyacencia porque el grafo es disperso: cada zona conecta con 2 o 3
    // zonas, no con todas. La matriz gastaría memoria O(V²) casi vacía,
    // mientras la lista gasta O(V + E).
    // ========================================================================

    /** Crea una vía de doble sentido entre dos zonas. */
    static void conectarZonas(String zonaA, String zonaB, int distancia) {
        if (!grafoEntregas.containsKey(zonaA)) {
            grafoEntregas.put(zonaA, new ArrayList<Arista>());
        }
        if (!grafoEntregas.containsKey(zonaB)) {
            grafoEntregas.put(zonaB, new ArrayList<Arista>());
        }
        // Se agrega en los dos sentidos porque el grafo es no dirigido
        grafoEntregas.get(zonaA).add(new Arista(zonaB, distancia));
        grafoEntregas.get(zonaB).add(new Arista(zonaA, distancia));
    }

    static void menuRedEntregas() {
        System.out.println("\n--- RED DE ENTREGAS ---");
        System.out.println("1. Ver el mapa de zonas");
        System.out.println("2. Recorrido BFS - por niveles (zonas más cercanas primero)");
        System.out.println("3. Recorrido DFS - en profundidad");
        System.out.println("4. Dijkstra - ruta más corta para una entrega");
        int op = leerEntero("Elige una opción: ");

        if (op == 1) {
            mostrarMapa();
        } else if (op == 2) {
            recorridoBFS();
        } else if (op == 3) {
            recorridoDFS();
        } else if (op == 4) {
            rutaMasCorta();
        } else {
            System.out.println("Esa opción no existe.");
        }
    }

    static void mostrarMapa() {
        System.out.println("\nZonas y sus conexiones (distancia en km):");
        for (String zona : grafoEntregas.keySet()) {
            System.out.print("  " + zona + " -> ");
            ArrayList<Arista> vecinas = grafoEntregas.get(zona);
            for (int i = 0; i < vecinas.size(); i++) {
                Arista a = vecinas.get(i);
                System.out.print(a.destino + "(" + a.distancia + "km)");
                if (i < vecinas.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        System.out.println("\nLa BODEGA es el punto de partida de todas las entregas.");
    }

    /**
     * BFS - BÚSQUEDA EN ANCHURA - Costo O(V + E)
     *
     * V son los vértices (zonas) y E las aristas (vías).
     *
     * Recorre el grafo POR NIVELES: primero todas las zonas vecinas directas,
     * después las vecinas de esas, y así. Es como una onda que se expande.
     *
     * Usa una COLA (FIFO, el primero que entra es el primero que sale). Al
     * visitar una zona se meten sus vecinas al final de la cola, y como se
     * atienden en orden de llegada, se termina recorriendo por niveles.
     *
     * Para qué sirve aquí: saber a cuántos "saltos" está cada zona de la
     * bodega. OJO: BFS cuenta saltos, no kilómetros. Para la distancia real
     * hay que usar Dijkstra.
     */
    static void recorridoBFS() {
        String inicio = "BODEGA";

        // La cola guarda las zonas por visitar
        LinkedList<String> cola = new LinkedList<>();
        // Las visitadas evitan pasar dos veces por la misma zona
        ArrayList<String> visitadas = new ArrayList<>();
        // El nivel dice a cuántos saltos está cada zona
        HashMap<String, Integer> nivel = new HashMap<>();

        cola.add(inicio);
        visitadas.add(inicio);
        nivel.put(inicio, 0);

        System.out.println("\nBFS desde " + inicio + " (por niveles):");

        while (!cola.isEmpty()) {
            // Se saca el primero de la cola
            String actual = cola.removeFirst();
            System.out.println("  Nivel " + nivel.get(actual) + " -> " + actual);

            // Se meten al final de la cola las vecinas que no se han visitado
            for (Arista a : grafoEntregas.get(actual)) {
                if (!visitadas.contains(a.destino)) {
                    visitadas.add(a.destino);
                    nivel.put(a.destino, nivel.get(actual) + 1);
                    cola.add(a.destino);
                }
            }
        }

        System.out.println("\nZonas alcanzadas: " + visitadas.size() + " de " + grafoEntregas.size());
        System.out.println("BFS cuenta SALTOS, no kilómetros.");
    }

    /**
     * DFS - BÚSQUEDA EN PROFUNDIDAD - Costo O(V + E)
     *
     * Recorre el grafo yendo lo más lejos posible por un camino antes de
     * devolverse a probar otro. Es lo contrario de BFS.
     *
     * Se implementa con recursividad: la pila de llamadas de Java hace el
     * trabajo de la pila (LIFO) que necesita el algoritmo.
     *
     * Para qué sirve aquí: comprobar CONECTIVIDAD, o sea si desde la bodega
     * se puede llegar a todas las zonas. Si al terminar quedaron zonas sin
     * visitar, esas zonas están aisladas y no se les puede entregar.
     */
    static void recorridoDFS() {
        String inicio = "BODEGA";
        ArrayList<String> visitadas = new ArrayList<>();

        System.out.println("\nDFS desde " + inicio + " (en profundidad):");
        visitarDFS(inicio, visitadas, 0);

        System.out.println("\nZonas alcanzadas: " + visitadas.size() + " de " + grafoEntregas.size());
        if (visitadas.size() == grafoEntregas.size()) {
            System.out.println("El grafo es CONEXO: se puede entregar en todas las zonas.");
        } else {
            System.out.println("Hay zonas AISLADAS a las que no se puede llegar.");
        }
    }

    /** Parte recursiva del DFS. La profundidad solo sirve para la sangría. */
    static void visitarDFS(String zona, ArrayList<String> visitadas, int profundidad) {
        visitadas.add(zona);

        String sangria = "";
        for (int i = 0; i < profundidad; i++) {
            sangria = sangria + "   ";
        }
        System.out.println("  " + sangria + "-> " + zona);

        for (Arista a : grafoEntregas.get(zona)) {
            if (!visitadas.contains(a.destino)) {
                visitarDFS(a.destino, visitadas, profundidad + 1);
            }
        }
    }

    /**
     * ALGORITMO DE DIJKSTRA - Costo O(V²) en esta versión
     *
     * Calcula la ruta de MENOR DISTANCIA desde la bodega hasta una zona,
     * sumando los kilómetros de las vías. A diferencia de BFS, que cuenta
     * saltos, Dijkstra sí toma en cuenta el peso de cada arista.
     *
     * Cómo funciona:
     *   1. Todas las zonas empiezan con distancia infinita, menos el origen
     *      que empieza en 0.
     *   2. Se elige la zona no visitada con la menor distancia conocida.
     *   3. Desde esa zona se revisan sus vecinas: si llegar por aquí resulta
     *      más corto que la distancia que ya tenían, se actualiza. Ese paso
     *      se llama RELAJACIÓN.
     *   4. Se marca la zona como visitada y se repite hasta terminar.
     *
     * Para reconstruir la ruta se guarda, por cada zona, desde qué zona se
     * llegó a ella (el arreglo "anterior"). Al final se sigue esa cadena
     * hacia atrás desde el destino hasta el origen y se invierte.
     *
     * Sobre el costo: en cada vuelta se busca el mínimo recorriendo todas las
     * zonas, y eso se repite V veces, entonces O(V²). Se puede bajar a
     * O((V + E) log V) usando una cola de prioridad, pero se dejó la versión
     * con búsqueda lineal porque es la forma clásica y se entiende mejor.
     *
     * Importante: Dijkstra solo funciona si NINGÚN peso es negativo. Aquí se
     * cumple, porque una distancia en kilómetros nunca es negativa.
     */
    static void rutaMasCorta() {
        String origen = "BODEGA";

        System.out.println("\nZonas disponibles:");
        for (String zona : grafoEntregas.keySet()) {
            if (!zona.equals(origen)) {
                System.out.println("  " + zona);
            }
        }

        String destino = leerTexto("Zona de entrega: ").toUpperCase();

        if (!grafoEntregas.containsKey(destino)) {
            System.out.println("Esa zona no existe en la red.");
            return;
        }
        if (destino.equals(origen)) {
            System.out.println("El origen y el destino son la misma zona.");
            return;
        }

        // distancia: la menor distancia conocida desde el origen hasta cada zona
        HashMap<String, Integer> distancia = new HashMap<>();
        // anterior: desde qué zona se llegó, para poder reconstruir la ruta
        HashMap<String, String> anterior = new HashMap<>();
        // visitadas: zonas que ya tienen su distancia definitiva
        ArrayList<String> visitadas = new ArrayList<>();

        final int INFINITO = 999999;

        // Paso 1: todas en infinito, el origen en cero
        for (String zona : grafoEntregas.keySet()) {
            distancia.put(zona, INFINITO);
            anterior.put(zona, null);
        }
        distancia.put(origen, 0);

        while (visitadas.size() < grafoEntregas.size()) {

            // Paso 2: buscar la zona no visitada con la menor distancia
            String actual = null;
            int menorDistancia = INFINITO;

            for (String zona : grafoEntregas.keySet()) {
                if (!visitadas.contains(zona) && distancia.get(zona) < menorDistancia) {
                    menorDistancia = distancia.get(zona);
                    actual = zona;
                }
            }

            // Si no se encontró ninguna, el resto de zonas es inalcanzable
            if (actual == null) {
                break;
            }

            visitadas.add(actual);

            // Paso 3: relajación de las vecinas
            for (Arista a : grafoEntregas.get(actual)) {
                if (visitadas.contains(a.destino)) {
                    continue;
                }
                int distanciaNueva = distancia.get(actual) + a.distancia;
                if (distanciaNueva < distancia.get(a.destino)) {
                    distancia.put(a.destino, distanciaNueva);
                    anterior.put(a.destino, actual);
                }
            }
        }

        // Resultado
        if (distancia.get(destino) == INFINITO) {
            System.out.println("\nNo hay ninguna ruta desde " + origen + " hasta " + destino + ".");
            return;
        }

        // Reconstruir la ruta siguiendo la cadena hacia atrás
        ArrayList<String> ruta = new ArrayList<>();
        String paso = destino;
        while (paso != null) {
            ruta.add(paso);
            paso = anterior.get(paso);
        }

        System.out.println("\n--- RUTA MÁS CORTA ---");
        System.out.print("Recorrido: ");
        // Se recorre al revés porque la ruta se armó desde el destino
        for (int i = ruta.size() - 1; i >= 0; i--) {
            System.out.print(ruta.get(i));
            if (i > 0) {
                System.out.print(" -> ");
            }
        }
        System.out.println();
        System.out.println("Distancia total: " + distancia.get(destino) + " km");
        System.out.println("Zonas evaluadas: " + visitadas.size());
    }

    // ========================================================================
    // GESTIÓN: VENDER Y LISTAR
    // ========================================================================

    /**
     * Para vender se busca el producto con get(), o sea búsqueda por llave
     * con costo O(1). Vender es de las operaciones más repetidas, por eso
     * conviene que sea la más rápida.
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

        // Como p apunta al objeto que está dentro del diccionario, el cambio
        // queda guardado sin necesidad de volver a hacer put.
        p.stock = p.stock - cantidad;

        double total = p.precio * cantidad;

        System.out.println("\n--- VENTA REALIZADA ---");
        System.out.println("Producto:  " + p.nombre);
        System.out.println("Cantidad:  " + cantidad);
        System.out.println("Total:     $" + total);
        System.out.println("Stock que queda: " + p.stock);
    }

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

    // ========================================================================
    // DATOS DE PRUEBA
    // ========================================================================

    static void cargarDatosDePrueba() {
        categorias.put("TEC", "Tecnología");
        categorias.put("HOG", "Hogar");
        categorias.put("PAP", "Papelería");

        agregarProductoInicial("P005", "Teclado mecánico", 45.50, 10, "TEC");
        agregarProductoInicial("P002", "Mouse inalámbrico", 18.00, 25, "TEC");
        agregarProductoInicial("P008", "Lámpara de escritorio", 22.75, 8, "HOG");
        agregarProductoInicial("P001", "Audífonos bluetooth", 32.00, 15, "TEC");
        agregarProductoInicial("P006", "Cuaderno universitario", 3.50, 40, "PAP");
        agregarProductoInicial("P003", "Juego de sábanas", 28.90, 12, "HOG");
        agregarProductoInicial("P007", "Esferos por 12", 5.25, 30, "PAP");

        // Nota: los códigos se cargan desordenados a propósito, para que el
        // árbol binario quede balanceado. Si se insertaran ya ordenados
        // (P001, P002, P003...) el árbol se degeneraría en una lista y la
        // búsqueda pasaría de O(log n) a O(n).
    }

    static void agregarProductoInicial(String codigo, String nombre, double precio, int stock, String categoria) {
        productos.put(codigo, new Producto(codigo, nombre, precio, stock, categoria));
        raizArbol = insertarEnArbol(raizArbol, codigo);
    }

    /**
     * Red de entregas de la tienda, con zonas de Quito.
     * La BODEGA es el punto de partida de todos los envíos.
     */
    static void cargarRedDeEntregas() {
        conectarZonas("BODEGA", "CENTRO", 4);
        conectarZonas("BODEGA", "NORTE", 9);
        conectarZonas("CENTRO", "SUR", 6);
        conectarZonas("CENTRO", "NORTE", 3);
        conectarZonas("NORTE", "AEROPUERTO", 18);
        conectarZonas("NORTE", "CUMBAYA", 11);
        conectarZonas("CUMBAYA", "VALLE", 7);
        conectarZonas("SUR", "VALLE", 14);
        conectarZonas("CUMBAYA", "AEROPUERTO", 12);
    }

    // ========================================================================
    // LECTURA DE DATOS POR TECLADO
    // ========================================================================

    static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().trim();
    }

    /**
     * Se lee la línea completa y después se convierte a número. Si el usuario
     * escribe letras se avisa y se vuelve a preguntar, así el programa no se cae.
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
