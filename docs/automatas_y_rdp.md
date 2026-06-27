# Autómatas, Gramáticas Tipo 3 y su Relación con el TP Final

---

## 1. ¿Qué es un Autómata?

Un **autómata** es un modelo matemático abstracto que representa a un sistema que cambia de estados en respuesta a una secuencia de estímulos o entradas.

### Definición Formal (dada en clase)

Un autómata es una **quintupla**:

> **A = (E, S, Q, f, g)**

| Símbolo | Nombre | Descripción |
|---|---|---|
| **E** | Vocabulario de entrada | Conjunto finito de entradas. Sus elementos se llaman **entradas** o **símbolos de entrada**. |
| **S** | Vocabulario de salida | Conjunto finito de salidas. Sus elementos se llaman **salidas** o **símbolos de salida**. |
| **Q** | Conjunto de estados | Conjunto de estados posibles; puede ser finito o infinito. |
| **f** | Función de transición | `f : E × Q → Q` — dado un par (entrada, estado actual) devuelve el **estado siguiente**. |
| **g** | Función de salida | `g : E × Q → S` — dado un par (entrada, estado actual) devuelve un **símbolo de salida**. |

El autómata **lee** símbolo a símbolo la cadena de entrada, aplica `f` para cambiar de estado y `g` para producir salida, y **decide** si la acepta o rechaza según el estado final en que termina.

---

## 2. La Jerarquía de Chomsky (el marco teórico)

Chomsky clasificó los lenguajes en 4 tipos según la gramática que los genera:

| Tipo | Gramática | Autómata equivalente | Ejemplo |
|---|---|---|---|
| **Tipo 0** | Sin restricción | Máquina de Turing | Cualquier programa |
| **Tipo 1** | Sensible al contexto | Autómata linealmente acotado | — |
| **Tipo 2** | Libre de contexto | Autómata de pila (PDA) | Expresiones aritméticas |
| **Tipo 3** | Regular | **Autómata Finito (AF)** | **Expresiones Regulares** |

> **La relación clave:** Gramática Tipo 3 ↔ Expresión Regular ↔ Autómata Finito  
> Son **tres formas distintas** de describir el **mismo lenguaje**.

---

## 3. El Autómata Finito en Detalle

Usando la notación de clase, un **Autómata Finito Determinista (AFD)** es la quintupla:

```
A = (E, S, Q, f, g)
```

| Símbolo | Nombre | Significado en el AFD |
|---|---|---|
| **E** | Vocabulario de entrada | Alfabeto: conjunto finito de símbolos de entrada |
| **S** | Vocabulario de salida | Símbolos de salida (en AFD puro: aceptar/rechazar) |
| **Q** | Conjunto de estados | Estados posibles del autómata |
| **f** | Función de transición | `f : E × Q → Q` — devuelve el estado siguiente |
| **g** | Función de salida | `g : E × Q → S` — devuelve el símbolo de salida |

El autómata **acepta** una cadena `w` si, partiendo del estado inicial `q0 ∈ Q` y aplicando `f` para cada símbolo de `w`, termina en un estado de aceptación.

---

## 4. La Relación con el TP Final

### 4.1 ¿Cuál es E (vocabulario de entrada) en el TP Final?

> **E = {T0, T1, T2, T3, T4, T5, T6, T7, T8, T9}**

Cada **disparo** de una transición es la lectura de **un símbolo de E**.  
La secuencia de disparos que genera la ejecución concurrente es la **cadena de entrada** al autómata.

### 4.2 ¿Cuál es Q (conjunto de estados) en el TP Final?

Los **estados** del autómata corresponden al **marcado de la Red de Petri**.  
Cada vector de marcado `M = [P0, P1, ..., P9]` es un elemento de Q.

| Elemento A = (E,S,Q,f,g) | Valor en el TP Final |
|---|---|
| **Estado inicial** `q0 ∈ Q` | `M0 = [3, 0, 0, 0, 0, 0, 0, 1, 1, 0]` |
| **Estados de aceptación** ⊆ Q | Marcados donde `contadorSalida >= 200` |

### 4.3 ¿Cuál es f (función de transición) en el TP Final?

```
f(q, e) = f(M, Ti) = M + W(:, Ti)
```

Esto es exactamente `rdp.disparar(transition)` en el código:

```java
// RdP.java — esta ES la función de transición f : E × Q → Q
marcadoActual[i] = marcadoActual[i] + matIncidencia[i][transition];
```

---

## 5. Los Invariantes de Transición = el Lenguaje Regular

