# Tienda Virtual — Sistema de Gestión de Datos

**Autor:** Pablo Revilla
**Materia:** Estructura de Datos
**Fase 3:** Procesamiento No Lineal y Optimización de Rutas
**Lenguaje:** Java (solo biblioteca estándar, sin dependencias externas)
**Repositorio:** https://github.com/Alej1405/Tienda_virutal_java.git

Sistema de gestión de una tienda ejecutable por terminal. No tiene interfaz
gráfica: todo funciona por menú de consola.

---

## Cómo ejecutarlo

Desde Java 11 en adelante se puede correr el archivo directamente:

```bash
java TiendaVirtual.java
```

Si se prefiere compilar primero:

```bash
javac -encoding UTF-8 TiendaVirtual.java
java TiendaVirtual
```

El programa arranca con 3 categorías, 7 productos y una red de 7 zonas de
entrega ya cargados, para poder probar sin tener que ingresar datos.

---

## Menú del sistema

```
GESTIÓN
  1. Agregar categoría
  2. Agregar producto
  3. Buscar productos
  4. Vender producto
  5. Listar todo
ESTRUCTURAS NO LINEALES
  6. Árbol de productos (recorridos)
  7. Red de entregas (BFS / DFS / Dijkstra)
  0. Salir
```

---

## Estructuras de datos utilizadas

El sistema usa tres estructuras distintas. Cada una resuelve un problema que
las otras no resuelven bien.

### 1. Diccionario (HashMap) — almacenamiento principal

```
categorias :  código de categoría  ->  nombre
productos  :  código de producto   ->  objeto Producto
```

Es la estructura principal porque la operación más repetida de una tienda es
*"dame el producto con este código"*, tanto para buscar como para vender. El
HashMap la resuelve en **O(1)**: calcula la posición del dato a partir de la
llave mediante una función hash y va directo, sin recorrer nada.

**Limitación:** no guarda ningún orden. Para listar los productos ordenados
habría que ordenarlos cada vez, con costo O(n log n). Ese es el motivo por el
que además se usa un árbol.

### 2. Árbol Binario de Búsqueda — jerarquía ordenada

Guarda los códigos de producto de forma ordenada: en cada nodo, todo lo que
está a la izquierda es menor y todo lo que está a la derecha es mayor.

Aporta lo que al diccionario le falta: el recorrido Inorden entrega los
productos ya ordenados alfabéticamente en **O(n)**, sin tener que ordenarlos.

**Contrapartida:** buscar en el árbol cuesta O(log n) y en el diccionario
O(1). El árbol es más lento para buscar, pero es el único que mantiene orden.

### 3. Grafo (lista de adyacencia) — red de entregas

Los vértices son zonas de la ciudad y las aristas son las vías que las
conectan, cada una con su distancia en kilómetros. Es un grafo **no dirigido**
(las vías son de doble sentido) y **ponderado** (cada vía tiene un peso).

Se eligió lista de adyacencia sobre matriz de adyacencia porque el grafo es
**disperso**: cada zona conecta con 2 o 3 zonas, no con todas. La matriz
gastaría O(V²) de memoria casi vacía; la lista gasta O(V + E).

---

## Documentación del rendimiento algorítmico

### Tabla resumen

| Operación | Estructura | Complejidad | Observación |
|---|---|---|---|
| Buscar por código | HashMap | **O(1)** | Acceso directo por función hash |
| Buscar por código | Árbol ABB | **O(log n)** promedio · O(n) peor caso | Descarta media rama en cada nivel |
| Buscar por nombre | HashMap | **O(n)** | El nombre no es la llave: recorre todo |
| Buscar por categoría | HashMap | **O(n)** | La categoría no es la llave: recorre todo |
| Insertar producto | HashMap + ABB | **O(1)** + O(log n) | Se guarda en las dos estructuras |
| Vender producto | HashMap | **O(1)** | Búsqueda por llave más descuento de stock |
| Recorrido Inorden | Árbol ABB | **O(n)** | Visita cada nodo exactamente una vez |
| Recorrido Preorden | Árbol ABB | **O(n)** | Visita cada nodo exactamente una vez |
| Recorrido Postorden | Árbol ABB | **O(n)** | Visita cada nodo exactamente una vez |
| BFS | Grafo | **O(V + E)** | Cada vértice y cada arista una vez |
| DFS | Grafo | **O(V + E)** | Cada vértice y cada arista una vez |
| Dijkstra | Grafo | **O(V²)** | Con búsqueda lineal del mínimo |

Donde `n` = cantidad de productos, `V` = zonas del grafo, `E` = vías del grafo.

---

### Análisis de las búsquedas

El sistema ofrece cuatro búsquedas distintas **a propósito**, para poder
comparar su costo en la práctica. Las búsquedas O(n) imprimen cuántos
productos revisaron, y la búsqueda en el árbol imprime cuántos niveles bajó.

**Buscar por código en el diccionario — O(1).** El código *es* la llave. El
HashMap responde con una sola operación. Da igual si hay 7 productos o 10.000.

**Buscar por código en el árbol — O(log n).** En cada nodo se descarta la
mitad del árbol restante, igual que en una búsqueda binaria. Con 7 productos
baja unos 3 niveles; con 1.000 bajaría unos 10.

**Buscar por nombre y por categoría — O(n).** Ninguno de los dos campos es la
llave, así que hay que revisar producto por producto. Con 10.000 productos se
hacen 10.000 comparaciones.

