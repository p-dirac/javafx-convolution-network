package datasci.model;

import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MatrixTests {

    private static final double DELTA = 1e-10;

    @Test
    void add() {
        double[] a = {4.0, 5.0, 7.0, 2.0, 1.0, 0.0};
        Matrix mA = new Matrix(2, 3, a);
        //
        double[] b = {2.0, 3.0, 8.0, 9.0, 1.0, 1.0};
        Matrix mB = new Matrix(2, 3, b);

        Matrix mC = MTX.add(mA, mB);

        double[] exAB = {6.0, 8.0, 15.0, 11.0, 2.0, 1.0};

        Assertions.assertArrayEquals(exAB, mC.a, DELTA,"MTX add failed");
    }

    @Test
    void multiply() {
        double[] w = {4.0, 5.0, 7.0, -2.0, 1.0, 0.0};
        Matrix mW = new Matrix(2, 3, w);
        //
        double[] x = {2.0, 3.0, 8.0, 9.0, 1.0, 1.0};
        Matrix mX = new Matrix(3, 2, x);

        Matrix mWX = MTX.mult(mW, mX);

        double[] exWX = {55.0, 64.0, 4.0, 3.0};

        Assertions.assertArrayEquals(exWX, mWX.a, DELTA,"MTX mult failed");
    }
    @Test
    void cellMultiply() {
        double[] u = {4.0, 5.0, 6.0, 2.0, 1.0, 0.0};
        Matrix mU = new Matrix(2, 3, u);
        //
        double[] v = {2.0, 3.0, -8.0, 9.0, 1.0, 1.0};
        Matrix mV = new Matrix(2, 3, v);

        Matrix mUV = MTX.cellMult(mU, mV);

        double[] exUV = {8.0, 15.0, -48.0, 18.0, 1.0, 0.0};

        Assertions.assertArrayEquals(exUV, mUV.a,DELTA, "MTX cellMultiply failed");
    }

    @Test
    void colToRow() {
        double[] w = {4.0, 5.0, -7.0, 2.0, -1.0, 0.0};
        Matrix mW = new Matrix(6, 1, w);
        //
        Matrix mC = MTX.colToRow(mW);
        // same array values just transposed from one column into one row
        double[] exC = {4.0, 5.0, -7.0, 2.0, -1.0, 0.0};

        Assertions.assertArrayEquals(exC, mC.a, DELTA, "MTX mulConstant failed");
    }

    @Test
    void rowToCol() {
        double[] w = {4.0, 5.0, -7.0, 2.0, -1.0, 0.0};
        Matrix mW = new Matrix(1, 6, w);
        //
        Matrix mC = MTX.rowToCol(mW);
        // same array values just transposed from one row into one column
        double[] exC = {4.0, 5.0, -7.0, 2.0, -1.0, 0.0};

        Assertions.assertArrayEquals(exC, mC.a, DELTA,"MTX mulConstant failed");
    }
    @Test
    void transpose() {
        double[] w = {4.0, 5.0, -7.0,
                      2.0, -1.0, 0.0};
        Matrix mW = new Matrix(2, 3, w);

        Matrix mT = MTX.transpose(mW);
        // same array values just transposed
        // from 2 rows, 3 columns to 3 rows, 2 cols
        double[] exT = {4.0, 2.0,
                        5.0, -1.0,
                       -7.0, 0.0};

        Assertions.assertArrayEquals(exT, mT.a, DELTA,"MTX transpose failed");
    }
    @Test
    void normalize() {
        double[] w = {4.0, 5.0, -7.0, 2.0, -1.0, 0.0};
        Matrix mW = new Matrix(2, 3, w);

        Matrix mNorm = MTX.normalize(mW);
        double max = 7.0;
        // same array values just scaled by abs max value
        double[] exNorm = {4.0/max, 5.0/max, -7.0/max, 2.0/max, -1.0/max, 0.0/max};


        Assertions.assertArrayEquals(exNorm, mNorm.a, DELTA,"MTX normalize failed");
    }

    @Test
    void mulConstant() {
        double[] w = {4.0, 5.0, -7.0, 2.0, -1.0, 0.0};
        Matrix mW = new Matrix(2, 3, w);
        //
        double u = -0.05;

        Matrix mC = MTX.mulConstant(mW, u);

        //   double[] exC = {-0.2, -0.25, -0.35, -0.1, -0.05, 0.0};
        double[] exC = {4.0*u, 5.0*u, -7.0*u, 2.0*u, -1.0*u, 0.0*u};

        Assertions.assertArrayEquals(exC, mC.a, DELTA,"MTX mulConstant failed");
    }

    @Test
    void getCell() {
        double[] im = {3.0, 0.0, 1.0, 2.0, 7.0, 4.0,
                1.0, 5.0, 8.0, 9.0, 3.0, 1.0,
                2.0, 7.0, 2.0, 5.0, 1.0, 3.0,
                0.0, 1.0, 3.0, 1.0, 7.0, 8.0,
                4.0, 2.0, 1.0, 6.0, 2.0, 8.0,
                2.0, 4.0, 5.0, 2.0, 3.0, 9.0};
        Matrix m = new Matrix(6, 6, im);

        int i = 1;
        int j = 4;
        double cell = MTX.getCell(m, i, j);

        double exCell = 3.0;

        Assertions.assertEquals(exCell, cell, DELTA,"MTX getCell failed");
    }

    @Test
    void maxCell() {
        double[] x = {1.0, 3.0, 2.0, 1.0,
                2.0, 9.0, 1.0, 1.0,
                1.0, 3.0, 2.0, 3.0,
                5.0, 6.0, 1.0, 2.0};
        Matrix m = new Matrix(4, 4, x);
        //
        double max = MTX.maxCell(m);

        double exMax = 9.0;

        Assertions.assertEquals(exMax, max, DELTA,"MTX maxCell failed");
    }
    @Test
    void maxAbsCell() {
        double[] x = {1.0, 3.0, 2.0, 1.0,
                2.0, -9.0, 1.0, 1.0,
                1.0, 3.0, 2.0, -3.0,
                5.0, 6.0, 1.0, 2.0};
        Matrix m = new Matrix(4, 4, x);
        //
        double max = MTX.maxAbsCell(m);

        double exMax = 9.0;

        Assertions.assertEquals(exMax, max, DELTA,"MTX maxAbsCell failed");
    }

    @Test
    void axPlusB() {
        double[] a = {4.0, 0.0, 1.0, 2.0, 1.0, 0.0};
        Matrix mA = new Matrix(2, 3, a);
        //
        double[] x = {2.0, 2.0, 1.0};
        Matrix mX = new Matrix(3, 1, x);
        //
        double[] b = {1.0, 3.0};
        Matrix mB = new Matrix(2, 1, b);

        Matrix mWX = MTX.aXplusB(mA, mX, mB);

        double[] exWX = {10.0, 9.0};

        Assertions.assertArrayEquals(exWX, mWX.a, DELTA,"MTX aXplusB failed");
    }

    @Test
    void subMatrix() {
        double[] i = {3.0, 0.0, 1.0, 2.0, 7.0, 4.0,
                1.0, 5.0, 8.0, 9.0, 3.0, 1.0,
                2.0, 7.0, 2.0, 5.0, 1.0, 3.0,
                0.0, 1.0, 3.0, 1.0, 7.0, 8.0,
                4.0, 2.0, 1.0, 6.0, 2.0, 8.0,
                2.0, 4.0, 5.0, 2.0, 3.0, 9.0};
        Matrix m = new Matrix(6, 6, i);

        int rowStart = 1;
        int rowEnd = 3;
        int colStart = 2;
        int colEnd = 4;
        Matrix mSub = MTX.subMatrix(m, rowStart, rowEnd, colStart, colEnd);

        double[] exSub = {8.0, 9.0, 3.0,
                2.0, 5.0, 1.0,
                3.0, 1.0, 7.0};

        Assertions.assertArrayEquals(exSub, mSub.a, DELTA,"MTX subMatrix failed");
    }

    @Test
    void convolve() {
        double[] i = {3.0, 0.0, 1.0, 2.0, 7.0, 4.0,
                1.0, 5.0, 8.0, 9.0, 3.0, 1.0,
                2.0, 7.0, 2.0, 5.0, 1.0, 3.0,
                0.0, 1.0, 3.0, 1.0, 7.0, 8.0,
                4.0, 2.0, 1.0, 6.0, 2.0, 8.0,
                2.0, 4.0, 5.0, 2.0, 3.0, 9.0};
        Matrix m = new Matrix(6, 6, i);
        //
        double[] u = {1.0, 0.0, -1.0,
                1.0, 0.0, -1.0,
                1.0, 0.0, -1.0,};
        Matrix f = new Matrix(3, 3, u);

        Matrix mC = MTX.convolve(m, f);

        double[] exC = {-5.0, -4.0, 0.0, 8.0,
                -10.0, -2.0, 2.0, 3.0,
                0.0, -2.0, -4.0, -7.0,
                -3.0, -2.0, -3.0, -16.0};


        Assertions.assertArrayEquals(exC, mC.a, DELTA,"MTX convolve failed");
    }
    @Test
    void copyAndPad() {
        double[] x = {1.0, 3.0, 2.0, 1.0,
                2.0, 9.0, 1.0, 1.0,
                1.0, 3.0, 2.0, 3.0,
                5.0, 6.0, 1.0, 2.0};
        Matrix m = new Matrix(4, 4, x);
        //
        Matrix mP = MTX.copyAndPad(m, 2);

        // pad with 2 zero cells around edges
        double[] exP = {
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 3.0, 2.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 2.0, 9.0, 1.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 3.0, 2.0, 3.0, 0.0, 0.0,
                0.0, 0.0, 5.0, 6.0, 1.0, 2.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        Assertions.assertArrayEquals(exP, mP.a, DELTA,"MTX copyAndPad failed");
    }

    @Test
    void maxPool() {
        double[] x = {1.0, 3.0, 2.0, 1.0,
                2.0, 9.0, 1.0, 1.0,
                1.0, 3.0, 2.0, 3.0,
                5.0, 6.0, 1.0, 2.0};
        Matrix m = new Matrix(4, 4, x);
        //
        Matrix mP = MTX.maxPool(m, 2, 2);

        double[] exP = {9.0, 2.0,
                6.0, 3.0};

        Assertions.assertArrayEquals(exP, mP.a, DELTA,"MTX maxPool failed");
    }

    @Test
    void poolIndex() {
        double[] x = {0.0, 55.0, 0.0, 0.0,
                20.0, 0.0, 41.0, 33.0,
                0.0, 90.0, 0.0, 0.0,
                0.0, 57.0, 0.0, 95.0};
        Matrix m = new Matrix(4, 4, x);
        //
        Matrix mPI = MTX.poolIndex(m, 2, 2);

        double[] exPI = {1.0, 6.0, 9.0, 15.0};

        Assertions.assertArrayEquals(exPI, mPI.a, DELTA,"MTX poolIndex failed");
    }

    @Test
    void rotate() {
        double[] f = {4.0, 5.0, -7.0, 2.0, -1.0, 0.0, 2.0, 3.0, 6.0};
        Matrix mF = new Matrix(3, 3, f);

        Matrix mR = MTX.rotate(mF);

        //   double[] exC = {-0.2, -0.25, -0.35, -0.1, -0.05, 0.0};
        double[] exR = {6.0, 3.0, 2.0, 0.0, -1.0, 2.0, -7.0, 5.0, 4.0};

        Assertions.assertArrayEquals(exR, mR.a, DELTA,"MTX rotate failed");
    }

    @Test
    void splitMatrix() {
        double[] x = {1.0, 3.0, 2.0, 1.0,
                2.0, 9.0, 1.0, 1.0,
                1.0, 3.0, 2.0, 3.0,
                5.0, 6.0, 1.0, 2.0};
        Matrix m = new Matrix(4, 4, x);
        //
        // split matrix m into 4 smaller matrix
        List<Matrix> mList = MTX.splitMatrix(m, 4);
        // get 2nd matrix from list (2nd row in matrix m)
        Matrix m1 = mList.get(1);
        // get 4th matrix from list (4th row in matrix m)
        Matrix m3 = mList.get(3);

        double[] exM1 = {2.0, 9.0, 1.0, 1.0};

        Assertions.assertArrayEquals(exM1, m1.a, DELTA,"MTX splitMatrix m1 failed");

        double[] exM3 = {5.0, 6.0, 1.0, 2.0};

        Assertions.assertArrayEquals(exM3, m3.a, DELTA,"MTX splitMatrix m3 failed");

    }
    @Test
    void toSingleCol() {
        double[] x1 = {1.0, 3.0, 2.0, 1.0,
                4.0, 3.0, 2.0, 5.0};
        Matrix m1 = new Matrix(2, 4, x1);
        double[] x2 = {2.0, -8.0, 1.0, 1.0};
        Matrix m2 = new Matrix(1, 4, x2);
        double[] x3 = {1.0, 3.0, 2.0, 3.0};
        Matrix m3 = new Matrix(1, 4, x3);
        double[] x4 = {5.0, 6.0, 1.0, -2.0};
        Matrix m4 = new Matrix(1, 4, x4);

        //
        // split matrix m into 4 smaller matrix
        List<Matrix> mList = new ArrayList<>();
        mList.add(m1);
        mList.add(m2);
        mList.add(m3);
        mList.add(m4);
        //
        Matrix c = MTX.listToSingleCol(mList);
        //
        double[] exC = {1.0, 3.0, 2.0, 1.0,
                4.0, 3.0, 2.0, 5.0,
                2.0, -8.0, 1.0, 1.0,
                1.0, 3.0, 2.0, 3.0,
                5.0, 6.0, 1.0, -2.0};

        Assertions.assertArrayEquals(exC, c.a, DELTA,"MTX toSingleCol failed");


    }
    @Test
    void concatInplace() {
        double[] x1 = {1.0, 3.0, 2.0, 1.0,
                2.0, -5.0, 1.0, 1.0,
                1.0, 3.0, 2.0, 3.0,
                5.0, -6.0, 1.0, 2.0};
        Matrix m1 = new Matrix(4, 4, x1);

        double[] x2 = {5.0, 3.0, 2.0, 1.0,
                2.0, 8.0, 1.0, 1.0,
                4.0, 3.0, 2.0, 3.0,
                5.0, 4.0, 1.0, 2.0};
        Matrix m2 = new Matrix(4, 4, x2);
        //
        // replace m1 with concatenated matrix
        MTX.concatInplace(m1, m2);
        //
        double[] exM1 = {1.0, 3.0, 2.0, 1.0,
                2.0, -5.0, 1.0, 1.0,
                1.0, 3.0, 2.0, 3.0,
                5.0, -6.0, 1.0, 2.0,
                5.0, 3.0, 2.0, 1.0,
                2.0, 8.0, 1.0, 1.0,
                4.0, 3.0, 2.0, 3.0,
                5.0, 4.0, 1.0, 2.0  };

        Assertions.assertArrayEquals(exM1, m1.a, DELTA,"MTX concatInplace failed");
    }

    @Test
    void listSum() {
        double[] x1 = {1.0, 3.0, 2.0, 1.0};
        Matrix m1 = new Matrix(4, 1, x1);
        double[] x2 = {2.0, -8.0, 1.0, 1.0};
        Matrix m2 = new Matrix(4, 1, x2);
        double[] x3 = {1.0, 3.0, 2.0, 3.0};
        Matrix m3 = new Matrix(4, 1, x3);
        double[] x4 = {5.0, 6.0, 1.0, -2.0};
        Matrix m4 = new Matrix(4, 1, x4);

        //
        // list of column matrix
        List<Matrix> mList = new ArrayList<>();
        mList.add(m1);
        mList.add(m2);
        mList.add(m3);
        mList.add(m4);
        //
        Matrix c = MTX.sumOfList(mList);
        // average matrix
        double[] exC = {9.0, 4.0, 6.0, 3.0};

        Assertions.assertArrayEquals(exC, c.a, DELTA,"MTX sumOfList failed");


    }
    @Test
    void listAverage() {
        double[] x1 = {1.0, 3.0, 2.0, 1.0};
        Matrix m1 = new Matrix(4, 1, x1);
        double[] x2 = {2.0, -8.0, 1.0, 1.0};
        Matrix m2 = new Matrix(4, 1, x2);
        double[] x3 = {1.0, 3.0, 2.0, 3.0};
        Matrix m3 = new Matrix(4, 1, x3);
        double[] x4 = {5.0, 6.0, 1.0, -2.0};
        Matrix m4 = new Matrix(4, 1, x4);

        //
        // list of column matrix
        List<Matrix> mList = new ArrayList<>();
        mList.add(m1);
        mList.add(m2);
        mList.add(m3);
        mList.add(m4);
        //
        Matrix c = MTX.averageOfList(mList);
        // average matrix
        double[] exC = {9.0/4, 4.0/4, 6.0/4, 3.0/4};

        Assertions.assertArrayEquals(exC, c.a, DELTA,"MTX averageOfList failed");


    }

}