Un **invariante de transición** (T-invariante) es una secuencia de disparos que vuelve el marcado al estado inicial. En la red del TP Final existen **tres invariantes**:

```
I1 (Tarjetas):       T0 -> T1 -> T2 -> T3 -> T9
I2 (Alto Riesgo):    T0 -> T4 -> T5 -> T9
I3 (Transferencias): T0 -> T6 -> T7 -> T8 -> T9
```

### 5.1 Los invariantes definen un Lenguaje Regular

Cada invariante es una **palabra** del lenguaje.  
El conjunto de todas las ejecuciones válidas de la red es un **Lenguaje Regular de Tipo 3**.

La **Expresión Regular** que describe ese lenguaje (con interleaving concurrente):

```
( T0 .* (T1 .* T2 .* T3 | T4 .* T5 | T6 .* T7 .* T8) .* T9 ){200}
```

### 5.2 Esto es exactamente lo que hace regex_invariantes.py

```python
# Esta regex es el lenguaje regular del TP Final:
regex = r"(T0)(.*?)((T1)(.*?)(T2)(.*?)(T3)|(T4)(.*?)(T5)|(T6)(.*?)(T7)(.*?)(T8))(.*?)(T9)"
```

El script lee `log_disparos.txt` (la cadena de disparos generada por la ejecución)  
y aplica el **autómata finito implícito** en la regex para verificar que cada secuencia cumple  
uno de los tres invariantes.

- EXITO si no queda ningún símbolo sin consumir → ejecución **válida**
- ERROR si quedan símbolos → ejecución **inválida** (se rompió un invariante)

---

## 6. El Mapa Completo: Teoría ↔ TP Final

Usando la notación `A = (E, S, Q, f, g)`:

```
NOTACIÓN A=(E,S,Q,f,g)         TP FINAL
---------------------------------------------------------------------
E  (vocab. de entrada)   <->   {T0, T1, T2, T3, T4, T5, T6, T7, T8, T9}
Símbolo e ∈ E            <->   Un disparo de transición Ti
Cadena w ∈ E*            <->   La secuencia de disparos en log_disparos.txt
S  (vocab. de salida)    <->   {VALIDO, INVALIDO} (resultado de la regex)
Q  (estados)             <->   Marcados posibles M = [P0..P9]
Estado inicial q0 ∈ Q    <->   M0 = [3, 0, 0, 0, 0, 0, 0, 1, 1, 0]
Estados de aceptación⊆Q  <->   contadorSalida >= 200
f(q, e)  (transición)    <->   M_nuevo = M + W(:, Ti)  -> rdp.disparar()
g(q, e)  (salida)        <->   VALIDO si el marcado llega al estado final
Lenguaje L(A)            <->   Todas las ejecuciones válidas de la red
Invariante de transición <->   Una palabra del Lenguaje Regular
Expresión Regular        <->   La regex en regex_invariantes.py
Autómata Finito          <->   El motor de regex que valida el log
Gramática Tipo 3         <->   Las reglas de producción de cada invariante
```

---

## 7. ¿Por qué el Lenguaje es Tipo 3 (Regular)?

Porque las reglas de producción de cada invariante tienen la forma:

```
A -> a B     (producción regular a derecha)
A -> a       (producción terminal)
```

### I1 (Tarjetas):
```
S  -> T0 A
A  -> T1 B
B  -> T2 C
C  -> T3 D
D  -> T9
```

### I2 (Alto Riesgo):
```
S  -> T0 A
A  -> T4 B
B  -> T5 C
C  -> T9
```

### I3 (Transferencias):
```
S  -> T0 A
A  -> T6 B
B  -> T7 C
C  -> T8 D
D  -> T9
```

No hay recursión ni anidamiento:
- **Gramática Regular (Tipo 3)**
- equivale a un **AFD**
- describible con una **Expresión Regular**

---

## 8. Preguntas Típicas del TP Final con su Respuesta

**P: ¿Cuál es la entrada del autómata en el TP Final?**  
R: Las transiciones disparadas (T0 a T9). Cada disparo es un símbolo del alfabeto.

**P: ¿Por qué el lenguaje de la red es de Tipo 3?**  
R: Porque cada invariante de transición se puede expresar como una gramática lineal a derecha
(producción A -> a B), sin necesidad de pila ni memoria adicional. Eso lo hace Regular.

**P: ¿Cómo se verifica que la ejecución es correcta?**  
R: Se aplica la expresión regular de regex_invariantes.py al log de disparos. Si la regex consume
completamente la cadena (sin residuo), todos los invariantes se cumplieron correctamente.

