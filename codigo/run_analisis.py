"""
run_analisis.py  - Script simplificado de analisis temporal.

Estrategia:
  - Usa los datos ya recolectados de las corridas anteriores
    (Minimos y Base-priorizada).
  - Ejecuta las configs pendientes: Lentos y Muy lentos (priorizada).
  - Ejecuta Base con politica aleatoria (requiere editar Main.java
    manualmente o usar una variante separada).
  - Genera todos los graficos y el reporte final.

Uso:
    py -X utf8 run_analisis.py
(Desde la carpeta codigo/)
"""

import subprocess, re, os, sys, json, time, statistics, math
from pathlib import Path
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np

# ---------------------------------------------------------------------------
# RUTAS
# ---------------------------------------------------------------------------
PROJECT_ROOT = Path(__file__).resolve().parent.parent
JAVA_SRC  = PROJECT_ROOT / "codigo" / "src"
JAVA_OUT  = PROJECT_ROOT / "codigo" / "out"
MAIN_CLASS = "monitor.Main"
OUTPUT_DIR = PROJECT_ROOT / "docs" / "graficos"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# ---------------------------------------------------------------------------
# DATOS YA RECOLECTADOS (corridas anteriores exitosas)
# ---------------------------------------------------------------------------
DATOS_PREVIOS = {
    "Minimos (50ms)": {
        "tiempos": [12400, 12488, 12536, 12552, 12432],
        "config": (50, 50, 75, 60, 60),
    },
    "Base (100/120/150) Priorizada": {
        "tiempos": [23722, 23301, 23455, 23407, 23534],
        "config": (100, 100, 150, 120, 120),
    },
}

# ---------------------------------------------------------------------------
# CONFIGS PENDIENTES (priorizada)
# ---------------------------------------------------------------------------
CONFIGS_PENDIENTES = [
    ("Lentos (200ms)",     200, 200, 250, 220, 220),
    ("Muy lentos (400ms)", 400, 400, 450, 420, 420),
]

RUNS = 2

# ---------------------------------------------------------------------------
# COTAS TEORICAS
# ---------------------------------------------------------------------------
def calcular_cotas(t2, t3, t5, t7, t8, n=200):
    t_tar  = t2 + t3
    t_ar   = t5
    t_tr   = t7 + t8
    t_max  = max(t_tar, t_ar, t_tr)
    t_min  = min(t_tar, t_ar, t_tr)
    rondas = math.ceil(n / 3)
    cota_min_ms = rondas * t_max
    cota_max_ms = n * t_max
    return cota_min_ms, cota_max_ms, {
        "t_tarjetas": t_tar, "t_alto_riesgo": t_ar,
        "t_transferencias": t_tr, "t_max": t_max,
    }

# ---------------------------------------------------------------------------
# PARCHEAR TIEMPOS EN Main.java y recompilar
# ---------------------------------------------------------------------------
def parchear_tiempos(t2, t3, t5, t7, t8):
    main_path = JAVA_SRC / "monitor" / "Main.java"
    txt = main_path.read_text(encoding="utf-8")
    def repl(txt, idx, alfa):
        return re.sub(
            rf'(tiempos\[{idx}\]\s*=\s*new SensibilizadoConTiempo\()(\d+)(,\s*Long\.MAX_VALUE\))',
            rf'\g<1>{alfa}\g<3>', txt)
    for idx, alfa in [(2, t2), (3, t3), (5, t5), (7, t7), (8, t8)]:
        txt = repl(txt, idx, alfa)
    main_path.write_text(txt, encoding="utf-8")
    # recompilar
    sources = list((JAVA_SRC / "monitor").glob("*.java"))
    JAVA_OUT.mkdir(parents=True, exist_ok=True)
    r = subprocess.run(["javac", "-d", str(JAVA_OUT)] + [str(s) for s in sources],
                       capture_output=True, text=True)
    if r.returncode != 0:
        print("ERROR compilacion:", r.stderr[:300])
        return False
    return True

