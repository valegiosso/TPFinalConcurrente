"""
Verificador de Invariantes de Transición mediante Expresiones Regulares.

Lee el archivo de log generado por el sistema de procesamiento de transacciones
de pago (Red de Petri) y valida que cada flujo de procesamiento haya respetado
el orden secuencial definido por los T-Invariantes de la red.

T-Invariantes de la red:
  T_inv1 (Flujo Tarjetas):        {T0, T1, T2, T3, T9}
  T_inv2 (Flujo Alto Riesgo):     {T0, T4, T5, T9}
  T_inv3 (Flujo Transferencias):  {T0, T6, T7, T8, T9}

Uso:
  python verificar_invariantes.py                          (usa log_disparos.txt por defecto)
  python verificar_invariantes.py ruta/al/archivo.txt      (ruta personalizada)
"""

import re
import sys
import os


# =============================================================================
# 1. LECTURA Y PARSEO DEL LOG
# =============================================================================

def leer_log(ruta_archivo):
    """
    Lee el archivo de log y extrae la secuencia de transiciones disparadas.
    Cada linea tiene formato: 'T<n> <timestamp>'
    Retorna una lista de strings con las transiciones, ej: ['T0', 'T1', ...]
    """
    transiciones = []

    with open(ruta_archivo, "r") as archivo:
        for linea in archivo:
            linea = linea.strip()
            if not linea:
                continue
            # re.match busca el patron solo al INICIO de la linea
            # Captura el identificador de transicion (T seguido de digitos)
            coincidencia = re.match(r"^(T\d+)", linea)
            if coincidencia:
                transiciones.append(coincidencia.group(1))

    return transiciones


# =============================================================================
# 2. FILTRADO DE SECUENCIAS POR FLUJO
# =============================================================================

def filtrar_por_flujo(transiciones):
    """
    Separa la secuencia global entrelazada en tres sub-secuencias independientes,
    una por cada flujo de procesamiento de la red de Petri.

    Dado que los hilos ejecutan en paralelo, el log contiene transiciones
    intercaladas de los tres flujos. Al filtrar por tipo, eliminamos el
    entrelazamiento y podemos verificar el orden interno de cada flujo.
    """
    flujo_tarjetas = []        # T1, T2, T3
    flujo_alto_riesgo = []     # T4, T5
    flujo_transferencias = []  # T6, T7, T8

    conteo_t0 = 0
    conteo_t9 = 0

    for t in transiciones:
        if t == "T0":
            conteo_t0 += 1
        elif t == "T9":
            conteo_t9 += 1
        elif t in ("T1", "T2", "T3"):
            flujo_tarjetas.append(t)
        elif t in ("T4", "T5"):
            flujo_alto_riesgo.append(t)
        elif t in ("T6", "T7", "T8"):
            flujo_transferencias.append(t)

    return {
        "tarjetas": "".join(flujo_tarjetas),
        "alto_riesgo": "".join(flujo_alto_riesgo),
        "transferencias": "".join(flujo_transferencias),
        "conteo_t0": conteo_t0,
        "conteo_t9": conteo_t9,
    }


# =============================================================================
# 3. VERIFICACION CON EXPRESIONES REGULARES
# =============================================================================

