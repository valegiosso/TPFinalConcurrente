# Archivo para registar desiciones tomadas a lo largo del Trabjo practico
## Analisis pipe
### Matriz de incidence & marking
#### Petri net incidence and marking

**Forwards incidence matrix I+**

|   | T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| P0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| P1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P2 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P3 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P4 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |
| P5 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 |
| P6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 |
| P7 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 0 |
| P8 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 1 | 0 |
| P9 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 1 | 0 |

**Backwards incidence matrix I-**

|   | T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| P0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P1 | 0 | 1 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 0 |
| P2 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P3 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 |
| P4 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 |
| P5 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 |
| P6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 |
| P7 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |
| P8 | 0 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 0 |
| P9 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 |

**Combined incidence matrix I**

|   | T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| P0 | -1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
| P1 | 1 | -1 | 0 | 0 | -1 | 0 | -1 | 0 | 0 | 0 |
| P2 | 0 | 1 | -1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P3 | 0 | 0 | 1 | -1 | 0 | 0 | 0 | 0 | 0 | 0 |
| P4 | 0 | 0 | 0 | 0 | 1 | -1 | 0 | 0 | 0 | 0 |
| P5 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | -1 | 0 | 0 |
| P6 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | -1 | 0 |
| P7 | 0 | -1 | 0 | 1 | -1 | 1 | 0 | 0 | 0 | 0 |
| P8 | 0 | 0 | 0 | 0 | -1 | 1 | -1 | 0 | 1 | 0 |
| P9 | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 1 | -1 |

**Marking**

|   | P0 | P1 | P2 | P3 | P4 | P5 | P6 | P7 | P8 | P9 |
|---|----:|----:|----:|----:|----:|----:|----:|----:|----:|----:|
| Initial | 3 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 1 | 0 |
| Current | 0 | 1 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 0 |

**Enabled transitions**

| T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
|---|---|---|---|---|---|---|---|---|---|
| no | no | no | yes | no | no | no | no | yes | no |

#### Petri net invariant analysis results

##### T-Invariants

| T0 | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 | T9 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 1 | 1 | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 1 |
| 1 | 0 | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 1 |
| 1 | 0 | 0 | 0 | 0 | 0 | 1 | 1 | 1 | 1 |

The net is covered by positive T-Invariants, therefore it might be bounded and live.

##### P-Invariants

| P0 | P1 | P2 | P3 | P4 | P5 | P6 | P7 | P8 | P9 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 0 | 0 | 1 | 1 | 1 | 0 | 0 | 1 | 0 | 0 |
| 0 | 0 | 0 | 0 | 1 | 1 | 1 | 0 | 1 | 0 |
| 1 | 1 | 1 | 1 | 1 | 1 | 1 | 0 | 0 | 1 |

The net is covered by positive P-Invariants, therefore it is bounded.

##### P-Invariant equations

M(P2) + M(P3) + M(P4) + M(P7) = 1  
M(P4) + M(P5) + M(P6) + M(P8) = 1  
M(P0) + M(P1) + M(P2) + M(P3) + M(P4) + M(P5) + M(P6) + M(P9) = 3  

Analysis time: 0.003


## desiciones codigo
Resumen — Implementación base del código Java
Fecha: 06/06/2026

Qué se hizo
Se creó la estructura de código base en codigo/src/monitor/ (package monitor, Java 8 vanilla) siguiendo el diagrama de clases actualizado en DiagramdeClases.md y el diagrama de secuencia en DiagramdeSecuencia.md.

Archivos creados (15 clases)
Clase	Rol
MonitorInterface	Interfaz con único método público fireTransition(int)
Monitor	Monitor de concurrencia agnóstico. Gestiona Mutex, RdP, Política y Logger. Cuenta invariantes completados y detiene el sistema al llegar a 200
Mutex	ReentrantLock (fair) + arreglo de Condition[] (una por transición)
RdP	Red de Petri genérica. Disparo algebraico: M = M - Pre(:,t) + Post(:,t)
Matrizi	Wrapper de int[][] para matrices Pre y Post
VectorDeEstado	Marcado actual + verificación de invariantes de plaza (3 ecuaciones de la bitácora)
VectorSensibilizadas	Estado de sensibilización por marcado + tiempo. Recalcula tras cada disparo
SensibilizadoConTiempo	Ventana temporal [alfa, beta] en ms por transición
Politica	Interfaz Strategy para resolver conflictos
PoliticaAleatoria	Selección aleatoria entre transiciones habilitadas con hilos esperando
PoliticaPriorizada	Prioriza flujo de alto riesgo (T4, T5); fallback aleatorio
HiloBase	Clase abstracta base. Ejecuta sus transiciones asignadas en bucle
HiloGenerador	Hilo de admisión (T0) / salida (T9)
HiloProcesadorTarjetas	Flujo tarjeta: T1 → T2 → T3
HiloProcesadorAltoRiesgo	Flujo alto riesgo: T4 → T5
HiloProcesadorTransferencias	Flujo transferencia: T6 → T7 → T8
Main	Orquestador: define matrices, crea componentes, arranca 5 hilos, espera con join()
Hilos del sistema (5)
HiloGenerador → dispara T0 (admisión)
HiloProcesadorTarjetas → dispara T1, T2, T3
HiloProcesadorAltoRiesgo → dispara T4, T5
HiloProcesadorTransferencias → dispara T6, T7, T8
HiloSalida (usa HiloGenerador) → dispara T9 (salida/liquidación)
Datos de la red cargados
Matrices Pre y Post extraídas de la bitácora (análisis PIPE)
Marcado inicial: {P0=3, P1=0, ..., P7=1, P8=1, P9=0}
Invariantes de plaza verificados después de cada disparo
Verificación
Compila sin errores con javac
Ejecuta correctamente: 200 invariantes completados en ~51ms (sin tiempos temporales)
Log generado en log_disparos.txt con formato T<n> <timestamp_ms>
Pendiente (TODO)
Asignar tiempos [alfa, beta] a las transiciones temporales T2, T3, T5, T7, T8 para que la ejecución dure entre 20-40 segundos
Probar con PoliticaPriorizada (descomentar en Main)
Validar invariantes de transición con regex sobre el log
¿Querés que lo agregue directamente al archivo 

bitacora.md
?