#!/usr/bin/env python3
# Windows: ejecutar con:  py analisis_temporal.py
"""
analisis_temporal.py
Script de análisis temporal del sistema de procesamiento de transacciones PSP.

Ejecuta el programa Java múltiples veces (con ambas políticas y distintos
conjuntos de tiempos), recolecta los tiempos, los compara con las cotas
teóricas mínima/máxima, y genera gráficos PNG en la carpeta 'graficos/'.

Uso:
    python analisis_temporal.py
(Ejecutar desde la carpeta raíz del proyecto, ej: TPFinalConcurrente/)
"""

import subprocess
import re
import os
import sys
import json
import time
import statistics
import matplotlib
matplotlib.use('Agg')  # backend sin pantalla
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
from pathlib import Path

# ---------------------------------------------------------------------------
# CONFIGURACIÓN
# ---------------------------------------------------------------------------

PROJECT_ROOT = Path(__file__).resolve().parent.parent  # TPFinalConcurrente/
JAVA_SRC  = PROJECT_ROOT / "codigo" / "src"
JAVA_OUT  = PROJECT_ROOT / "codigo" / "out"
MAIN_CLASS = "monitor.Main"
LOG_FILE   = JAVA_SRC / "log_disparos.txt"
OUTPUT_DIR = PROJECT_ROOT / "docs" / "graficos"
RUNS       = 5          # ejecuciones por configuración
MAX_INVARIANTES = 200

# Configuraciones de tiempo a evaluar (alfa en ms para T2,T3,T5,T7,T8)
# Formato: (etiqueta, T2, T3, T5, T7, T8)
CONFIGS = [
    ("Mínimos (50ms)",   50,  50,  75,  60,  60),
    ("Base (100/120/150)", 100, 100, 150, 120, 120),
    ("Lentos (200ms)",  200, 200, 250, 220, 220),
    ("Muy lentos (400ms)", 400, 400, 450, 420, 420),
]

# ---------------------------------------------------------------------------
# COTAS TEÓRICAS
# ---------------------------------------------------------------------------

def calcular_cotas(t2, t3, t5, t7, t8, n=200):
    """
    Cota inferior (peor caso): ejecución completamente secuencial.
    - Solo un token en P0 activo a la vez (sin paralelismo).
    - El camino más largo (flujo tarjetas: T1+T2+T3 = t2+t3) o
      flujo transferencias (T7+T8 = t7+t8), según cuál sea mayor.
    - Cada invariante tarda el tiempo del flujo más lento.
    - Se suma para los 200 invariantes (3 tokens en P0, con cola).

    Nota: con M0(P0)=3 hay hasta 3 transacciones en vuelo.
    El cuello de botella real es el recurso compartido más lento.

    Cota MÍNIMA de tiempo (mejor caso teórico con paralelismo completo):
    - Los 3 flujos corren en paralelo sobre los 3 tokens de P0.
    - Ceil(200/3) rondas × tiempo del flujo más lento.

    Cota MÁXIMA de tiempo (peor caso: todo serial):
    - 200 invariantes × tiempo del invariante más lento.
    """
    # Tiempo de cada invariante (alfa mínimo de cada flujo)
    t_tarjetas      = t2 + t3           # T2 + T3
    t_alto_riesgo   = t5                # solo T5
    t_transferencias = t7 + t8         # T7 + T8

    t_max_invariante = max(t_tarjetas, t_alto_riesgo, t_transferencias)
    t_min_invariante = min(t_tarjetas, t_alto_riesgo, t_transferencias)

    # Peor caso: todos los invariantes pasan por el flujo más lento, en serie
    cota_max_ms = n * t_max_invariante

    # Mejor caso: paralelismo máximo con 3 tokens, ceil(n/3) rondas
    import math
    rondas = math.ceil(n / 3)
    cota_min_ms = rondas * t_max_invariante

    return cota_min_ms, cota_max_ms, {
        "t_tarjetas": t_tarjetas,
        "t_alto_riesgo": t_alto_riesgo,
        "t_transferencias": t_transferencias,
        "t_max": t_max_invariante,
        "t_min": t_min_invariante,
    }