def verificar_invariantes(datos_filtrados):
    """
    Aplica expresiones regulares sobre cada flujo filtrado para validar
    que el orden de las transiciones sea correcto.

    Patrones esperados:
      Tarjetas:       (T1T2T3)+ -> cada ciclo es T1 seguido de T2 seguido de T3
      Alto Riesgo:    (T4T5)+   -> cada ciclo es T4 seguido de T5
      Transferencias: (T6T7T8)+ -> cada ciclo es T6 seguido de T7 seguido de T8
    """
    resultados = {}

    # --- Flujo Tarjetas ---
    patron_tarjetas = r"^(T1T2T3)+$"
    seq = datos_filtrados["tarjetas"]
    if len(seq) == 0:
        resultados["tarjetas"] = {"valido": True, "ciclos": 0, "mensaje": "Sin transacciones de tarjeta"}
    else:
        # re.match evalua el patron desde el inicio del string
        match = re.match(patron_tarjetas, seq)
        if match:
            # re.subn reemplaza cada ocurrencia del ciclo y cuenta cuantas hubo
            _, cantidad = re.subn(r"T1T2T3", "", seq)
            resultados["tarjetas"] = {"valido": True, "ciclos": cantidad, "mensaje": "OK"}
        else:
            # re.search busca la primera ruptura del patron para diagnostico
            error = re.search(r"(T[123])(T[123])", seq)
            detalle = f"Secuencia rota cerca de: ...{error.group()}..." if error else "Patron no coincide"
            resultados["tarjetas"] = {"valido": False, "ciclos": 0, "mensaje": detalle}

    # --- Flujo Alto Riesgo ---
    patron_alto_riesgo = r"^(T4T5)+$"
    seq = datos_filtrados["alto_riesgo"]
    if len(seq) == 0:
        resultados["alto_riesgo"] = {"valido": True, "ciclos": 0, "mensaje": "Sin transacciones de alto riesgo"}
    else:
        match = re.match(patron_alto_riesgo, seq)
        if match:
            _, cantidad = re.subn(r"T4T5", "", seq)
            resultados["alto_riesgo"] = {"valido": True, "ciclos": cantidad, "mensaje": "OK"}
        else:
            error = re.search(r"(T[45])(T[45])", seq)
            detalle = f"Secuencia rota cerca de: ...{error.group()}..." if error else "Patron no coincide"
            resultados["alto_riesgo"] = {"valido": False, "ciclos": 0, "mensaje": detalle}

    # --- Flujo Transferencias ---
    patron_transferencias = r"^(T6T7T8)+$"
    seq = datos_filtrados["transferencias"]
    if len(seq) == 0:
        resultados["transferencias"] = {"valido": True, "ciclos": 0, "mensaje": "Sin transacciones de transferencia"}
    else:
        match = re.match(patron_transferencias, seq)
        if match:
            _, cantidad = re.subn(r"T6T7T8", "", seq)
            resultados["transferencias"] = {"valido": True, "ciclos": cantidad, "mensaje": "OK"}
        else:
            error = re.search(r"(T[678])(T[678])", seq)
            detalle = f"Secuencia rota cerca de: ...{error.group()}..." if error else "Patron no coincide"
            resultados["transferencias"] = {"valido": False, "ciclos": 0, "mensaje": detalle}

    return resultados


# =============================================================================
# 4. VERIFICACION DE CONTADORES GLOBALES
# =============================================================================

def verificar_contadores(datos_filtrados, resultados_flujos):
    """
    Verifica la consistencia global del sistema:
    - T0 (admision) debe ser igual a T9 (liquidacion)
    - La suma de ciclos de los tres flujos debe ser igual a T9
    """
    t0 = datos_filtrados["conteo_t0"]
    t9 = datos_filtrados["conteo_t9"]

    ciclos_tarjetas = resultados_flujos["tarjetas"]["ciclos"]
    ciclos_alto_riesgo = resultados_flujos["alto_riesgo"]["ciclos"]
    ciclos_transferencias = resultados_flujos["transferencias"]["ciclos"]
    total_ciclos = ciclos_tarjetas + ciclos_alto_riesgo + ciclos_transferencias

    return {
        "t0_igual_t9": t0 == t9,
        "suma_ciclos_igual_t9": total_ciclos == t9,
        "conteo_t0": t0,
        "conteo_t9": t9,
        "total_ciclos": total_ciclos,
        "ciclos_tarjetas": ciclos_tarjetas,
        "ciclos_alto_riesgo": ciclos_alto_riesgo,
        "ciclos_transferencias": ciclos_transferencias,
    }


# =============================================================================
# 5. REPORTE DE RESULTADOS
# =============================================================================

