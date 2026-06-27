import re
import os
import sys

#  Buscar el archivo de log 
ruta_log = sys.argv[1] if len(sys.argv) > 1 else "log_disparos.txt"
if not os.path.exists(ruta_log):
    script_dir = os.path.dirname(os.path.abspath(__file__))
    ruta_log = os.path.join(script_dir, "log_disparos.txt")

if not os.path.exists(ruta_log):
    print(f"Error: no se encontro el log en '{ruta_log}'")
    sys.exit(1)

with open(ruta_log, "r") as archivo:
    contenido = archivo.read()

# Quedarse solo con las transiciones (sin timestamps) y unirlas: T0T1T6T2...
test_str = "".join(re.findall(r"T\d+", contenido))

# Reconoce un invariante completo. Los (.*?) absorben el interleaving.
regex = r"(T0)(.*?)((T1)(.*?)(T2)(.*?)(T3)|(T4)(.*?)(T5)|(T6)(.*?)(T7)(.*?)(T8))(.*?)(T9)"

# Conserva solo los gaps (interleaving) y borra las transiciones del invariante
reemplazo = r"\g<2>\g<5>\g<7>\g<10>\g<13>\g<15>\g<17>"

invariantes_transicion = {
    "Tarjetas": 0,   # T0T1T2T3T9
    "AltoRiesgo":   0,   # T0T4T5T9
    "Transferencias": 0,   # T0T6T7T8T9
}

# Cuenta cada invariante segun la rama que matcheo
def reconocer_invariante(texto_a_iterar, iteraciones):
    for _ in range(iteraciones):
        match = re.search(regex, texto_a_iterar)
        if not match:
            break
        if match.group(9) is not None:
            invariantes_transicion["AltoRiesgo"] += 1
        elif match.group(12) is not None:
            invariantes_transicion["Transferencias"] += 1
        else:
            invariantes_transicion["Tarjetas"] += 1
        start, end = match.span()
        texto_a_iterar = texto_a_iterar[:start] + texto_a_iterar[end:]

# Reemplazar invariantes hasta que no quede ninguno
result = re.subn(regex, reemplazo, test_str)
coincidencias = result[1]
reconocer_invariante(test_str, result[1])

while result[1] > 0:
    aux = result[0]
    result = re.subn(regex, reemplazo, result[0])
    coincidencias += result[1]
    reconocer_invariante(aux, result[1])


print("=" * 55)
print("  VERIFICACION DE INVARIANTES DE TRANSICION (consumo)")
print("=" * 55)
print(f"  Log: {os.path.abspath(ruta_log)}")
print(f"  Transiciones totales: {len(test_str) // 2}")
print()
if len(result[0]) == 0:
    print("  VALIDACION DE INVARIANTES EXITOSA")
else:
    print("  VALIDACION DE INVARIANTES FALLIDA")
    print(f"  Transiciones restantes: {result[0]}")
print()
print(f"  Coincidencias (invariantes completados): {coincidencias}")
print(f"    Tarjetas       (T0T1T2T3T9): {invariantes_transicion['Tarjetas']}")
print(f"    Alto Riesgo    (T0T4T5T9):   {invariantes_transicion['AltoRiesgo']}")
print(f"    Transferencias (T0T6T7T8T9): {invariantes_transicion['Transferencias']}")
print("=" * 55)

sys.exit(0 if len(result[0]) == 0 else 1)