# ---------------------------------------------------------------------------
# COMPILACIÓN JAVA
# ---------------------------------------------------------------------------

def compilar_java():
    print("-> Compilando Java...")
    JAVA_OUT.mkdir(parents=True, exist_ok=True)
    # Compilar todos los .java del paquete monitor
    sources = list(JAVA_SRC.glob("monitor/*.java"))
    cmd = ["javac", "-d", str(JAVA_OUT)] + [str(s) for s in sources]
    JAVA_OUT.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print("ERROR compilación:")
        print(result.stderr)
        sys.exit(1)
    print("  [OK] Compilación exitosa")

# ---------------------------------------------------------------------------
# PARCHE DINÁMICO DE TIEMPOS EN Main.java
# ---------------------------------------------------------------------------

def parchear_main(t2, t3, t5, t7, t8, politica="priorizada"):
    """Modifica Main.java para usar los tiempos indicados y recompila."""
    main_path = JAVA_SRC / "monitor" / "Main.java"
    with open(main_path, "r", encoding="utf-8") as f:
        contenido = f.read()

    # Reemplazar tiempos de transiciones temporales
    def reemplazar_tiempo(contenido, idx, nuevo_alfa):
        patron = rf'(tiempos\[{idx}\]\s*=\s*new SensibilizadoConTiempo\()(\d+)(,\s*Long\.MAX_VALUE\))'
        return re.sub(patron, rf'\g<1>{nuevo_alfa}\g<3>', contenido)

    contenido = reemplazar_tiempo(contenido, 2, t2)
    contenido = reemplazar_tiempo(contenido, 3, t3)
    contenido = reemplazar_tiempo(contenido, 5, t5)
    contenido = reemplazar_tiempo(contenido, 7, t7)
    contenido = reemplazar_tiempo(contenido, 8, t8)

    # Reemplazar política
    if politica == "aleatoria":
        contenido = re.sub(
            r'//\s*Politica politica = new PoliticaAleatoria\(\);',
            'Politica politica = new PoliticaAleatoria();', contenido)
        contenido = re.sub(
            r'Politica politica = new PoliticaPriorizada\([^)]+\);',
            '//Politica politica = new PoliticaPriorizada(new int[]{4, 5});', contenido)
    else:
        contenido = re.sub(
            r'Politica politica = new PoliticaAleatoria\(\);',
            '//Politica politica = new PoliticaAleatoria();', contenido)
        contenido = re.sub(
            r'//\s*Politica politica = new PoliticaPriorizada\([^)]+\);',
            'Politica politica = new PoliticaPriorizada(new int[]{4, 5});', contenido)

    with open(main_path, "w", encoding="utf-8") as f:
        f.write(contenido)

    compilar_java()

# ---------------------------------------------------------------------------
# EJECUCIÓN Y PARSEO
# ---------------------------------------------------------------------------

def ejecutar_una_vez():
    """Ejecuta el programa Java y retorna el tiempo en ms o None si falla."""
    cmd = ["java", "-cp", str(JAVA_OUT), MAIN_CLASS]
    t0 = time.time()
    result = subprocess.run(cmd, capture_output=True, text=True,
                            cwd=str(JAVA_SRC), timeout=180)
    t1 = time.time()

    # Intentar leer el tiempo reportado por el propio programa
    match = re.search(r"Tiempo de ejecucion:\s*(\d+)\s*ms", result.stdout)
    if match:
        return int(match.group(1))
    # Fallback: tiempo wall-clock
    if result.returncode == 0:
        return int((t1 - t0) * 1000)
    print(f"  [FAIL] Fallo: {result.stderr[:200]}")
    return None

