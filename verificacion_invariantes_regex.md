# Verificación de Invariantes de Transición mediante Expresiones Regulares

## 1. Objetivo

El enunciado del TP Final establece en el **Requerimiento 13**:

> *"Verificar el cumplimiento de los invariantes de transiciones mediante el análisis de un archivo log de las transiciones disparadas al finalizar la ejecución. El análisis de los invariantes debe hacerse mediante expresiones regulares."*

Para cumplir con este requisito se implementó el script [`regex.py`](file:///d:/facultad/concurrente/TPFinalConcurrente/codigo/regex.py), que lee el archivo `log_disparos.txt` generado por el sistema Java al finalizar la simulación y valida automáticamente que las secuencias de disparo respeten los T-Invariantes de la red.

---

## 2. Contexto Teórico: T-Invariantes de la Red

Los **Invariantes de Transición (T-Invariants)** representan secuencias cíclicas de disparos que, al completarse, devuelven el marcado de la red a su estado original. Es decir, representan el ciclo de vida completo de una transacción: desde su arribo, pasando por su procesamiento en uno de los tres flujos, hasta su liquidación final.

Los T-Invariantes calculados con PIPE para nuestra red son:

| T-Invariante | Transiciones | Flujo que representa |
| :--- | :--- | :--- |
| $T_{inv1}$ | $\{T_0, T_1, T_2, T_3, T_9\}$ | Pago con tarjeta de crédito/débito |
| $T_{inv2}$ | $\{T_0, T_4, T_5, T_9\}$ | Pago de alto riesgo |
| $T_{inv3}$ | $\{T_0, T_6, T_7, T_8, T_9\}$ | Transferencia bancaria |

Cada transacción que ingresa al sistema ($T_0$) debe salir ($T_9$) habiendo recorrido **exactamente uno** de los tres caminos intermedios, respetando el orden secuencial de sus etapas internas.

---

## 3. El Problema del Entrelazamiento (Interleaving)

### ¿Por qué no se puede aplicar una Regex directamente al log completo?

Dado que el sistema ejecuta **5 hilos concurrentes**, las transiciones de los tres flujos se intercalan (entrelazan) en el archivo de log. Un fragmento típico del log se ve así:

```
T0, T1, T0, T6, T2, T7, T3, T8, T9, T9
```

En este ejemplo, las transiciones de tarjetas ($T_1, T_2, T_3$) y de transferencias ($T_6, T_7, T_8$) aparecen mezcladas. Si intentáramos aplicar una única expresión regular al string completo, no encontraríamos ningún patrón válido porque el orden global no corresponde a ningún invariante individual.

### La solución: Filtrado por flujo

La estrategia consiste en **separar la secuencia global en tres sub-secuencias independientes**, una por cada flujo de procesamiento. Esto se logra recorriendo el log una sola vez y clasificando cada transición según el flujo al que pertenece:

- **Flujo Tarjetas:** se conservan solo las apariciones de $T_1$, $T_2$ y $T_3$.
- **Flujo Alto Riesgo:** se conservan solo las apariciones de $T_4$ y $T_5$.
- **Flujo Transferencias:** se conservan solo las apariciones de $T_6$, $T_7$ y $T_8$.

Las transiciones comunes ($T_0$ y $T_9$) se usan para conteo global, no para la validación interna de flujos.

### Ejemplo práctico del filtrado

Dada la secuencia entrelazada del log:

```
T0, T1, T0, T6, T2, T7, T3, T8, T9, T9
```

Después del filtrado obtenemos:

| Sub-secuencia | Contenido | String resultante |
| :--- | :--- | :--- |
| Tarjetas | $T_1, T_2, T_3$ | `"T1T2T3"` |
| Alto Riesgo | *(vacío)* | `""` |
| Transferencias | $T_6, T_7, T_8$ | `"T6T7T8"` |

El entrelazamiento desaparece y cada sub-secuencia puede validarse de forma independiente con una expresión regular.

---

## 4. Las Expresiones Regulares Utilizadas

### Patrones por flujo

Cada flujo tiene una expresión regular que describe la repetición válida de su ciclo interno:

| Flujo | Expresión Regular | Significado |
| :--- | :--- | :--- |
| Tarjetas | `^(T1T2T3)+$` | Una o más repeticiones exactas de la secuencia $T_1 \rightarrow T_2 \rightarrow T_3$ |
| Alto Riesgo | `^(T4T5)+$` | Una o más repeticiones exactas de la secuencia $T_4 \rightarrow T_5$ |
| Transferencias | `^(T6T7T8)+$` | Una o más repeticiones exactas de la secuencia $T_6 \rightarrow T_7 \rightarrow T_8$ |

### Desglose de la sintaxis

Tomando como ejemplo `^(T1T2T3)+$`:

| Componente | Significado |
| :--- | :--- |
| `^` | Ancla de inicio: el patrón debe coincidir desde el primer carácter del string |
| `(T1T2T3)` | Grupo de captura: define la secuencia exacta de un ciclo completo |
| `+` | Cuantificador: el grupo debe repetirse **una o más veces** |
| `$` | Ancla de fin: el patrón debe coincidir hasta el último carácter del string |

Si la sub-secuencia filtrada fuera `"T1T2T3T1T2T3T1T2T3"` (3 ciclos completos), la Regex daría `match = True`. Pero si fuera `"T1T3T2"` (orden incorrecto), daría `match = False`, detectando la violación del invariante.

---

## 5. Funciones de `re` de Python Utilizadas

El script utiliza las cuatro funciones principales del módulo `re` de Python:

### `re.match(patron, string)`

Busca el patrón **únicamente al principio** del string. Se usa en dos contextos:

1. **Parseo del log:** Para extraer el identificador de transición de cada línea del archivo.
   ```python
   coincidencia = re.match(r"^(T\d+)", linea)
   # En la línea "T0 1782224504611", captura "T0"
   ```

2. **Validación del invariante:** Para verificar que la sub-secuencia filtrada coincida exactamente con el patrón cíclico.
   ```python
   match = re.match(r"^(T1T2T3)+$", "T1T2T3T1T2T3")
   # Retorna un objeto Match (truthy) -> el invariante se cumple
   ```

### `re.search(patron, string)`

Escanea **todo el string** y retorna la **primera coincidencia** que encuentre, sin importar dónde esté. Se usa para **diagnosticar errores** cuando un flujo falla la validación:

```python
error = re.search(r"(T[123])(T[123])", "T1T3T2")
# Encuentra "T1T3" -> indica dónde se rompió el orden esperado
```

A diferencia de `re.match`, que solo busca al inicio, `re.search` recorre todo el string buscando la primera aparición del patrón. Esto es útil para localizar la posición exacta de la ruptura del invariante.

### `re.sub(patron, reemplazo, string)`

Busca **todas las ocurrencias** del patrón y las **reemplaza** por el texto indicado. Se usa conceptualmente de forma interna por `re.subn`.

### `re.subn(patron, reemplazo, string)`

Hace lo mismo que `re.sub()`, pero además retorna una **tupla** con el string resultante y la **cantidad de reemplazos** realizados. Se usa para **contar los ciclos completados** de cada flujo:

```python
_, cantidad = re.subn(r"T1T2T3", "", "T1T2T3T1T2T3T1T2T3")
# cantidad = 3 -> se completaron 3 ciclos del flujo de tarjetas
```

El primer valor de la tupla (el string con los reemplazos aplicados) se descarta con `_` porque solo nos interesa el conteo.

---

## 6. Estructura del Script `regex.py`

El script se organiza en 6 secciones funcionales:

### Sección 1: Lectura y Parseo del Log (`leer_log`)

- Abre el archivo `log_disparos.txt`.
- Recorre línea por línea.
- Usa `re.match(r"^(T\d+)", linea)` para extraer el identificador de cada transición.
- Retorna una lista ordenada de transiciones: `['T0', 'T1', 'T6', 'T2', ...]`.

### Sección 2: Filtrado por Flujo (`filtrar_por_flujo`)

- Recorre la lista de transiciones una sola vez.
- Clasifica cada transición en su flujo correspondiente usando `if/elif`.
- Construye tres strings concatenados (uno por flujo) y cuenta los disparos de $T_0$ y $T_9$.
- Retorna un diccionario con los strings filtrados y los contadores.

### Sección 3: Verificación con Regex (`verificar_invariantes`)

- Para cada flujo, aplica `re.match` con el patrón correspondiente sobre el string filtrado.
- Si el match es exitoso, usa `re.subn` para contar los ciclos completados.
- Si el match falla, usa `re.search` para localizar la primera ruptura del patrón y generar un mensaje de diagnóstico.
- Retorna un diccionario con los resultados de validación por flujo.

### Sección 4: Verificación de Contadores Globales (`verificar_contadores`)

Valida dos condiciones de consistencia que deben cumplirse matemáticamente:

1. **$T_0 = T_9$**: Toda transacción que ingresa al sistema debe salir. El número de admisiones debe ser igual al número de liquidaciones.
2. **Ciclos(Tarjetas) + Ciclos(Alto Riesgo) + Ciclos(Transferencias) = $T_9$**: La suma de invariantes completados por cada flujo debe ser igual al total de transacciones procesadas.

### Sección 5: Reporte de Resultados (`imprimir_reporte`)

Genera un reporte detallado en consola con:
- Estado de cada flujo (`PASS` / `FAIL`).
- La expresión regular utilizada.
- Cantidad de ciclos completados por flujo.
- Validación de contadores globales.
- Veredicto final del sistema.

### Sección 6: Main

- Determina la ruta del archivo de log (por argumento o por defecto).
- Ejecuta los 5 pasos en orden secuencial.
- Retorna código de salida `0` si todo está correcto, `1` si hay fallos.

---

## 7. Ejemplo de Salida del Script

```
Leyendo log de: log_disparos.txt
Total de transiciones registradas: 937

============================================================
  VERIFICACION DE INVARIANTES DE TRANSICION (Regex)
============================================================

--- Validacion por Flujo (Expresiones Regulares) ---

  [PASS] Tarjetas (T1->T2->T3)
         Patron: ^(T1T2T3)+$
         Ciclos completados: 67
         Estado: OK

  [PASS] Alto Riesgo (T4->T5)
         Patron: ^(T4T5)+$
         Ciclos completados: 66
         Estado: OK

  [PASS] Transferencias (T6->T7->T8)
         Patron: ^(T6T7T8)+$
         Ciclos completados: 67
         Estado: OK

--- Validacion de Contadores Globales ---

  Disparos de T0 (admision):    200
  Disparos de T9 (liquidacion): 200
  T0 == T9:                     PASS

  Ciclos Tarjetas:              67
  Ciclos Alto Riesgo:           66
  Ciclos Transferencias:        67
  Total ciclos:                 200
  Suma ciclos == T9:            PASS

============================================================
  RESULTADO FINAL: TODOS LOS INVARIANTES VERIFICADOS
============================================================
```

---

## 8. Uso

```bash
# Desde el directorio codigo/
python regex.py                        # usa log_disparos.txt por defecto
python regex.py log_disparos.txt       # ruta explícita
python regex.py ../otro_log.txt        # otro archivo de log
```

---

## 9. ¿Qué demuestra esta verificación?

Si el script retorna `PASS` en todos los campos, se demuestran las siguientes propiedades del sistema concurrente:

1. **Correctitud del Monitor:** La exclusión mutua y la sincronización funcionan correctamente. Ningún hilo disparó una transición fuera de orden.
2. **Correctitud de las Políticas:** Tanto la política aleatoria como la priorizada resuelven los conflictos sin romper la semántica de la red.
3. **Cumplimiento de los T-Invariantes:** Cada transacción completó su ciclo de vida de forma íntegra, pasando por todas las etapas de su flujo en el orden correcto.
4. **Conservación de tokens:** El mismo número de transacciones que ingresaron al sistema ($T_0$) fueron liquidadas ($T_9$), lo cual es consistente con los P-Invariantes de la red.
