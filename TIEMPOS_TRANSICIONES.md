# Resumen para estudiar: temporización de transiciones

## Qué se hizo

Se implementó la semántica temporal para las transiciones de la red que la consigna marca como temporales: `T2`, `T3`, `T5`, `T7` y `T8`.

Esto significa que esas transiciones ya no se disparan inmediatamente al estar habilitadas; deben esperar un tiempo mínimo antes de poder activarse.

## Fórmulas implementadas

Cada transición temporal usa una ventana en milisegundos: `[alfa, beta]`.

- `alfa`: mínimo tiempo que debe transcurrir desde que la transición se sensibiliza.
- `beta`: máximo tiempo durante el cual sigue siendo válida.

Cuando la transición se vuelve sensibilizada, se guarda el instante de tiempo `timeStamp`.

Cálculo:

- `transcurrido = ahora - timeStamp`
- condición de disparo: `alfa <= transcurrido <= beta`
- si falta llegar a `alfa`: `espera = alfa - transcurrido`

En resumen:

- Si `transcurrido < alfa`, el monitor espera `espera` milisegundos.
- Si `alfa <= transcurrido <= beta`, la transición puede dispararse.
- Si `transcurrido > beta`, la ventana expiró, y la transición deja de ser válida temporalmente.

## Cómo se tradujo al código

### 1. `Main.java`

Se definió el vector de tiempos en `codigo/src/monitor/Main.java`.

Las transiciones temporales recibieron ventanas distintas de `[0, 0]`, mientras que las transiciones inmediatas se mantienen en `[0, 0]`.

Valores usados:

- `T2` = `new SensibilizadoConTiempo(100, 10000)`
- `T3` = `new SensibilizadoConTiempo(100, 10000)`
- `T5` = `new SensibilizadoConTiempo(150, 10000)`
- `T7` = `new SensibilizadoConTiempo(120, 10000)`
- `T8` = `new SensibilizadoConTiempo(120, 10000)`

### 2. `SensibilizadoConTiempo.java`

Esta clase guarda la ventana temporal y el `timeStamp` cuando la transición cambia a sensibilizada.

Funciones clave:

- `testVentanaTiempo()`: devuelve `true` si `alfa <= transcurrido <= beta`.
- `antesDeLaVentana()`: devuelve `true` si `transcurrido < alfa`.
- `tiempoRestante()`: devuelve `alfa - transcurrido` cuando falta tiempo.

### 3. `VectorSensibilizadas.java`

Esta clase actualiza el estado de sensibilización de cada transición.

Cuando una transición pasa de no habilitada a habilitada, se llama a `setNuevoTimeStamp()` para fijar el momento de inicio de la ventana.

### 4. `Monitor.java`

El monitor es el responsable de respetar el tiempo antes de disparar.

Antes de intentar disparar una transición, el monitor hace:

- si `estaSensibilizadoPeroAntes(transition)` es verdadero, espera el tiempo restante;
- si la transición ya está dentro de la ventana temporal, procede a dispararla;
- si la transición no está habilitada por marcado, no hace nada.

El comportamiento exacto es:

1. Si falta tiempo para `alfa`, libera el mutex y duerme `espera` ms.
2. Al despertar, vuelve a tomar el mutex y reevalúa.
3. Si la transición está en ventana, la dispara.

## Por qué elegí esos valores

### Elección general

Los tiempos fueron elegidos para:

- simular latencia real de procesos como autorización, scoring y transferencia,
- mantener el programa dentro de 20-40 segundos para 200 invariantes,
- no bloquear el sistema ni hacer que las transiciones temporales dominen toda la ejecución.

### Detalle por transición

- `T2` y `T3`: 100 ms cada una. Representan autorización y captura de tarjeta. Son pasos rápidos pero no instantáneos.
- `T5`: 150 ms. Representa el flujo de alto riesgo, que exige más procesamiento conjunto de `P7` y `P8`.
- `T7` y `T8`: 120 ms cada una. Representan la etapa de transferencia bancaria, algo más lenta que una tarjeta pero aún rápida.

El `beta` alto de `10000 ms` evita que la transición expire antes de dispararse en el flujo normal del sistema.

## Qué estudiar de este cambio

- la diferencia entre transiciones inmediatas y transiciones temporales;
- la ventana `[alfa, beta]` y su significado en la activación temporal;
- cómo se usa el `timeStamp` para medir `transcurrido`;
- por qué el monitor debe esperar solo si la transición está habilitada pero antes de `alfa`.

## Resultado esperado

Con esta implementación, las transiciones `T2`, `T3`, `T5`, `T7` y `T8` contribuyen al tiempo total de ejecución.

El sistema sigue funcionando con el mismo flujo lógico, pero ahora el rendimiento refleja una semántica temporal realista.

## Conclusión

- Se agregó semántica temporal sin cambiar la estructura de la red.
- La lógica temporal se centraliza en `SensibilizadoConTiempo` y `Monitor`.
- El cambio principal está en `Main.java`, donde se fijaron los tiempos de las transiciones temporales.