def ejecutar_n_veces(n, etiqueta):
    print(f"  Ejecutando {n} veces ({etiqueta})...")
    tiempos = []
    for i in range(n):
        t = ejecutar_una_vez()
        if t is not None:
            tiempos.append(t)
            print(f"    Run {i+1}: {t} ms")
        else:
            print(f"    Run {i+1}: FALLO")
    return tiempos

# ---------------------------------------------------------------------------
# GRÁFICOS
# ---------------------------------------------------------------------------

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

COLORES = {
    "Mínimos (50ms)":        "#2ecc71",
    "Base (100/120/150)":    "#3498db",
    "Lentos (200ms)":        "#e67e22",
    "Muy lentos (400ms)":    "#e74c3c",
}

def grafico_boxplot_configs(resultados):
    """Boxplot de tiempos por configuración."""
    fig, ax = plt.subplots(figsize=(12, 6))
    labels = list(resultados.keys())
    data   = [resultados[l]["tiempos"] for l in labels]
    colors = [COLORES.get(l, "#95a5a6") for l in labels]

    bp = ax.boxplot(data, patch_artist=True, notch=False,
                    medianprops=dict(color="white", linewidth=2))
    for patch, color in zip(bp['boxes'], colors):
        patch.set_facecolor(color)
        patch.set_alpha(0.8)

    # Cotas del enunciado (20-40 s)
    ax.axhline(y=20000, color='green',  linestyle='--', linewidth=1.5,
               label='Cota mínima enunciado (20 s)')
    ax.axhline(y=40000, color='red',    linestyle='--', linewidth=1.5,
               label='Cota máxima enunciado (40 s)')
    ax.fill_between(range(len(labels)+2), 20000, 40000, alpha=0.08,
                    color='green', zorder=0)

    ax.set_xticks(range(1, len(labels)+1))
    ax.set_xticklabels(labels, rotation=15, ha='right')
    ax.set_ylabel("Tiempo de ejecución (ms)")
    ax.set_title("Distribución de Tiempos de Ejecución por Configuración\n"
                 "(200 invariantes, política priorizada)")
    ax.legend()
    ax.grid(axis='y', alpha=0.3)
    plt.tight_layout()
    path = OUTPUT_DIR / "boxplot_configs.png"
    plt.savefig(path, dpi=150)
    plt.close()
    print(f"  [OK] {path}")
    return path

def grafico_cotas_teoricas(resultados):
    """Barra comparativa: cota mínima teórica, media real, cota máxima teórica."""
    labels  = list(resultados.keys())
    c_min   = [resultados[l]["cota_min_ms"]/1000 for l in labels]
    c_max   = [resultados[l]["cota_max_ms"]/1000 for l in labels]
    c_real  = [statistics.mean(resultados[l]["tiempos"])/1000
                if resultados[l]["tiempos"] else 0 for l in labels]

    x = np.arange(len(labels))
    width = 0.25

    fig, ax = plt.subplots(figsize=(13, 6))
    bars_min  = ax.bar(x - width,  c_min,  width, label='Cota Mín (mejor caso teórico)',
                       color='#2ecc71', alpha=0.8)
    bars_real = ax.bar(x,          c_real, width, label='Media Real (ejecuciones)',
                       color='#3498db', alpha=0.9)
    bars_max  = ax.bar(x + width,  c_max,  width, label='Cota Máx (peor caso teórico)',
                       color='#e74c3c', alpha=0.8)

    ax.axhline(y=20, color='green', linestyle=':', linewidth=1.5,
               label='Límite inferior enunciado (20 s)')
    ax.axhline(y=40, color='red',   linestyle=':', linewidth=1.5,
               label='Límite superior enunciado (40 s)')

    ax.set_xticks(x)
    ax.set_xticklabels(labels, rotation=12, ha='right')
    ax.set_ylabel("Segundos")
    ax.set_title("Cotas Teóricas vs Tiempo Real de Ejecución\n"
                 "(200 invariantes, política priorizada)")
    ax.legend(loc='upper left')
    ax.grid(axis='y', alpha=0.3)

    # Anotar valores
    for bar in bars_real:
        h = bar.get_height()
        ax.annotate(f'{h:.1f}s', xy=(bar.get_x() + bar.get_width()/2, h),
                    xytext=(0, 3), textcoords="offset points",
                    ha='center', va='bottom', fontsize=8)

    plt.tight_layout()
    path = OUTPUT_DIR / "cotas_vs_real.png"
    plt.savefig(path, dpi=150)
    plt.close()
    print(f"  [OK] {path}")
    return path

