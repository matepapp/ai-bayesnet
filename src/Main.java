import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.linear.*;

/**
 * Created by matepapp on 2016. 11. 01..
 */
public class Main {
    static double[] parameters;
    static int I;
    static int J;
    static int L;
    static double beta;
    static int alfaU;
    static int alfaV;
    static int burnin;
    static int iteration;

    static double[][] H;
    static double[][] U;
    static double[][] V;
    static double[][] LUnionMatrix;
    static double[] LNullVector;

    static RealMatrix HMatrix;
    static RealMatrix UMatrix;
    static RealMatrix VMatrix;
    static RealMatrix diagonalMatrix;
    static RealMatrix nullVector;

    public static void main(String[] args) {
        InputReader reader = new InputReader();
        parameters = reader.readDoubleLine();
        I = (int) parameters[0];
        J = (int) parameters[1];
        L = (int) parameters[2];
        beta = parameters[3];
        alfaU = 1;
        alfaV = 1;
        burnin = 15;
        iteration = 5;

        H = new double[I][J];
        U = new double[L][I];
        V = new double[L][J];
        LUnionMatrix = new double[L][L];
        LNullVector = new double[L];

        // read the H matrix from input
        for (int i = 0; i < I; i++)
            H[i] = reader.readDoubleLine();

        // initialize the L union matrix
        for (int i = 0; i < L; i++)
            for (int j = 0; j < L; j++)
                if (i == j)
                    LUnionMatrix[i][j] = 1.0;
                else
                    LUnionMatrix[i][j] = 0.0;

        // initialize the L null vector
        for (int i = 0; i < L; i++)
            LNullVector[i] = 0.0;

        HMatrix = new Array2DRowRealMatrix(H);
        UMatrix = new Array2DRowRealMatrix(U);
        VMatrix = new Array2DRowRealMatrix(V);
        diagonalMatrix = new Array2DRowRealMatrix(LUnionMatrix);
        nullVector = new Array2DRowRealMatrix(LNullVector);

        // initialize the U matrix
        double alfa = Math.pow((double) alfaU, -1.0);
        RealMatrix covarianceMatrix = diagonalMatrix.scalarMultiply(alfa);
        for (int i = 0; i < I; i++) {
            double[] numbers = calculateNormalDistribution(nullVector.getColumn(0), covarianceMatrix.getData());
            UMatrix.setColumn(i, numbers);
        }

        // initialize the V matrix
        alfa = Math.pow((double) alfaV, -1.0);
        covarianceMatrix = diagonalMatrix.scalarMultiply(alfa);
        for (int j = 0; j < J; j++) {
            double[] numbers = calculateNormalDistribution(nullVector.getColumn(0), covarianceMatrix.getData());
            VMatrix.setColumn(j, numbers);
        }

        // refresh U and V matrix burnin times
        for (int i = 0; i < burnin; i++) {
            refreshUMatrix();
            refreshVMatrix();
        }

        // refresh and save U and V matrix iteration times
        RealMatrix USumMatrix = new Array2DRowRealMatrix(L, I);
        RealMatrix VSumMatrix = new Array2DRowRealMatrix(L, J);
        for (int i = 0; i < iteration; i++) {
            USumMatrix = USumMatrix.add(refreshUMatrix());
            VSumMatrix = VSumMatrix.add(refreshVMatrix());
        }

        double substract = (double) 1/iteration;
        USumMatrix = USumMatrix.scalarMultiply(substract).transpose();
        VSumMatrix = VSumMatrix.scalarMultiply(substract).transpose();

        RealMatrix result = USumMatrix.multiply(VSumMatrix.transpose());

        printMatrix(USumMatrix);
        System.out.println();
        printMatrix(VSumMatrix);
//        System.out.println();
//        System.out.println();
//        printMatrix(result);
    }