**P: ¿Qué relación hay entre invariante de transición y lenguaje regular?**  
R: Cada T-invariante es una **palabra** del lenguaje. El conjunto de ejecuciones válidas
(200 invariantes completados con interleaving) es el **lenguaje regular** que reconoce el autómata.

**P: ¿Cuál es la función de transición f del autómata?**  
R: `f(q, e) = f(M, Ti) = M + W(:, Ti)`, implementada en `rdp.disparar()`. El nuevo estado (marcado)
se obtiene sumando la columna correspondiente de la matriz de incidencia al marcado actual.

**P: ¿Cuál es la función de salida g del autómata?**  
R: `g(q, e)` devuelve VALIDO si tras aplicar `f` se alcanza un estado de aceptación (contadorSalida >= 200),
o continúa acumulando disparos hasta completar los 200 invariantes.

---

## 9. Diagrama de Relaciones

```
Gramática Tipo 3
(producciones lineales a derecha)
        |
        | genera el mismo lenguaje
        v
Expresión Regular ──────────────────> Motor de Regex (Python)
(T0.*(T1.*T2.*T3|T4.*T5|T6.*T7.*T8).*T9)    verifica log_disparos.txt
        |
        | equivale a
        v
Autómata Finito Determinista (AFD)
        |
        | implementado por
        v
Red de Petri del TP Final — A = (E, S, Q, f, g):
  E  = {T0, T1, T2, T3, T4, T5, T6, T7, T8, T9}
  S  = {VALIDO, INVALIDO}
  Q  = marcados posibles [P0..P9]
  f  = rdp.disparar()  [M_nuevo = M + W(:,Ti)]
  g  = resultado de la regex sobre log_disparos.txt
  q0 = M0 = [3,0,0,0,0,0,0,1,1,0]
  Qa = {M : contadorSalida >= 200}  (estados de aceptación)
```
Viewed automatas_y_rdp.md:110-129

## Desglose punto por punto de la regex

```
( T0 .* (T1 .* T2 .* T3 | T4 .* T5 | T6 .* T7 .* T8) .* T9 ){200}
```

---

### 🔵 `T0`
Literal — disparo obligatorio de la transición T0.  
**Es el inicio de CUALQUIER invariante.** Ningún camino puede comenzar sin T0.

---

### 🔸 `.*`  *(primer .*)* 
`.*` = "cualquier cosa, cualquier cantidad de veces".  
Representa el **interleaving concurrente**: entre T0 y el cuerpo del invariante pueden intercalarse disparos de *otros* hilos que están ejecutando sus propios invariantes en paralelo.

---

### 🔀 `( ... | ... | ... )` — el OR de los 3 invariantes

```
(  T1 .* T2 .* T3   |   T4 .* T5   |   T6 .* T7 .* T8  )
```

Estos son los **3 caminos posibles** dentro de la red:

| Rama | Invariante | Proceso |
|---|---|---|
| `T1 .* T2 .* T3` | **I1** | Tarjetas (3 pasos internos) |
| `T4 .* T5` | **I2** | Alto Riesgo (2 pasos internos) |
| `T6 .* T7 .* T8` | **I3** | Transferencias (3 pasos internos) |

El `|` significa **OR exclusivo**: cada ejecución toma exactamente **uno** de los tres caminos.

Los `.*` **dentro** de cada rama cumplen el mismo rol: permiten que otros disparos concurrentes se cuelen entre los pasos del mismo invariante.

---

### 🔸 `.*` *(segundo .*, al final)*
Otro interleaving: entre el final del cuerpo (`T3`, `T5` o `T8`) y el cierre `T9` también pueden haberse intercalado disparos de otros hilos.

---

### 🔵 `T9`
Literal — disparo obligatorio de cierre. **Todos los invariantes terminan en T9**, es la transición de salida de la red.

---

### 🔁 `{200}`
El grupo completo se repite **exactamente 200 veces**.  
Cada repetición = **un invariante completado** (una operación bancaria finalizada).  
La red debe completar 200 para que la ejecución sea válida.

---

### Visión completa

```
(  T0  .*  (  T1.*T2.*T3  |  T4.*T5  |  T6.*T7.*T8  )  .*  T9  ){200}
   │    │         │                │             │        │    │     │
   │    │         └────────────────┴─────────────┘        │    │     │
  inicio  │              uno de los 3 caminos              │   fin   repetir
          │                                                │        200 veces
          └──────── interleaving de otros hilos ───────────┘
```

> **En resumen:** la regex describe una cadena donde aparecen exactamente 200 bloques del tipo `T0 → {algún camino} → T9`, con cualquier cantidad de disparos ajenos intercalados entre medias.