def grafico_variacion_tiempos(resultados):
    """Línea: media real en función del alfa de transición."""
    labels = list(resultados.keys())
    # Extraer el T5 (el mayor alfa en cada config) como eje X representativo
    alfas  = [resultados[l]["config"][3] for l in labels]  # T7 alfa
    medias = [statistics.mean(resultados[l]["tiempos"])/1000
               if resultados[l]["tiempos"] else 0 for l in labels]
    stds   = [statistics.stdev(resultados[l]["tiempos"])/1000
               if len(resultados[l]["tiempos"]) > 1 else 0 for l in labels]
    c_mins = [resultados[l]["cota_min_ms"]/1000 for l in labels]
    c_maxs = [resultados[l]["cota_max_ms"]/1000 for l in labels]

    fig, ax = plt.subplots(figsize=(10, 6))
    ax.plot(alfas, medias, 'o-', color='#3498db', linewidth=2,
            markersize=8, label='Tiempo Real (media)')
    ax.fill_between(alfas,
                    [m - s for m, s in zip(medias, stds)],
                    [m + s for m, s in zip(medias, stds)],
                    alpha=0.2, color='#3498db', label='±1 desviación estándar')
    ax.plot(alfas, c_mins, 's--', color='#2ecc71', linewidth=1.5,
            markersize=6, label='Cota Mín Teórica (mejor caso)')
    ax.plot(alfas, c_maxs, 'v--', color='#e74c3c', linewidth=1.5,
            markersize=6, label='Cota Máx Teórica (peor caso)')

    ax.axhline(y=20, color='green', linestyle=':', alpha=0.6)
    ax.axhline(y=40, color='red',   linestyle=':', alpha=0.6)
    ax.fill_between([min(alfas)-10, max(alfas)+10], 20, 40,
                    alpha=0.06, color='green', zorder=0,
                    label='Rango aceptable (20-40 s)')

    ax.set_xlabel("α de transición más lenta (ms)")
    ax.set_ylabel("Tiempo total de ejecución (s)")
    ax.set_title("Impacto de Variar los Tiempos de Transición\nen el Tiempo Total de Ejecución")
    ax.legend(fontsize=9)
    ax.grid(alpha=0.3)
    plt.tight_layout()
    path = OUTPUT_DIR / "variacion_tiempos.png"
    plt.savefig(path, dpi=150)
    plt.close()
    print(f"  [OK] {path}")
    return path

def grafico_distribucion_invariantes(resultados):
    """Pie chart: distribución de invariantes por flujo (del log)."""
    log = JAVA_SRC / "log_disparos.txt"
    if not log.exists():
        print("  [FAIL] log_disparos.txt no encontrado, omitiendo gráfico de distribución")
        return None

    with open(log, "r") as f:
        transiciones = [re.match(r"^(T\d+)", l).group(1)
                        for l in f if re.match(r"^T\d+", l)]

    tarjetas  = sum(1 for t in transiciones if t == "T3")  # 1 por ciclo
    alto      = sum(1 for t in transiciones if t == "T5")
    transfer  = sum(1 for t in transiciones if t == "T8")
    total = tarjetas + alto + transfer or 1

    fig, ax = plt.subplots(figsize=(7, 7))
    sizes = [tarjetas, alto, transfer]
    labels = [f'Tarjetas (T1->T2->T3)\n{tarjetas} ciclos ({tarjetas/total*100:.1f}%)',
              f'Alto Riesgo (T4->T5)\n{alto} ciclos ({alto/total*100:.1f}%)',
              f'Transferencias (T6->T7->T8)\n{transfer} ciclos ({transfer/total*100:.1f}%)']
    colors = ['#3498db', '#e74c3c', '#2ecc71']
    wedges, _ = ax.pie(sizes, colors=colors, startangle=90,
                       wedgeprops=dict(width=0.6, edgecolor='white', linewidth=2))
    ax.legend(wedges, labels, loc="lower center", bbox_to_anchor=(0.5, -0.15),
              fontsize=9)
    ax.set_title("Distribución de Invariantes por Flujo\n(última ejecución)", pad=20)
    plt.tight_layout()
    path = OUTPUT_DIR / "distribucion_invariantes.png"
    plt.savefig(path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"  [OK] {path}")
    return path