def ejecutar_java():
    cmd = ["java", "-cp", str(JAVA_OUT), MAIN_CLASS]
    r = subprocess.run(cmd, capture_output=True, text=True,
                       cwd=str(JAVA_SRC), timeout=180)
    m = re.search(r"Tiempo de ejecucion:\s*(\d+)\s*ms", r.stdout)
    if m:
        return int(m.group(1))
    if r.returncode == 0:
        return None
    print("  FALLO:", r.stderr[:200])
    return None

# ---------------------------------------------------------------------------
# COLECTAR PENDIENTES
# ---------------------------------------------------------------------------
def colectar_pendientes():
    resultados = dict(DATOS_PREVIOS)
    for (etiqueta, t2, t3, t5, t7, t8) in CONFIGS_PENDIENTES:
        print(f"\n--- {etiqueta} ---")
        print(f"  T2={t2} T3={t3} T5={t5} T7={t7} T8={t8}")
        ok = parchear_tiempos(t2, t3, t5, t7, t8)
        if not ok:
            print("  [SKIP] fallo compilacion")
            continue
        tiempos = []
        for i in range(RUNS):
            t = ejecutar_java()
            if t:
                tiempos.append(t)
                print(f"    Run {i+1}: {t} ms")
            else:
                print(f"    Run {i+1}: FALLO")
        resultados[etiqueta] = {"tiempos": tiempos, "config": (t2, t3, t5, t7, t8)}
    # restaurar config base
    parchear_tiempos(100, 100, 150, 120, 120)
    return resultados

# ---------------------------------------------------------------------------
# COLECTAR BASE CON ALEATORIA (editando Main.java manualmente aqui)
# ---------------------------------------------------------------------------
def colectar_aleatoria():
    print("\n--- Base (100/120/150) - Politica ALEATORIA ---")
    main_path = JAVA_SRC / "monitor" / "Main.java"
    original = main_path.read_text(encoding="utf-8")
    # cambiar politica
    modificado = original.replace(
        "//Politica politica = new PoliticaAleatoria();",
        "Politica politica = new PoliticaAleatoria();")
    modificado = modificado.replace(
        "Politica politica = new PoliticaPriorizada(new int[]{4, 5}); // prioriza alto riesgo",
        "//Politica politica = new PoliticaPriorizada(new int[]{4, 5}); // prioriza alto riesgo")
    main_path.write_text(modificado, encoding="utf-8")
    # compilar
    sources = list((JAVA_SRC / "monitor").glob("*.java"))
    r = subprocess.run(["javac", "-d", str(JAVA_OUT)] + [str(s) for s in sources],
                       capture_output=True, text=True)
    if r.returncode != 0:
        print("  ERROR compilacion:", r.stderr[:200])
        main_path.write_text(original, encoding="utf-8")
        return []
    tiempos = []
    for i in range(RUNS):
        t = ejecutar_java()
        if t:
            tiempos.append(t)
            print(f"  Run {i+1}: {t} ms")
    # Guardar log de aleatoria
    log_src = JAVA_SRC / "log_disparos.txt"
    log_dest = JAVA_SRC / "log_disparos_alea.txt"
    if log_src.exists():
        import shutil
        shutil.copy(str(log_src), str(log_dest))
    # restaurar
    main_path.write_text(original, encoding="utf-8")
    parchear_tiempos(100, 100, 150, 120, 120)
    return tiempos

# ---------------------------------------------------------------------------
# GRAFICOS
# ---------------------------------------------------------------------------
COLORES = {
    "Minimos (50ms)":                  "#2ecc71",
    "Base (100/120/150) Priorizada":   "#3498db",
    "Lentos (200ms)":                  "#e67e22",
    "Muy lentos (400ms)":              "#e74c3c",
}