**Conclusión de diseño:** se elige como llave el campo por el que más se
busca. Aquí es el código, porque es el que se usa al buscar y al vender.

---

### Análisis del árbol binario

Los recorridos no son intercambiables: cada uno sirve para algo distinto.

| Recorrido | Orden de visita | Para qué sirve |
|---|---|---|
| **Inorden** | izquierda → raíz → derecha | Devuelve los datos **ordenados** de menor a mayor |
| **Preorden** | raíz → izquierda → derecha | **Copiar o guardar** el árbol: la raíz va antes que sus hijos, así al reinsertar queda idéntico |
| **Postorden** | izquierda → derecha → raíz | **Borrar** el árbol: se eliminan los hijos antes que el padre |

**Sobre el peor caso.** El ABB solo rinde O(log n) si está razonablemente
balanceado. Si los códigos se insertan ya ordenados (P001, P002, P003…), cada
nodo queda con un solo hijo, el árbol degenera en una lista y la búsqueda cae
a **O(n)**.

Por eso los datos de prueba se cargan **desordenados a propósito**
(P005, P002, P008, P001, P006, P003, P007). El árbol resultante es:

```
            P005
          /      \
       P002      P008
       /   \     /
    P001  P003 P006
                  \
                  P007
```

Altura 4 con 7 nodos. Si se hubieran insertado ordenados, la altura sería 7.

**Verificación de los recorridos sobre este árbol:**

- Inorden: `P001 P002 P003 P005 P006 P007 P008` ← ordenado, correcto
- Preorden: `P005 P002 P001 P003 P008 P006 P007`
- Postorden: `P001 P003 P002 P007 P006 P008 P005`

---

### Análisis del grafo

La red de entregas modela zonas de Quito. La BODEGA es el punto de partida de
todos los envíos.

```
BODEGA ──4── CENTRO ──6── SUR
   │            │           │
   9            3          14
   │            │           │
 NORTE ─────────┘         VALLE
   │  \                     │
  18   11                   7
   │     \                  │
AEROPUERTO ──12── CUMBAYA ──┘
```

**BFS — O(V + E).** Recorre por niveles usando una **cola** (FIFO). Responde
"¿a cuántos saltos está cada zona?". Importante: **cuenta saltos, no
kilómetros**.

**DFS — O(V + E).** Recorre en profundidad usando la **pila de llamadas** de
la recursión. Se usa para verificar **conectividad**: si al terminar quedan
zonas sin visitar, esas zonas están aisladas y no se les puede entregar.

**Dijkstra — O(V²).** Calcula la ruta de **menor distancia** sumando los
kilómetros. A diferencia de BFS, sí toma en cuenta el peso de cada arista.

Funcionamiento:

1. Todas las zonas empiezan en distancia infinita, menos el origen en 0.
2. Se elige la zona no visitada con la menor distancia conocida.
3. **Relajación:** se revisan sus vecinas; si llegar por aquí resulta más
   corto que lo que ya tenían, se actualiza la distancia y se anota desde
   dónde se llegó.
4. Se marca como visitada y se repite.

Al final se reconstruye la ruta siguiendo hacia atrás la cadena de "desde
dónde se llegó", y se invierte.

**Restricción:** Dijkstra solo funciona si ningún peso es negativo. Aquí se
cumple porque una distancia en kilómetros nunca es negativa.

**Sobre el costo.** En cada vuelta se busca el mínimo recorriendo todas las
zonas, y eso se repite V veces, de ahí **O(V²)**. Se puede bajar a
**O((V + E) log V)** usando una cola de prioridad (montículo binario). Se dejó
la versión con búsqueda lineal porque es la forma clásica y se entiende mejor,
y porque con 7 vértices la diferencia es irrelevante.

---

### BFS frente a Dijkstra: por qué no dan lo mismo

Es la comparación más importante del trabajo.

Para llegar de **BODEGA a AEROPUERTO**:

- **BFS** responde: nivel 2, o sea 2 saltos (`BODEGA → NORTE → AEROPUERTO`).
- **Dijkstra** responde: 25 km por `BODEGA → CENTRO → NORTE → AEROPUERTO`.

Dijkstra usa **3 saltos en vez de 2** y aun así es la mejor ruta, porque la
vía directa BODEGA→NORTE mide 9 km mientras que pasar por CENTRO suma solo
4 + 3 = 7 km. **Menos saltos no significa menos distancia.**

BFS solo coincide con Dijkstra cuando todas las aristas pesan lo mismo.

**Otros casos verificados:**

| Destino | Ruta calculada | Distancia | Por qué no es la obvia |
|---|---|---|---|
| NORTE | BODEGA → CENTRO → NORTE | **7 km** | Existe vía directa de 9 km, pero rodear es más corto |
| VALLE | BODEGA → CENTRO → SUR → VALLE | **24 km** | La ruta por CUMBAYA da 25 km |
| AEROPUERTO | BODEGA → CENTRO → NORTE → AEROPUERTO | **25 km** | Usa 3 saltos en vez de los 2 de BFS |

---

## Contenido del repositorio

```
TiendaVirtual.java   Código fuente completo (1033 líneas, un solo archivo)
README.md            Esta documentación técnica
```

Todo el sistema está en un único archivo, con las clases `Producto`,
`NodoArbol` y `Arista` como clases internas estáticas.