def grafico_comparativa_politicas(res_prior, res_alea):
    """Barras comparando ambas políticas en la config base."""
    configs_base = "Base (100/120/150)"
    prior = res_prior.get(configs_base, {}).get("tiempos", [])
    alea  = res_alea.get(configs_base, {}).get("tiempos", [])

    if not prior or not alea:
        print("  [FAIL] Datos insuficientes para gráfico de políticas")
        return None

    fig, axes = plt.subplots(1, 2, figsize=(12, 5))

    # Histograma
    axes[0].hist([t/1000 for t in prior], bins=5, alpha=0.7,
                 color='#3498db', label='Priorizada')
    axes[0].hist([t/1000 for t in alea],  bins=5, alpha=0.7,
                 color='#e67e22', label='Aleatoria')
    axes[0].axvline(x=20, color='green', linestyle='--', linewidth=1.5)
    axes[0].axvline(x=40, color='red',   linestyle='--', linewidth=1.5)
    axes[0].set_xlabel("Tiempo (s)")
    axes[0].set_ylabel("Frecuencia")
    axes[0].set_title("Distribución de Tiempos por Política\n(Config Base)")
    axes[0].legend()
    axes[0].grid(alpha=0.3)

    # Barra media ± std
    labels   = ['Priorizada', 'Aleatoria']
    medias   = [statistics.mean(prior)/1000, statistics.mean(alea)/1000]
    stds     = [statistics.stdev(prior)/1000 if len(prior) > 1 else 0,
                statistics.stdev(alea)/1000  if len(alea)  > 1 else 0]
    colors   = ['#3498db', '#e67e22']

    bars = axes[1].bar(labels, medias, yerr=stds, capsize=8,
                       color=colors, alpha=0.8)
    axes[1].axhline(y=20, color='green', linestyle='--', linewidth=1.5,
                    label='Límites enunciado')
    axes[1].axhline(y=40, color='red',   linestyle='--', linewidth=1.5)
    axes[1].set_ylabel("Tiempo medio (s)")
    axes[1].set_title("Media y Desviación Estándar\nPor Política (Config Base)")
    axes[1].grid(axis='y', alpha=0.3)
    for bar, media in zip(bars, medias):
        axes[1].text(bar.get_x() + bar.get_width()/2, media + 0.5,
                     f'{media:.1f}s', ha='center', va='bottom', fontweight='bold')

    plt.tight_layout()
    path = OUTPUT_DIR / "comparativa_politicas.png"
    plt.savefig(path, dpi=150)
    plt.close()
    print(f"  [OK] {path}")
    return path

# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------