def grafico_boxplot(resultados):
    fig, ax = plt.subplots(figsize=(13, 6))
    labels = [k for k, v in resultados.items() if v["tiempos"] and "Aleatoria" not in k]
    data   = [resultados[k]["tiempos"] for k in labels]
    colors = [COLORES.get(k, "#95a5a6") for k in labels]
    bp = ax.boxplot(data, patch_artist=True,
                    medianprops=dict(color="white", linewidth=2.5))
    for patch, color in zip(bp['boxes'], colors):
        patch.set_facecolor(color); patch.set_alpha(0.82)
    ax.axhline(20000, color='#27ae60', ls='--', lw=2,
               label='Limite inferior enunciado (20 s)')
    ax.axhline(40000, color='#c0392b', ls='--', lw=2,
               label='Limite superior enunciado (40 s)')
    ax.fill_between([0, len(labels)+1], 20000, 40000,
                    alpha=0.07, color='#27ae60', zorder=0)
    ax.set_xticks(range(1, len(labels)+1))
    ax.set_xticklabels([l.replace(" Priorizada","") for l in labels],
                       rotation=12, ha='right', fontsize=10)
    ax.set_ylabel("Tiempo de ejecucion (ms)", fontsize=11)
    ax.set_title("Distribucion de Tiempos por Configuracion de Transiciones\n"
                 "(200 invariantes, politica priorizada)", fontsize=12)
    ax.legend(fontsize=9); ax.grid(axis='y', alpha=0.3)
    plt.tight_layout()
    p = OUTPUT_DIR / "boxplot_configs.png"
    plt.savefig(p, dpi=150); plt.close()
    print(f"  [OK] {p.name}")
    return p

def grafico_cotas_vs_real(resultados):
    labels = [k for k, v in resultados.items() if v["tiempos"] and "Aleatoria" not in k]
    c_min, c_max, c_real = [], [], []
    for k in labels:
        t2,t3,t5,t7,t8 = resultados[k]["config"]
        mn, mx, _ = calcular_cotas(t2,t3,t5,t7,t8)
        c_min.append(mn/1000); c_max.append(mx/1000)
        c_real.append(statistics.mean(resultados[k]["tiempos"])/1000)

    x = np.arange(len(labels)); w = 0.25
    fig, ax = plt.subplots(figsize=(14, 6))
    b1 = ax.bar(x-w, c_min,  w, label='Cota Min teorica (mejor caso)',
                color='#2ecc71', alpha=0.8)
    b2 = ax.bar(x,   c_real, w, label='Media real (ejecuciones)',
                color='#3498db', alpha=0.9)
    b3 = ax.bar(x+w, c_max,  w, label='Cota Max teorica (peor caso)',
                color='#e74c3c', alpha=0.8)
    ax.axhline(20, color='#27ae60', ls=':', lw=1.8,
               label='Req. enunciado 20 s')
    ax.axhline(40, color='#c0392b', ls=':', lw=1.8,
               label='Req. enunciado 40 s')
    ax.set_xticks(x)
    ax.set_xticklabels([l.replace(" Priorizada","") for l in labels],
                       rotation=12, ha='right', fontsize=9)
    ax.set_ylabel("Segundos"); ax.set_title(
        "Cotas Teoricas vs Tiempo Real de Ejecucion\n(200 invariantes)")
    ax.legend(loc='upper left', fontsize=9); ax.grid(axis='y', alpha=0.3)
    for bar in b2:
        h = bar.get_height()
        ax.annotate(f'{h:.1f}s', (bar.get_x()+bar.get_width()/2, h),
                    xytext=(0,3), textcoords='offset points',
                    ha='center', va='bottom', fontsize=8, fontweight='bold')
    plt.tight_layout()
    p = OUTPUT_DIR / "cotas_vs_real.png"
    plt.savefig(p, dpi=150); plt.close()
    print(f"  [OK] {p.name}")
    return p

