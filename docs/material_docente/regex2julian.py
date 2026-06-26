import re

# Log del logWriter
LOG_PATH = "../src/main/resources/logs/log.txt"

with open(LOG_PATH, 'r') as file:
    test_str = file.read()

regex = r'(T0)(.*?)(T1)(?!\d)(.*?)((T2)(.*?)(T3)(.*?)(T4)|(T5)(.*?)(T6)|(T7)(.*?)(T8)(.*?)(T9)(.*?)(T10))(.*?)(T11)(.*?)'
reemplazo = r'\g<2>\g<4>\g<7>\g<9>\g<12>\g<15>\g<17>\g<19>\g<21>\g<23>'

invariantes_transicion = {
    "T0T1T2T3T4T11": 0,
    "T0T1T5T6T11": 0,
    "T0T1T7T8T9T10T11": 0
}

# Detectamos los respectivos invariantes de transicion
def reconocer_invariante(texto_a_iterar, iteraciones):
    for i in range(iteraciones):
        # Buscamos la primera coincidencia en la cadena de transiciones
        match = re.search(regex, texto_a_iterar)
        # Si encontramos una coincidencia
        if match:
            match_str = match.group()
            # Sumamos una coincidencia al invariante correspondiente
            if "T7" in match_str and "T8" in match_str and "T9" in match_str and "T10" in match_str:
                invariantes_transicion["T0T1T7T8T9T10T11"] += 1
            elif "T5" in match_str and "T6" in match_str:
                invariantes_transicion["T0T1T5T6T11"] += 1
            else:
                invariantes_transicion["T0T1T2T3T4T11"] += 1

        start, end = match.span()
        # Sacamos la coincidencia de la cadena de transiciones para volver a buscar
        texto_a_iterar = texto_a_iterar[:start] + texto_a_iterar[end:]


# Reemplazamos los invariantes por el interleaving de transiciones que ocurren en medio
result = re.subn(regex, reemplazo, test_str)

coincidencias = result[1]

reconocer_invariante(test_str, result[1])

# Mientras que el número de reemplazos sea mayor a 0, seguimos ejecutando reemplazos
while result[1] > 0:
    aux = result[0]
    result = re.subn(regex, reemplazo, result[0])
    coincidencias += result[1]
    reconocer_invariante(aux, result[1])

# Si la cadena resultante resulta vacia, significa que la secuencia de T invariantes es correcta.
if len(result[0]) == 0:
    print("VALIDACION DE INVARIANTES EXITOSA")
# Sobraron transiciones, secuencia incorrecta.
else:
    print("VALIDACION DE INVARIANTES FALLIDA")
    print("Transiciones restantes: ", result[0])

print("COINCIDENCIAS:\t", coincidencias)
print("INVARIANTES:\t", invariantes_transicion, "\n")