def main():
    print("=" * 60)
    print("  ANÁLISIS TEMPORAL - PSP Petri Net Monitor")
    print("=" * 60)

    compilar_java()

    resultados_priorizada = {}
    resultados_aleatoria  = {}

    for (etiqueta, t2, t3, t5, t7, t8) in CONFIGS:
        cota_min, cota_max, detalle = calcular_cotas(t2, t3, t5, t7, t8)
        print(f"\n{'-'*50}")
        print(f"CONFIG: {etiqueta}")
        print(f"  Tiempos: T2={t2} T3={t3} T5={t5} T7={t7} T8={t8}")
        print(f"  Cota mín (teórica) : {cota_min/1000:.1f}s")
        print(f"  Cota máx (teórica) : {cota_max/1000:.1f}s")

        # ---- Política Priorizada ----
        print(f"\n  [Política PRIORIZADA]")
        parchear_main(t2, t3, t5, t7, t8, politica="priorizada")
        tiempos_p = ejecutar_n_veces(RUNS, etiqueta)

        resultados_priorizada[etiqueta] = {
            "tiempos": tiempos_p,
            "cota_min_ms": cota_min,
            "cota_max_ms": cota_max,
            "detalle": detalle,
            "config": (t2, t3, t5, t7, t8),
        }

        # ---- Política Aleatoria ----
        if etiqueta == "Base (100/120/150)":
            print(f"\n  [Política ALEATORIA]")
            parchear_main(t2, t3, t5, t7, t8, politica="aleatoria")
            tiempos_a = ejecutar_n_veces(RUNS, etiqueta)
            resultados_aleatoria[etiqueta] = {
                "tiempos": tiempos_a,
                "cota_min_ms": cota_min,
                "cota_max_ms": cota_max,
                "detalle": detalle,
                "config": (t2, t3, t5, t7, t8),
            }

    # ---- Restaurar config base con política priorizada ----
    print("\n-> Restaurando configuración base...")
    parchear_main(100, 100, 150, 120, 120, politica="priorizada")

    # ---- Generar gráficos ----
    print(f"\n{'-'*50}")
    print("Generando gráficos...")
    g1 = grafico_boxplot_configs(resultados_priorizada)
    g2 = grafico_cotas_teoricas(resultados_priorizada)
    g3 = grafico_variacion_tiempos(resultados_priorizada)
    g4 = grafico_distribucion_invariantes(resultados_priorizada)
    g5 = grafico_comparativa_politicas(resultados_priorizada, resultados_aleatoria)

    # ---- Guardar datos JSON ----
    datos_json = {}
    for k, v in resultados_priorizada.items():
        datos_json[k] = {
            "tiempos": v["tiempos"],
            "cota_min_s": round(v["cota_min_ms"]/1000, 2),
            "cota_max_s": round(v["cota_max_ms"]/1000, 2),
            "media_s":    round(statistics.mean(v["tiempos"])/1000, 2) if v["tiempos"] else None,
            "std_s":      round(statistics.stdev(v["tiempos"])/1000, 2) if len(v["tiempos"]) > 1 else 0,
            "cumple_enunciado": all(20000 <= t <= 40000 for t in v["tiempos"]),
        }

    datos_path = OUTPUT_DIR / "resultados.json"
    with open(datos_path, "w") as f:
        json.dump(datos_json, f, indent=2, ensure_ascii=False)

    # ---- Reporte consola ----
    print(f"\n{'='*60}")
    print("  REPORTE FINAL")
    print(f"{'='*60}")
    print(f"{'Configuración':<28} {'Media(s)':>8} {'Min(s)':>7} {'Max(s)':>7} {'Cota↓':>7} {'Cota↑':>7} {'OK?':>5}")
    print("-"*65)
    for k, v in datos_json.items():
        tiempos = resultados_priorizada[k]["tiempos"]
        mn = min(tiempos)/1000 if tiempos else 0
        mx = max(tiempos)/1000 if tiempos else 0
        ok = "[OK]" if v["cumple_enunciado"] else "[FAIL]"
        print(f"{k:<28} {v['media_s']:>7.1f}s {mn:>6.1f}s {mx:>6.1f}s "
              f"{v['cota_min_s']:>6.1f}s {v['cota_max_s']:>6.1f}s {ok:>4}")
    print(f"\nGráficos guardados en: {OUTPUT_DIR.resolve()}")
    print(f"Datos guardados en:    {datos_path.resolve()}")
    print("=" * 60)

if __name__ == "__main__":
    main()
