public class machineproblem2v3 {

static int determinant(int[][] m) {
    return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
         - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
         + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
}
    public static void main(String[] args) {
    int[][] matrix = {
        {4, -1, -3},
        {-2, 5, 1},
        {3, -2, 4}
    };
    System.out.println("Determinant: " + determinant(matrix));
}
}