def grafico_variacion(resultados):
    # Eje X: alfa del flujo mas lento (transferencias T7+T8)
    labels_ord = ["Minimos (50ms)", "Base (100/120/150) Priorizada",
                  "Lentos (200ms)", "Muy lentos (400ms)"]
    labels = [l for l in labels_ord if l in resultados and resultados[l]["tiempos"]]
    alfas  = [resultados[l]["config"][3]+resultados[l]["config"][4]
              for l in labels]  # T7+T8
    medias = [statistics.mean(resultados[l]["tiempos"])/1000 for l in labels]
    stds   = [statistics.stdev(resultados[l]["tiempos"])/1000
               if len(resultados[l]["tiempos"])>1 else 0 for l in labels]
    c_mins, c_maxs = [], []
    for l in labels:
        t2,t3,t5,t7,t8 = resultados[l]["config"]
        mn,mx,_ = calcular_cotas(t2,t3,t5,t7,t8)
        c_mins.append(mn/1000); c_maxs.append(mx/1000)

    fig, ax = plt.subplots(figsize=(10, 6))
    ax.plot(alfas, medias, 'o-', color='#3498db', lw=2.5,
            ms=9, label='Tiempo real (media)', zorder=3)
    ax.fill_between(alfas,
                    [m-s for m,s in zip(medias,stds)],
                    [m+s for m,s in zip(medias,stds)],
                    alpha=0.18, color='#3498db', label='+/-1 desv. std')
    ax.plot(alfas, c_mins, 's--', color='#27ae60', lw=1.8,
            ms=7, label='Cota Min teorica')
    ax.plot(alfas, c_maxs, 'v--', color='#c0392b', lw=1.8,
            ms=7, label='Cota Max teorica')
    x_pad = (max(alfas)-min(alfas))*0.05
    ax.fill_between([min(alfas)-x_pad, max(alfas)+x_pad], 20, 40,
                    alpha=0.07, color='#27ae60', zorder=0,
                    label='Rango aceptable (20-40 s)')
    ax.axhline(20, color='#27ae60', ls=':', alpha=0.5)
    ax.axhline(40, color='#c0392b', ls=':', alpha=0.5)
    ax.set_xlabel("Suma alpha flujo Transferencias - T7+T8 (ms)", fontsize=11)
    ax.set_ylabel("Tiempo total de ejecucion (s)", fontsize=11)
    ax.set_title("Impacto de Variar los Tiempos de Transicion\nen el Tiempo Total (200 invariantes)", fontsize=12)
    ax.legend(fontsize=9); ax.grid(alpha=0.3)
    # anotar puntos
    for x_, y_, l in zip(alfas, medias, labels):
        etiq = l.split("(")[1].rstrip(")")
        ax.annotate(etiq, (x_, y_), xytext=(6, 4),
                    textcoords='offset points', fontsize=8, color='#2c3e50')
    plt.tight_layout()
    p = OUTPUT_DIR / "variacion_tiempos.png"
    plt.savefig(p, dpi=150); plt.close()
    print(f"  [OK] {p.name}")
    return p

def parsear_log(log_path):
    if not log_path.exists():
        return None
    with open(log_path, encoding="utf-8") as f:
        trans = []
        for line in f:
            m = re.match(r"^(T\d+)", line)
            if m:
                trans.append(m.group(1))
    tar = sum(1 for t in trans if t == "T3")
    ar  = sum(1 for t in trans if t == "T5")
    tr  = sum(1 for t in trans if t == "T8")
    return tar, ar, tr

