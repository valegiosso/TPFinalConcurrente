package monitor;

import java.util.Arrays;

/**
 * Representa el marcado (estado) actual de la Red de Petri.
 * Mantiene la cantidad de tokens en cada plaza.
 */
public class VectorDeEstado {

    private int[] marcado;

    public VectorDeEstado(int[] marcadoInicial) {
        this.marcado = Arrays.copyOf(marcadoInicial, marcadoInicial.length);
    }

    public int[] getMarcado() {
        return marcado;
    }

    /**
     * Resta al marcado actual la columna Pre de la transicion disparada.
     * M = M - Pre(:, t)
     */
    public void restarColumna(int[] columnaPre) {
        for (int i = 0; i < marcado.length; i++) {
            marcado[i] -= columnaPre[i];
        }
    }

    /**
     * Suma al marcado actual la columna Post de la transicion disparada.
     * M = M + Post(:, t)
     */
    public void sumarColumna(int[] columnaPost) {
        for (int i = 0; i < marcado.length; i++) {
            marcado[i] += columnaPost[i];
        }
    }

    /**
     * Verifica los invariantes de plaza de la red.
     * Ecuaciones extraidas del analisis PIPE:
     *   M(P2) + M(P3) + M(P4) + M(P7) = 1
     *   M(P4) + M(P5) + M(P6) + M(P8) = 1
     *   M(P0) + M(P1) + M(P2) + M(P3) + M(P4) + M(P5) + M(P6) + M(P9) = 3
     */
    public boolean verificarInvariantePlazas() {
        int inv1 = marcado[2] + marcado[3] + marcado[4] + marcado[7];
        int inv2 = marcado[4] + marcado[5] + marcado[6] + marcado[8];
        int inv3 = marcado[0] + marcado[1] + marcado[2] + marcado[3]
                 + marcado[4] + marcado[5] + marcado[6] + marcado[9];

        return (inv1 == 1) && (inv2 == 1) && (inv3 == 3);
    }
}
