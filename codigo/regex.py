import re
import sys
import os

def leer_log(ruta_archivo):
    transiciones = []
    with open(ruta_archivo, "r") as archivo:
        for linea in archivo:
            linea = linea.strip()
            if not linea:
                continue
            coincidencia = re.match(r"^(T\d+)", linea)
            if coincidencia:
                transiciones.append(coincidencia.group(1))
    return transiciones

def filtrar_por_flujo(transiciones):
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

def verificar_invariantes(datos_filtrados):
    resultados = {}

    # --- Flujo Tarjetas ---
    patron_tarjetas = r"^(T1T2T3)+$"
    seq = datos_filtrados["tarjetas"]
    # Se remueven secuencias incompletas al final debido al apagado abrupto (ej. T1 o T1T2)
    seq_limpia = re.sub(r"(T1T2|T1)$", "", seq)
    if len(seq_limpia) == 0:
        resultados["tarjetas"] = {"valido": True, "ciclos": 0, "mensaje": "Sin transacciones de tarjeta o incompletas al final"}
    else:
        match = re.match(patron_tarjetas, seq_limpia)
        if match:
            _, cantidad = re.subn(r"T1T2T3", "", seq_limpia)
            resultados["tarjetas"] = {"valido": True, "ciclos": cantidad, "mensaje": "OK"}
        else:
            error = re.search(r"(T[123])(T[123])", seq)
            detalle = f"Secuencia rota cerca de: ...{error.group()}..." if error else "Patron no coincide"
            resultados["tarjetas"] = {"valido": False, "ciclos": 0, "mensaje": detalle}

    # --- Flujo Alto Riesgo ---
    patron_alto_riesgo = r"^(T4T5)+$"
    seq = datos_filtrados["alto_riesgo"]
    seq_limpia = re.sub(r"(T4)$", "", seq)
    if len(seq_limpia) == 0:
        resultados["alto_riesgo"] = {"valido": True, "ciclos": 0, "mensaje": "Sin transacciones de alto riesgo o incompletas al final"}
    else:
        match = re.match(patron_alto_riesgo, seq_limpia)
        if match:
            _, cantidad = re.subn(r"T4T5", "", seq_limpia)
            resultados["alto_riesgo"] = {"valido": True, "ciclos": cantidad, "mensaje": "OK"}
        else:
            error = re.search(r"(T[45])(T[45])", seq)
            detalle = f"Secuencia rota cerca de: ...{error.group()}..." if error else "Patron no coincide"
            resultados["alto_riesgo"] = {"valido": False, "ciclos": 0, "mensaje": detalle}

    # --- Flujo Transferencias ---
    patron_transferencias = r"^(T6T7T8)+$"
    seq = datos_filtrados["transferencias"]
    seq_limpia = re.sub(r"(T6T7|T6)$", "", seq)
    if len(seq_limpia) == 0:
        resultados["transferencias"] = {"valido": True, "ciclos": 0, "mensaje": "Sin transacciones de transferencia o incompletas al final"}
    else:
        match = re.match(patron_transferencias, seq_limpia)
        if match:
            _, cantidad = re.subn(r"T6T7T8", "", seq_limpia)
            resultados["transferencias"] = {"valido": True, "ciclos": cantidad, "mensaje": "OK"}
        else:
            error = re.search(r"(T[678])(T[678])", seq)
            detalle = f"Secuencia rota cerca de: ...{error.group()}..." if error else "Patron no coincide"
            resultados["transferencias"] = {"valido": False, "ciclos": 0, "mensaje": detalle}

    return resultados

def verificar_contadores(datos_filtrados, resultados_flujos):
    t0 = datos_filtrados["conteo_t0"]
    t9 = datos_filtrados["conteo_t9"]

    ciclos_tarjetas = resultados_flujos["tarjetas"]["ciclos"]
    ciclos_alto_riesgo = resultados_flujos["alto_riesgo"]["ciclos"]
    ciclos_transferencias = resultados_flujos["transferencias"]["ciclos"]
    total_ciclos = ciclos_tarjetas + ciclos_alto_riesgo + ciclos_transferencias

    # Para el conteo de T0, como sabemos que hay hasta 3 transacciones en vuelo,
    # t0 debe ser igual a t9 + transacciones en vuelo al apagar.
    # Por lo tanto, t0 >= t9 y t0 <= t9 + 3.
    t0_consistente = (t0 >= t9) and (t0 <= t9 + 3)

    return {
        "t0_igual_t9": t0_consistente, # Tolerante a transacciones en vuelo
        "suma_ciclos_igual_t9": total_ciclos == t9,
        "conteo_t0": t0,
        "conteo_t9": t9,
        "total_ciclos": total_ciclos,
        "ciclos_tarjetas": ciclos_tarjetas,
        "ciclos_alto_riesgo": ciclos_alto_riesgo,
        "ciclos_transferencias": ciclos_transferencias,
    }

def imprimir_reporte(resultados_flujos, resultados_contadores):
    print()
    print("=" * 60)
    print("  VERIFICACION DE INVARIANTES DE TRANSICION (Regex)")
    print("=" * 60)

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

    c = resultados_contadores
    print("--- Validacion de Contadores Globales ---")
    print()
    print(f"  Disparos de T0 (admision):    {c['conteo_t0']}")
    print(f"  Disparos de T9 (liquidacion): {c['conteo_t9']}")
    print(f"  T0 consistente (con in-flight): {'PASS' if c['t0_igual_t9'] else 'FAIL'}")
    print()
    print(f"  Ciclos Tarjetas:              {c['ciclos_tarjetas']}")
    print(f"  Ciclos Alto Riesgo:           {c['ciclos_alto_riesgo']}")
    print(f"  Ciclos Transferencias:        {c['ciclos_transferencias']}")
    print(f"  Total ciclos:                 {c['total_ciclos']}")
    print(f"  Suma ciclos == T9:            {'PASS' if c['suma_ciclos_igual_t9'] else 'FAIL'}")

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

def main():
    if len(sys.argv) > 1:
        ruta_log = sys.argv[1]
    else:
        ruta_log = "log_disparos.txt"
        if not os.path.exists(ruta_log):
            script_dir = os.path.dirname(os.path.abspath(__file__))
            ruta_log = os.path.join(script_dir, "log_disparos.txt")

    if not os.path.exists(ruta_log):
        print(f"Error: No se encontro el archivo de log en '{ruta_log}'")
        sys.exit(1)

    print(f"Leyendo log de: {ruta_log}")
    transiciones = leer_log(ruta_log)
    print(f"Total de transiciones registradas: {len(transiciones)}")
    datos = filtrar_por_flujo(transiciones)
    resultados_flujos = verificar_invariantes(datos)
    resultados_contadores = verificar_contadores(datos, resultados_flujos)
    exito = imprimir_reporte(resultados_flujos, resultados_contadores)
    sys.exit(0 if exito else 1)

if __name__ == "__main__":
    main()