def grafico_distribucion_invariantes():
    log_prior = JAVA_SRC / "log_disparos_prior.txt"
    log_alea = JAVA_SRC / "log_disparos_alea.txt"
    
    # Fallback to general log if specific ones aren't found
    if not log_prior.exists() and (JAVA_SRC / "log_disparos.txt").exists():
        import shutil
        shutil.copy(str(JAVA_SRC / "log_disparos.txt"), str(log_prior))
        
    fig, axes = plt.subplots(1, 2, figsize=(14, 7))
    colores = ['#3498db', '#e74c3c', '#2ecc71']
    
    for i, (log_path, title) in enumerate([
            (log_prior, 'Política Priorizada\n(Alto Riesgo Priorizado)'),
            (log_alea, 'Política Aleatoria\n(Distribución Equitativa)')]):
        
        datos_ciclos = parsear_log(log_path)
        if not datos_ciclos:
            axes[i].text(0.5, 0.5, "Log no disponible\n(Ejecutar simulación)",
                         ha='center', va='center', fontsize=12)
            axes[i].set_title(title, fontsize=12, pad=15)
            continue
            
        tar, ar, tr = datos_ciclos
        total = tar + ar + tr or 1
        sizes = [tar, ar, tr]
        labels = [
            f'Tarjetas\n{tar} ({tar/total*100:.1f}%)',
            f'Alto Riesgo\n{ar} ({ar/total*100:.1f}%)',
            f'Transferencias\n{tr} ({tr/total*100:.1f}%)'
        ]
        
        # Gráfico de anillo (pie con width en wedgeprops)
        wedges, texts = axes[i].pie(
            sizes, colors=colores, startangle=90,
            wedgeprops=dict(width=0.42, edgecolor='white', lw=2.5),
            labels=labels, textprops=dict(fontsize=10, fontweight='bold')
        )
        
        # Añadir un círculo blanco central para acentuar el efecto anillo (donut)
        centre_circle = plt.Circle((0,0),0.70,fc='white')
        axes[i].add_artist(centre_circle)
        
        axes[i].set_title(title, fontsize=13, pad=20, fontweight='bold')
        
    plt.suptitle("Distribución de Invariantes por Flujo según Política de Conflicto\n(200 Invariantes totales)",
                 fontsize=14, fontweight='bold', y=0.98)
    plt.tight_layout()
    p = OUTPUT_DIR / "distribucion_invariantes.png"
    plt.savefig(p, dpi=150, bbox_inches='tight'); plt.close()
    print(f"  [OK] {p.name}")
    return p

def grafico_politicas(prior_tiempos, alea_tiempos):
    if not prior_tiempos or not alea_tiempos:
        print("  [SKIP] datos insuficientes para comparativa de politicas")
        return None
        
    fig, ax = plt.subplots(figsize=(8, 6))
    
    pols = ['Priorizada', 'Aleatoria']
    meds = [statistics.mean(prior_tiempos)/1000,
            statistics.mean(alea_tiempos)/1000]
    stds = [statistics.stdev(prior_tiempos)/1000 if len(prior_tiempos)>1 else 0,
            statistics.stdev(alea_tiempos)/1000  if len(alea_tiempos)>1 else 0]

    cols = ['#3498db', '#e67e22']
    bars = ax.bar(pols, meds, yerr=stds, color=cols, alpha=0.85, width=0.4, error_kw=dict(lw=2, capsize=9))

    # Dibujar Cotas Teoricas y Rango
    # Cota Minima
    ax.axhline(16.08, color='#2ecc71', ls='-.', lw=2,
               label='Cota Mínima Teórica (16.08 s)')
    # Cota Maxima
    ax.axhline(48.0, color='#e74c3c', ls='-.', lw=2,
               label='Cota Máxima Teórica (48.0 s)')
    # Rango del Enunciado (20-40s)
    ax.fill_between([-0.5, 1.5], 20, 40, color='green', alpha=0.06,
                     label='Rango Enunciado (20-40 s)')

    ax.set_xlim(-0.5, 1.5)
    ax.set_ylim(0, 52) # Rango de 0 a 52 para que quepan las cotas
    ax.set_ylabel("Segundos", fontsize=11)
    ax.set_title("Tiempos Medios de Ejecución vs Cotas Teóricas y Enunciado\n(Configuración Base, 200 invariantes)", fontsize=12, fontweight='bold', pad=15)
    ax.legend(loc='lower center', bbox_to_anchor=(0.5, -0.22), ncol=2, fontsize=9)
    ax.grid(axis='y', alpha=0.2)

    # Anotar los valores arriba de las barras
    for bar in bars:
        h = bar.get_height()
        ax.annotate(f'{h:.2f}s', (bar.get_x() + bar.get_width()/2, h),
                    xytext=(0, 4), textcoords='offset points',
                    ha='center', va='bottom', fontsize=10, fontweight='bold')

    plt.tight_layout()
    p = OUTPUT_DIR / "comparativa_politicas.png"
    plt.savefig(p, dpi=150, bbox_inches='tight'); plt.close()
    print(f"  [OK] {p.name}")
    return p

