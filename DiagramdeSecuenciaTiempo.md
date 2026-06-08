sequenceDiagram
    participant Usuario as Actor
    participant rdP as "rdP: RdP"
    participant matrizI as "matrizI: Matrizi"
    participant vectorDeEstado as "vectorDeEstado: VectorDeEstado"
    participant vectorSensibilizadas as "vectorSensibilizadas: VectorSensibilizadas"
    participant sensibilizadoConTiempo as "sensibilizadoConTiempo: SensibilizadoConTiempo"
    participant mutex as "mutex: Mutex"

    Usuario->>rdP: 1: disparar()
    activate rdP

    rdP->>vectorSensibilizadas: 1.1: estaSensibilizado()
    activate vectorSensibilizadas

    vectorSensibilizadas->>sensibilizadoConTiempo: 1.1.1: testVentanaTiempo()
    sensibilizadoConTiempo-->>vectorSensibilizadas: 1.1.2: ventana

    alt ventana == true
        alt alfa < (ahora-timeStamp) < beta and esperando == false or viene de sleep
            alt esperando == false
                vectorSensibilizadas->>sensibilizadoConTiempo: 1.1.3: setNuevoTimeStamp()
                sensibilizadoConTiempo-->>vectorSensibilizadas: 1.1.4: k=true
                vectorSensibilizadas-->>rdP: 1.1.5: k=true
            else esperando == true
                vectorSensibilizadas-->>rdP: 1.1.6: k=false
            end
        else alfa < (ahora-timeStamp) < beta and esperando == true y no viene de sleep
            vectorSensibilizadas-->>rdP: 1.1.7: k=false
        end
    else ventana == false
        alt antes == true
            vectorSensibilizadas->>sensibilizadoConTiempo: 1.1.8: antesDeLaVentana()
            vectorSensibilizadas->>mutex: 2: release
            vectorSensibilizadas->>sensibilizadoConTiempo: 3: setEsperando()
            vectorSensibilizadas->>vectorSensibilizadas: 4: sleep(timeStamp+alfa-ahora)
        else antes == false
            vectorSensibilizadas-->>rdP: 5: k=false
        end
    end
    deactivate vectorSensibilizadas

    alt k == true
        rdP->>vectorDeEstado: 6: calculoDeVectorEstado()
        rdP->>sensibilizadoConTiempo: 6.1: resetEsperando()
        rdP->>matrizI: 6.2: getColumna()
        matrizI-->>rdP: 6.3: columna
        rdP->>vectorDeEstado: 6.3: estado = estado + columna
        rdP->>rdP: 6.4: #columna = 0

        loop #columna < #transiciones
            rdP->>matrizI: 6.5: getColumna()
            rdP->>vectorSensibilizadas: 6.6: actualiceSensibilizadoT()
            vectorSensibilizadas->>sensibilizadoConTiempo: 6.6.1: setNuevoTimeStamp()
            rdP->>rdP: 7: #columna++
        end

        rdP-->>Usuario: 8.1: k = true
    else k == false
        rdP-->>Usuario: 9: k = false
    end
    deactivate rdP