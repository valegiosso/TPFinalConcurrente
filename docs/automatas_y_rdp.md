# Autómatas, Gramáticas Tipo 3 y su Relación con el TP Final

---

## 1. ¿Qué es un Autómata?

Un **autómata** es un modelo matemático de un sistema de cómputo.  
Tiene tres partes fundamentales:

| Componente | Qué es |
|---|---|
| **Entrada** | Una cadena de símbolos (alfabeto Σ) |
| **Estados** | Conjunto finito de configuraciones posibles |
| **Transiciones** | Reglas de cambio de estado al leer un símbolo |

El autómata **lee** símbolo a símbolo la cadena de entrada y **decide** si la acepta o rechaza según el estado final en que termina.

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

Un **Autómata Finito Determinista (AFD)** se define como la 5-upla:

```
M = (Q, Sigma, delta, q0, F)
```

| Símbolo | Significado |
|---|---|
| **Q** | Conjunto finito de estados |
| **Sigma** | Alfabeto (conjunto de símbolos de entrada) |
| **delta** | Función de transición: delta: Q × Sigma → Q |
| **q0** | Estado inicial |
| **F** | Conjunto de estados de aceptación |

El autómata **acepta** una cadena `w` si, partiendo de `q0` y aplicando `delta` para cada símbolo de `w`, termina en un estado de `F`.

---

## 4. La Relación con el TP Final

### 4.1 ¿Cuál es el alfabeto Sigma en el TP Final?

> **Las transiciones `{T0, T1, T2, T3, T4, T5, T6, T7, T8, T9}`**

Cada **disparo** de una transición es la lectura de **un símbolo**.  
La secuencia de disparos que genera la ejecución concurrente es la **cadena de entrada** al autómata.

### 4.2 ¿Cuáles son los estados Q?

Los **estados** del autómata corresponden al **marcado (M) de la Red de Petri**.  
Cada vector de marcado `M = [P0, P1, ..., P9]` es un estado.

| Elemento | Valor |
|---|---|
| **Estado inicial q0** | `M0 = [3, 0, 0, 0, 0, 0, 0, 1, 1, 0]` |
| **Estados de aceptación F** | Marcados donde `contadorSalida >= 200` |

### 4.3 ¿Cuál es la función de transición delta?

```
delta(M, Ti) = M + W(:, Ti)
```

Esto es exactamente `rdp.disparar(transition)` en el código:

```java
// RdP.java — esta ES la función de transición delta:
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

```
TEORIA                          TP FINAL
---------------------------------------------------------------------
Alfabeto Sigma          <->     {T0, T1, T2, T3, T4, T5, T6, T7, T8, T9}
Símbolo sigma en Sigma  <->     Un disparo de transición Ti
Cadena w en Sigma*      <->     La secuencia de disparos en log_disparos.txt
Estado q en Q           <->     Un marcado M de la Red de Petri
Estado inicial q0       <->     M0 = [3, 0, 0, 0, 0, 0, 0, 1, 1, 0]
Estado de aceptación F  <->     contadorSalida >= 200
Función delta(q, sigma) <->     M_nuevo = M + W(:, Ti)  -> rdp.disparar()
Lenguaje L(M)           <->     Todas las ejecuciones válidas de la red
Invariante de transición<->     Una palabra del Lenguaje Regular
Expresión Regular       <->     La regex en regex_invariantes.py
Autómata Finito         <->     El motor de regex que valida el log
Gramática Tipo 3        <->     Las reglas de producción de cada invariante
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

**P: ¿Cuál es la función de transición delta del autómata?**  
R: delta(M, Ti) = M + W(:, Ti), implementada en rdp.disparar(). El nuevo estado (marcado)
se obtiene sumando la columna correspondiente de la matriz de incidencia al marcado actual.

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
Red de Petri del TP Final:
  Q     = marcados posibles [P0..P9]
  Sigma = {T0, T1, T2, T3, T4, T5, T6, T7, T8, T9}
  delta = rdp.disparar()  [M_nuevo = M + W(:,Ti)]
  q0    = M0 = [3,0,0,0,0,0,0,1,1,0]
  F     = {M : contadorSalida >= 200}
```