# ---------------------------------------------------------------------------
# REPORTE JSON Y CONSOLA
# ---------------------------------------------------------------------------
def generar_reporte(resultados, alea_tiempos):
    datos = {}
    for k, v in resultados.items():
        if not v["tiempos"]: continue
        t2,t3,t5,t7,t8 = v["config"]
        mn, mx, det = calcular_cotas(t2,t3,t5,t7,t8)
        ts = v["tiempos"]
        datos[k] = {
            "tiempos_ms": ts,
            "media_s":    round(statistics.mean(ts)/1000, 2),
            "min_s":      round(min(ts)/1000, 2),
            "max_s":      round(max(ts)/1000, 2),
            "std_s":      round(statistics.stdev(ts)/1000 if len(ts)>1 else 0, 2),
            "cota_min_s": round(mn/1000, 2),
            "cota_max_s": round(mx/1000, 2),
            "cumple_20_40s": all(20000<=t<=40000 for t in ts),
            "flujo_mas_lento_ms": det["t_max"],
        }
    p = OUTPUT_DIR / "resultados.json"
    p.write_text(json.dumps(datos, indent=2, ensure_ascii=False), encoding="utf-8")

    print(f"\n{'='*70}")
    print("  REPORTE FINAL - ANALISIS TEMPORAL")
    print(f"{'='*70}")
    print(f"{'Configuracion':<34} {'Media':>7} {'Min':>7} {'Max':>7} {'CotaMin':>8} {'CotaMax':>8} {'OK?':>5}")
    print("-"*72)
    for k, d in datos.items():
        ok = "[OK]" if d.get("cumple_20_40s") else "[--]"
        print(f"{k:<34} {d['media_s']:>6.1f}s {d['min_s']:>6.1f}s "
              f"{d['max_s']:>6.1f}s {d.get('cota_min_s','-'):>7}s "
              f"{d.get('cota_max_s','-'):>7}s {ok:>5}")
    print(f"{'='*70}")
    print(f"Graficos en: {OUTPUT_DIR}")
    print(f"Datos JSON:  {p}")
    return datos

# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------
def main():
    print("=" * 60)
    print("  ANALISIS TEMPORAL - PSP Petri Net Monitor")
    print("=" * 60)

    # 1. Colectar datos pendientes (Lentos y Muy lentos)
    resultados = colectar_pendientes()

    # 2. Colectar Base con politica aleatoria
    alea_tiempos = colectar_aleatoria()
    if alea_tiempos:
        resultados["Base (100/120/150) Aleatoria"] = {
            "tiempos": alea_tiempos,
            "config": (100, 100, 150, 120, 120),
        }

    # 2.5 Asegurar log de priorizada para grafico de anillo
    log_prior = JAVA_SRC / "log_disparos_prior.txt"
    if not log_prior.exists():
        print("\n--- Generando log de disparos para Politica Priorizada ---")
        parchear_tiempos(100, 100, 150, 120, 120)
        ejecutar_java()
        log_src = JAVA_SRC / "log_disparos.txt"
        if log_src.exists():
            import shutil
            shutil.copy(str(log_src), str(log_prior))

    # 3. Generar graficos
    print("\nGenerando graficos...")
    grafico_boxplot(resultados)
    grafico_cotas_vs_real(resultados)
    grafico_variacion(resultados)
    grafico_distribucion_invariantes()
    base_prior = resultados.get("Base (100/120/150) Priorizada", {}).get("tiempos", [])
    grafico_politicas(base_prior, alea_tiempos)

    # 4. Reporte
    generar_reporte(resultados, alea_tiempos)

if __name__ == "__main__":
    main()
