package monitor;

/**
 * Representa la matriz de incidencia (Pre o Post) de la Red de Petri.
 * Cada columna corresponde a una transicion, cada fila a una plaza.
 */
public class Matrizi {

    private final int[][] matrix;

    public Matrizi(int[][] matrix) {
        this.matrix = matrix;
    }

    /**
     * Devuelve la columna correspondiente a la transicion indicada.
     * Es decir, el vector de plazas afectadas por esa transicion.
     */
    public int[] getColumna(int transition) {
        int filas = matrix.length;
        int[] columna = new int[filas];
        for (int i = 0; i < filas; i++) {
            columna[i] = matrix[i][transition];
        }
        return columna;
    }

    public int getFilas() {
        return matrix.length;
    }

    public int getColumnas() {
        return matrix[0].length;
    }
}