    private static RealMatrix refreshUMatrix() {
        for (int i = 0; i < I; i++) {
            RealMatrix sumMatrix = new Array2DRowRealMatrix(L, L);
            for (int j = 0; j < J; j++) {
                RealMatrix columnMatrix = new Array2DRowRealMatrix(VMatrix.getColumn(j));
                RealMatrix transposedColumnMatrix = columnMatrix.transpose();
                sumMatrix = sumMatrix.add(columnMatrix.multiply(transposedColumnMatrix));
            }
            sumMatrix = sumMatrix.scalarMultiply(beta);
            RealMatrix alfaUnitMatrix = diagonalMatrix.scalarMultiply((double) alfaU);
            RealMatrix lamdaMatrix = sumMatrix.add(alfaUnitMatrix);

            sumMatrix = new Array2DRowRealMatrix(L, 1);
            for (int j = 0; j < J; j++) {
                RealMatrix columnMatrix = new Array2DRowRealMatrix(VMatrix.getColumn(j));
                double entry = HMatrix.getEntry(i, j);
                sumMatrix = sumMatrix.add(columnMatrix.scalarMultiply(entry));
            }
            sumMatrix = sumMatrix.scalarMultiply(beta);
            RealMatrix inverseLambdaMatrix = new LUDecomposition(lamdaMatrix).getSolver().getInverse();
            RealMatrix ksziMatrix = inverseLambdaMatrix.multiply(sumMatrix);

            double[] numbers = calculateNormalDistribution(ksziMatrix.getColumn(0), inverseLambdaMatrix.getData());
            UMatrix.setColumn(i, numbers);
        }

        return UMatrix;
    }

    private static RealMatrix refreshVMatrix() {
        for (int j = 0; j < J; j++) {
            RealMatrix sumMatrix = new Array2DRowRealMatrix(L, L);
            for (int i = 0; i < I; i++) {
                RealMatrix columnMatrix = new Array2DRowRealMatrix(UMatrix.getColumn(i));
                RealMatrix transposedColumnMatrix = columnMatrix.transpose();
                sumMatrix = sumMatrix.add(columnMatrix.multiply(transposedColumnMatrix));
            }
            sumMatrix = sumMatrix.scalarMultiply(beta);
            RealMatrix alfaUnitMatrix = diagonalMatrix.scalarMultiply((double) alfaV);
            RealMatrix lamdaMatrix = sumMatrix.add(alfaUnitMatrix);

            sumMatrix = new Array2DRowRealMatrix(L, 1);
            for (int i = 0; i < I; i++) {
                RealMatrix columnMatrix = new Array2DRowRealMatrix(UMatrix.getColumn(i));
                double entry = HMatrix.getEntry(i, j);
                sumMatrix = sumMatrix.add(columnMatrix.scalarMultiply(entry));
            }
            sumMatrix = sumMatrix.scalarMultiply(beta);
            RealMatrix inverseLambdaMatrix = new LUDecomposition(lamdaMatrix).getSolver().getInverse();
            RealMatrix ksziMatrix = inverseLambdaMatrix.multiply(sumMatrix);

            double[] numbers = calculateNormalDistribution(ksziMatrix.getColumn(0), inverseLambdaMatrix.getData());
            VMatrix.setColumn(j, numbers);
        }

        return VMatrix;
    }

    public static double[] calculateNormalDistribution(double[] means, double[][] covarianceMatrix) {
        MultivariateNormalDistribution mnd = new MultivariateNormalDistribution(means, covarianceMatrix);
        return mnd.sample();
    }

    public static void printMatrix(RealMatrix matrix) {
        int columns = matrix.getColumnDimension();
        int rows = matrix.getRowDimension();
        double[][] matrixArray = matrix.getData();

        for (int l = 0; l < rows; l++)
            for (int j = 0; j < columns; j++) {
                if (j != columns - 1)
                    System.out.print(matrixArray[l][j] + ",");
                else
                    System.out.println(matrixArray[l][j]);
            }
    }
}