def imprimir_reporte(resultados_flujos, resultados_contadores):
    """
    Imprime un reporte completo de la verificacion de invariantes.
    """
    print()
    print("=" * 60)
    print("  VERIFICACION DE INVARIANTES DE TRANSICION (Regex)")
    print("=" * 60)

    # Resultados por flujo
    print()
    print("--- Validacion por Flujo (Expresiones Regulares) ---")
    print()

    flujos = [
        ("Tarjetas (T1->T2->T3)",       "tarjetas",        r"^(T1T2T3)+$"),
        ("Alto Riesgo (T4->T5)",         "alto_riesgo",     r"^(T4T5)+$"),
        ("Transferencias (T6->T7->T8)",  "transferencias",  r"^(T6T7T8)+$"),
    ]

    for nombre, clave, patron in flujos:
        r = resultados_flujos[clave]
        estado = "PASS" if r["valido"] else "FAIL"
        print(f"  [{estado}] {nombre}")
        print(f"         Patron: {patron}")
        print(f"         Ciclos completados: {r['ciclos']}")
        print(f"         Estado: {r['mensaje']}")
        print()

    # Resultados globales
    c = resultados_contadores
    print("--- Validacion de Contadores Globales ---")
    print()
    print(f"  Disparos de T0 (admision):    {c['conteo_t0']}")
    print(f"  Disparos de T9 (liquidacion): {c['conteo_t9']}")
    print(f"  T0 == T9:                     {'PASS' if c['t0_igual_t9'] else 'FAIL'}")
    print()
    print(f"  Ciclos Tarjetas:              {c['ciclos_tarjetas']}")
    print(f"  Ciclos Alto Riesgo:           {c['ciclos_alto_riesgo']}")
    print(f"  Ciclos Transferencias:        {c['ciclos_transferencias']}")
    print(f"  Total ciclos:                 {c['total_ciclos']}")
    print(f"  Suma ciclos == T9:            {'PASS' if c['suma_ciclos_igual_t9'] else 'FAIL'}")

    # Veredicto final
    print()
    print("=" * 60)
    todo_ok = (
        all(r["valido"] for r in resultados_flujos.values())
        and c["t0_igual_t9"]
        and c["suma_ciclos_igual_t9"]
    )
    if todo_ok:
        print("  RESULTADO FINAL: TODOS LOS INVARIANTES VERIFICADOS")
    else:
        print("  RESULTADO FINAL: FALLO EN LA VERIFICACION")
    print("=" * 60)
    print()

    return todo_ok


# =============================================================================
# 6. MAIN
# =============================================================================

def main():
    # Determinar ruta del archivo de log
    if len(sys.argv) > 1:
        ruta_log = sys.argv[1]
    else:
        # Buscar en el directorio actual o en el directorio del script
        ruta_log = "log_disparos.txt"
        if not os.path.exists(ruta_log):
            script_dir = os.path.dirname(os.path.abspath(__file__))
            ruta_log = os.path.join(script_dir, "log_disparos.txt")

    if not os.path.exists(ruta_log):
        print(f"Error: No se encontro el archivo de log en '{ruta_log}'")
        sys.exit(1)

    print(f"Leyendo log de: {ruta_log}")

    # Paso 1: Leer y parsear el log
    transiciones = leer_log(ruta_log)
    print(f"Total de transiciones registradas: {len(transiciones)}")

    # Paso 2: Filtrar por flujo
    datos = filtrar_por_flujo(transiciones)

    # Paso 3: Verificar invariantes con regex
    resultados_flujos = verificar_invariantes(datos)

    # Paso 4: Verificar contadores globales
    resultados_contadores = verificar_contadores(datos, resultados_flujos)

    # Paso 5: Imprimir reporte
    exito = imprimir_reporte(resultados_flujos, resultados_contadores)

    sys.exit(0 if exito else 1)


if __name__ == "__main__":
    main()
