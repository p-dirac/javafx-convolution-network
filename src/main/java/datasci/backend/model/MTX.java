package datasci.backend.model;
/*******************************************************************************
 *
 * Copyright 2022 Ronald Cook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 Matrix in one-D row format
 */
public class MTX {

    private static final Logger LOG = Logger.getLogger(MTX.class.getName());

    public static double Hi_LIMIT = 1.0E6;
    public static double Low_LIMIT = 1.0E-6;
    public static double Loss_Low_LIMIT = 1.0E-3;

    //
    // Matrix library for flat matrix operations
    //
    // All matrix are assumed to contain a single array a[] for cell values.
    // size = rows * cols = total number of cells in matrix
    // a[] = one-D array to flatten matrix
    // two-D (i,j) row, column index to one-D array index k = i*c + j where c = number of columns
    // matrix cell(i, j) = a[i*c * j]
    //
    // How to get i,j from array index k?
    //    i = int(k/c),  j = k mod c
    //
    //
    private MTX() {
    }

    /**
     Create a new Matrix.

     @param rows the rows
     @param cols the cols
     @return new matrix, initialized with zero values
     */
    public static Matrix createMatrix(int rows, int cols) {
        Matrix m = new Matrix();
        m.rows = rows;
        m.cols = cols;
        m.size = rows * cols;
        // init array to all zero
        m.a = new double[m.size];
        return m;
    }

    /**
     Create a new Matrix.

     @param rows the number of rows
     @param cols the number of cols
     @param a    array to initialize new matrix; must have size = rows*cols
     @return new matrix
     */
    public static Matrix createMatrix(int rows, int cols, double[] a) {
        Matrix m = new Matrix();
        m.rows = rows;
        m.cols = cols;
        m.size = rows * cols;
        // init array
        //The memory address of the array is passed (reference) in the method argument.
        //Therefore, any changes to this array in the method will affect the input array argument.
        //  m.a = a; //don't use
        // To avoid this, use copy:
        m.a = Arrays.copyOf(a, m.size);
        return m;
    }

    /**
     Copy matrix to new matrix

     @param b matrix to copy
     @return new matrix = copy of matrix b
     */
    public static Matrix copy(Matrix b) {
        Matrix m = new Matrix();
        m.rows = b.rows;
        m.cols = b.cols;
        m.size = b.size;
        // copy all matrix cells
        m.a = Arrays.copyOf(b.a, b.size);
        return m;
    }

    /**
     Log elements of matrix

     @param b matrix to copy
     @param n number of elements to log
     */
    public static void logMatrx(String name, Matrix b, int n) {
        LOG.info(name + ", matrix: " + b);
        for (int k = 0; k < n; k++) {
            LOG.info("k: " + k + ", a[k]: " + b.a[k]);
        }
    }

    /**
     Log matrix if min, max limits exceedded

     @param name matrix name
     @param b    matrix to log
     */
    public static void logMinMax(String name, Matrix b) {
        double maxB = MTX.maxAbsCell(b);
        double minB = MTX.minAbsCell(b);
        if (maxB > MTX.Hi_LIMIT || (0 < minB && minB < MTX.Low_LIMIT)) {
            LOG.info(name + ", matrix: " + b);
        }
    }
    /**
     Log matrix if fist, last, min, max not all zero

     @param name matrix name
     @param b    matrix to log
     */
    public static void logNotZero(String name, Matrix b) {
        double first = b.a[0];
        int len = b.size;
        double last = b.a[len - 1];
        double maxB = MTX.maxAbsCell(b);
        double minB = MTX.minAbsCell(b);
        double ave = MTX.aveCell(b);
        if (first != 0.0 && last != 0.0 && maxB != 0.0 &&  minB != 0.0 && ave != 0.0) {
            LOG.info(name + ", matrix: " + b);
        }
    }

    /**
     Create i,j index from array index k,
     where i = row index; j = column index
     Array index k = i*cols + j

     @param cols numder of columns of some matrix
     @param k    array index
     @return i, j index record
     */
    public static IndexIJ kToIJ(int cols, int k) {
        // division of integers with truncation
        int i = k / cols;
        // remainder
        int j = k % cols;
        return new IndexIJ(i, j);
    }

    /**
     Initialize matrix cells to specified value.

     @param m   matrix to Initialize
     @param val specified value
     */
    public static void initValue(Matrix m, double val) {
        for (int k = 0; k < m.size; k++) {
            // initialize each matrix cell to single value
            m.a[k] = val;
        }
    }

    /**
     Create square identity matrix

     @param rows number of rows
     @param cols number of columns (must equal number of rows)
     @return square identity matrix
     */
    public static Matrix createIdentMatrix(int rows, int cols) {
        Matrix m = new Matrix(rows, cols);
        // all cells init to zero, change diagonal to 1.0
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                // initialize diagonal cell to 1.0
                if (i == j) {
                    m.a[i * m.cols + j] = 1.0;
                }
            }
        }
        return m;
    }
    /**
     Create square diagonal matrix

     @param rows number of rows
     @param cols number of columns (must equal number of rows)
     @param diag diagonal value
     @return square diagonal matrix
     */
    public static Matrix createDiagonalMatrix(int rows, int cols, double diag) {
        Matrix m = new Matrix(rows, cols);
        // all cells init to zero, change diagonal to diag
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                // initialize diagonal cell to diag
                if (i == j) {
                    m.a[i * m.cols + j] = diag;
                }
            }
        }
        return m;
    }

    /**
     Compare two matrix, cell by cell

     @param m matrix to compare
     @param b matrix to compare
     @return true if matrix m = matrix b, otherwise false
     */
    public static boolean equals(Matrix m, Matrix b) {
        boolean same = false;
        boolean rowsSame = (m.rows == b.rows);
        boolean colsSame = (m.cols == b.cols);
        boolean valSame = Arrays.equals(m.a, b.a);
        // all conditions must be true if matrix m = matrix b
        same = rowsSame && colsSame && valSame;
        return same;
    }

    /**
     Set matrix cells from 2D array

     @param m matrix to set
     @param b 2D array to be converted to flat matrix array
     */
    public static void setFrom2D(Matrix m, double[][] b) {
        // set matrix a = matrix b
        // convert 2D array to 1D array
        m.a = Stream.of(b)
                .flatMapToDouble(DoubleStream::of)
                .toArray();
    }

    /**
     Create column matrix

     @param b 1D array to be converted to flat column matrix
     @return column matrix
     */
    public static Matrix createColMatrix(double[] b) {
        Matrix m = new Matrix();
        // set 1D matrix a = 1D matrix b
        // convert 1D array to 1D array
        m.rows = b.length;
        m.cols = 1;
        m.size = m.rows;
        m.a = Arrays.copyOf(b, m.size);
        return m;
    }

    /**
     Create row matrix

     @param b 1D array to be converted to flat row matrix
     @return row matrix
     */
    public static Matrix createRowMatrix(double[] b) {
        Matrix m = new Matrix();
        // set 1D matrix a = 1D matrix b
        // convert 1D array to 1D array
        m.rows = 1;
        m.cols = b.length;
        m.size = m.cols;
        m.a = Arrays.copyOf(b, m.size);
        return m;
    }


    /**
     Get stream of this matrix

     @param m matrix to stream
     @return stream of this matrix
     */
    public static DoubleStream getStreamA(Matrix m) {

        return DoubleStream.of(m.a);
    }

    /**
     Concatenate matrix m with matrix b by rows
     Matrix b must have same number of cols as matrix m
     Result matrix #rows = this #rows + b #rows
     Result matrix #cols = this #cols

     @param m matrix to concatenate
     @param b matrix to concatenate
     @return Concatenation of this matrix with matrix b by rows
     */
    public static Matrix concat(Matrix m, Matrix b) {
        Matrix c = new Matrix();
        c.rows = m.rows + b.rows;
        c.cols = m.cols;
        c.a = DoubleStream.concat(Arrays.stream(m.a), Arrays.stream(b.a)).toArray();
        return c;
    }

    /**
     Sets cell value at given row index and column index

     @param m    matrix of cells
     @param rowI row index i
     @param colJ column index j
     @param val  cell value to set
     */
    public static void setCell(Matrix m, int rowI, int colJ, double val) {
        // row formatted array: index = rowI * cols + colJ
        m.a[rowI * m.cols + colJ] = val;
    }

    /**
     Sets cell value at given array index

     @param m     matrix of cells
     @param cellK array index k, where k = i*cols + j
     @param val   cell value to set
     */
    public static void setCell(Matrix m, int cellK, double val) {
        // row formatted array: index cellK = rowI * cols + colJ
        m.a[cellK] = val;
    }

    /**
     Gets cell value by row index and column index

     @param m    matrix of cells
     @param rowI the row i
     @param colJ the col j
     @return the cell value
     */
    public static double getCell(Matrix m, int rowI, int colJ) {
        return m.a[rowI * m.cols + colJ];
    }

    /**
     Gets cell value by array index

     @param m     matrix of cells
     @param cellK array index k, where k = i*cols + j
     @return the cell value
     */
    public static double getCell(Matrix m, int cellK) {
        return m.a[cellK];
    }

    /**
     Update one matrix cell value by a constant

     @param m     matrix of cells to be incremented
     @param rowI  cell row i
     @param colJ  cell col j
     @param delta increment to add to specified cell
     */
    public static void updateCell(Matrix m, int rowI, int colJ, double delta) {
        // row formatted array: index = rowI * cols + colJ
        m.a[rowI * m.cols + colJ] += delta;
    }

    /**
     Get row array from matrix

     @param m    matrix with row to retrieve
     @param rowI the row i
     @return row array from matrix
     */
    public static double[] getRow(Matrix m, int rowI) {
        double[] row = new double[m.cols];
        // copy all colums in single row
        row = Arrays.copyOfRange(m.a, rowI * m.cols, rowI * m.cols + m.cols);
        return row;
    }

    /**
     Get column array from matrix

     @param m    matrix with column to retrieve
     @param colJ the col j
     @return column array from matrix
     */
    public static double[] getCol(Matrix m, int colJ) {
        double[] col = new double[m.rows];
        for (int i = 0; i < m.rows; i++) {
            col[i] = m.a[i * m.cols + colJ];
        }
        return col;
    }

    /**
     Gets sub matrix.

     @param m        matrix with submatrix
     @param rowStart the row start in matrix m
     @param rowEnd   the row end in matrix m
     @param colStart the col start in matrix m
     @param colEnd   the col end in matrix m
     @return sub matrix
     */
    public static Matrix subMatrix(Matrix m, int rowStart, int rowEnd, int colStart, int colEnd) {
        int subRows = rowEnd - rowStart;
        int subCols = colEnd - colStart;
        //   LOG.info("subRows: " + subRows + ", subCols: " + subCols);
        Matrix subMatrix = new Matrix(subRows, subCols);
        try {
            // copy cols from a matrix row to rows of subMatrix
            int subI = 0;
            int subJ = 0;
            for (int i = rowStart; i < rowEnd; i++) {
                for (int j = colStart; j < colEnd; j++) {
                    // get specified cell(i, j) from matrix m to form sub matrix cell(subI, subJ)
                    subMatrix.a[subI * subCols + subJ] = getCell(m, i, j);
                    // next col of sub matrix
                    subJ++;
                }
                subJ = 0;
                // next row of sub matrix
                subI++;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return subMatrix;
    }

    /**
     Split this matrix into list of n submatrix
     Each submatrix will have same number of columns as this matrix

     @param m matrix to be split
     @param n number of submatrix
     @return n sub matrix in list
     */
    public static List<Matrix> splitMatrix(Matrix m, int n) {
        List<Matrix> subMatrixList = new ArrayList<>();
        try {
            int subSize = m.size / n;
            int subRows = m.rows / n;
            int start = 0;
            int end = start + subSize;
            for (int i = 0; i < n; i++) {
                Matrix subMatrix = new Matrix(subRows, m.cols);
                subMatrix.a = Arrays.copyOfRange(m.a, start, end);
                subMatrixList.add(subMatrix);
                start += subSize;
                end += subSize;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return subMatrixList;
    }

    /**
     Transpose matrix.

     @param m matrix to be transposed
     @return transposed matrix
     */
    public static Matrix transpose(Matrix m) {
        // number of rows and colums transposed
        // if matrix m is 3 rows by 2 cols
        // transpose will be 2 rows by 3 cols
        Matrix trans = new Matrix();
        try {
            trans.rows = m.cols;
            trans.cols = m.rows;
            trans.size = m.size;
            trans.a = new double[m.size];
            for (int i = 0; i < m.rows; i++) {
                for (int j = 0; j < m.cols; j++) {
                    // this matrix has number of cols = cols
                    int oldIJ = i * m.cols + j;
                    // transpose has number of cols = rows
                    int newJI = j * m.rows + i;
                    // transpose cell at j,i = this matrix cell at i,j
                    trans.a[newJI] = m.a[oldIJ];
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return trans;
    }

    /**
     Rotate matrix: reverse elements

     @param m matrix to be rotated
     @return rotated matrix
     */
    public static Matrix rotate(Matrix m) {
        Matrix rotated = copy(m);
        try {
            //   Collections.reverse(Arrays.asList(rotated.a));
            int len = 0;
            if (rotated.a != null) {
                len = rotated.a.length;
            }
            for (int i = 0; i < len / 2; i++) {
                double temp = rotated.a[i];
                rotated.a[i] = rotated.a[len - i - 1];
                rotated.a[len - i - 1] = temp;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return rotated;
    }

    /**
     Create single column matrix from list of matrix

     @param mList list of matrix
     @return single column matrix
     */
    public static Matrix listToSingleCol(List<Matrix> mList) {
        Matrix singleCol = null;
        try {
            // n = number of matrix in list
            int n = mList.size();
            // check first matrix for size
            Matrix m = mList.get(0);
            int totalRows = n * m.rows;
            // init single column output matrix with first matrix from list
            singleCol = copy(m);
            int totalSize = m.size;
            for (int k = 1; k < n; k++) {
                // next matrix from input list
                Matrix u = mList.get(k);
                totalSize += u.size;
                // concatenate as array to output matrix
                concatInplace(singleCol, u);
            }
            // now make it single column'
            singleCol.cols = 1;
            singleCol.rows = totalSize;
            singleCol.size = totalSize;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return singleCol;
    }

    /**
     Concatenate in place matrix m with matrix b by rows
     Matrix b must have same number of cols as matrix m
     Result matrix #rows = m #rows + b #rows
     Result matrix #cols = m #cols

     @param m input matrix to be concatenated, changed by operation
     @param b matrix to be concatenated to matrix m
     */
    public static void concatInplace(Matrix m, Matrix b) {
        try {
            // replace array in matrix m
            m.a = DoubleStream.concat(Arrays.stream(m.a), Arrays.stream(b.a)).toArray();
            // replace matrix m sizes
            m.rows = m.rows + b.rows;
            m.size = m.rows * m.cols;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }

    /**
     Find transpose of single column matrix

     @param m column matrix to be transposed
     @return single row matrix: the transpose of single column matrix
     */
    public static Matrix colToRow(Matrix m) {
        Matrix c = new Matrix();
        try {
            c.rows = 1;
            c.cols = m.size;
            c.size = m.size;
            // copy all matrix cells
            c.a = Arrays.copyOf(m.a, m.size);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }
    /**
     Find transpose of single column matrix

     @param m column matrix to be transposed
     */
    public static void colToRowInPlace(Matrix m) {
        try {
            m.rows = 1;
            m.cols = m.size;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Find transpose of single row matrix

     @param m row matrix to be transposed
     @return single column matrix: the transpose of single row matrix
     */
    public static Matrix rowToCol(Matrix m) {
        Matrix c = new Matrix();
        try {
            c.rows = m.size;
            c.cols = 1;
            c.size = m.size;
            // copy all matrix cells
            c.a = Arrays.copyOf(m.a, m.size);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }

    /**
     Multiply two matrix.
     Number of columns in matrix m = number of rows in matrix b
     Product matrix:  #rows = m #rows, #cols = b #cols

     @param m matrix to be multiplied
     @param b matrix to be multiplied
     @return product matrix = m * b
     */
    public static Matrix mult(Matrix m, Matrix b) {
        Matrix prodMatrix = null;
        try {
            // product matrix c = matrix m * matrix b
            // c[i,j] = m[i,k]*b[k,j] summed over k
            // c #rows = m #rows, c #cols = b #cols
            // bcols = number of cols in matrix b
        //    m.checkNaN("mult m");
        //    b.checkNaN("mult b");
            int bcols = b.cols;
            // crows = number of rows in matrix c
            int crows = m.rows;
            // ccols = number of cols in matrix c
            int ccols = bcols;
            prodMatrix = new Matrix(crows, ccols);
            for (int i = 0; i < m.rows; i++) {
                // i = row # in this matrix a
                for (int j = 0; j < bcols; j++) {
                    // j = col # in matrix b
                    double temp = 0;
                    for (int k = 0; k < m.cols; k++) {
                        //  k = col # in matrix a or row # in matrix b
                        // c[i,j] = a[i,k]*b[k,j] summed over k
                        double mc = m.a[i * m.cols + k];
                        double bc = b.a[k * bcols + j];
                        //
                        temp += mc * bc;
                    }
                    // i = row # in marix c, j = col # in matrix c
                    prodMatrix.a[i * ccols + j] = temp;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return prodMatrix;
    }

    /**
     Add two matrix.

     @param m matrix to be added
     @param b matrix to be added to matrix m
     @return matrix = matrix m + matrix b
     */
    public static Matrix add(Matrix m, Matrix b) {
        // matrix c = this matrix m + matrix b
        // c[i,j] = m[i,j] + b[i,j]
        // matrix m, b, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        try {
            for (int k = 0; k < m.size; k++) {
                // add cell values
                c.a[k] = m.a[k] + b.a[k];
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }

    /**
     Subtract two matrix.

     @param m matrix to be subtracted from by matrix b
     @param b matrix to be subtract from matrix m
     @return matrix = matrix m - matrix b
     */
    public static Matrix subtract(Matrix m, Matrix b) {
        // matrix c = matrix m - matrix b
        // c[i,j] = m[i,j] - b[i,j]
        // matrix m, b, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        try {
            for (int k = 0; k < m.size; k++) {
                // subtract cell values
                c.a[k] = m.a[k] - b.a[k];
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }

    /**
     Add in place matrix, two matrices with same number of rows and columns

     @param m matrix to be updated; its cells will be replaced by adding cells from matrix b
     @param b matrix to be added to matrix m
     */
    public static void addInplace(Matrix m, Matrix b) {
        try {
            // matrix m =  matrix m + matrix b
            // add corresponding cells from each matrix
            // m[i,j] = m[i,j] + b[i,j]
            // matrix m, b have same number of rows and cols
            for (int k = 0; k < m.size; k++) {
                // add cell values, and replace m cell
                m.a[k] += b.a[k];
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }

    /**
     Subtract in place matrix, two matrix with same number of rows and columns

     @param m matrix to be updated; its cells will be replaced by subtracting b
     @param b matrix to be subtracted from matrix m
     */
    public static void subtractInplace(Matrix m, Matrix b) {
        try {
            // matrix m = matrix m - matrix b
            // m[i,j] = m[i,j] - b[i,j]
            // matrix m, b have same number of rows and cols
            for (int k = 0; k < m.size; k++) {
                // subtract cell values, and replace m cell
                m.a[k] -= b.a[k];
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    /**
     Add constant to matrix.

     @param m matrix of cells
     @param u constant to add to each cell in matrix m
     @return matrix with each cell = m cell + u
     */
    public static Matrix addConstant(Matrix m, double u) {
        // matrix c = matrix m + const
        // const added to each element of matrix a to form matrix c
        // c[i,j] = m[i,j] + const
        // matrix m, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        try {
            c.a = Arrays.stream(m.a)
                    .map(i -> i + u)
                    .toArray();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }
    /**
     Add constant to matrix.

     @param m matrix of cells
     @param u constant to add to each cell in matrix m
     @return matrix with each cell = m cell + u
     */
    public static void addConstantInPlace(Matrix m, double c) {
        // const added to each element of matrix a to form matrix c
        // m[i,j] = m[i,j] + c
        try {
            m.a = Arrays.stream(m.a)
                    .map(i -> i + c)
                    .toArray();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Multiply this matrix by constant

     @param m matrix to multiply
     @param u constant to multiply matrix
     @return new matrix um
     */
    public static Matrix mulConstant(Matrix m, double u) {
        // matrix c = matrix m * const
        // each element of matrix m multiplied by const u to form matrix c
        // c[i,j] = m[i,j] * const
        // matrix m, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        try {
            c.a = Arrays.stream(m.a)
                    .map(i -> i * u)
                    .toArray();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }

    /**
     Multiply matrix in place by constant

     @param m matrix to multiply
     @param u constant to multiply matrix m
     */
    public static void mulConstInPlace(Matrix m, double u) {
        try {
            // matrix m = matrix m * const
            // each element of matrix m multiplied by const u to replace matrix m
            // m[i,j] = m[i,j] * const
            m.a = Arrays.stream(m.a)
                    .map(mij -> mij * u)
                    .toArray();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Gets matrix c = mx + b.
     Matrix c and m are rectangular.
     Matrix x is one column. Matrix b is one column.

     @param m rectangular matrix with rows to multiply
     @param x one column matrix x
     @param b one column matrix b
     @return rectangular matrix c = mx + b
     */
    public static Matrix aXplusB(Matrix m, Matrix x, Matrix b) {
        Matrix c = null;
        try {
            // matrix c = matrix m * matrix column x + matrix column b
            // c[i,j] = m[i,j] * x[j] + b[i]
            // matrix m, c have same number of rows and cols
            // matrix x is 1 col, with # rows = # cols in matrix m
            // matrix b is 1 col, with # rows = # rows in matrix m
            //    m.checkNaN();
            //    x.checkNaN();
      //      b.checkNaN("aXplusB b");
            c = mult(m, x);
            //   c.checkNaN();
            //   b.checkNaN();
            addInplace(c, b);
        //    c.checkNaN("aXplusB c");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }
    /**
     Normalizes matrix z with mean and std dev

     @param z matrix to normalize
     @param mean average of z
     @param stdDev standard deviaton of z
     @return normalized z matrix
     */
    public static Matrix layerNorm(Matrix z, double mean, double stdDev) {
        Matrix norm = null;
        try {
            // matrix norm = (matrix z - mean) / (stdDev)
            // norm[i,j] = (z[i,j] - mean) / (stdDev)
            norm = addConstant(z, -mean);
            mulConstInPlace(norm, 1.0/stdDev);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return norm;
    }

    /**
     Multiply two matrix cell by cell, not matrix multiplication
     c[i,j] = m[i,j]*b[i,j] Hadamard matrix

     @param m matrix with cells to multiply
     @param b matrix with cells to multiply
     @return the new product matrix (Hadamard matrix)
     */
    public static Matrix cellMult(Matrix m, Matrix b) {
        // not matrix multiply, but only same cell multiplication
        // matrix c = this matrix a * matrix b
        // c[i,j] = a[i,j]*b[i,j]
        // matrix a, b, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        try {
            for (int k = 0; k < m.size; k++) {
                /*
                if (Double.isNaN(m.a[k]) || Double.isInfinite(m.a[k])) {
                    LOG.info("m.a[k] : " + m.a[k] + ", b.a[k]: " + b.a[k] + ", k: " + k);
                    throw new RuntimeException("m.a[k] is NaN or Inf");
                }
                if (Double.isNaN(b.a[k]) || Double.isInfinite(b.a[k])) {
                    LOG.info("m.a[k] : " + m.a[k] + ", b.a[k]: " + b.a[k] + ", k: " + k);
                    throw new RuntimeException("b.a[k] is NaN or Inf");
                }

                 */
                double mc = m.a[k];
                double bc = b.a[k];
                /*
                if (Math.abs(mc) > Hi_LIMIT) {
                    //  LOG.info("temp: " + temp);
                    mc = Math.signum(mc) * Hi_LIMIT;
                }
                if (Math.abs(bc) > Hi_LIMIT) {
                    //  LOG.info("temp: " + temp);
                    bc = Math.signum(bc) * Hi_LIMIT;
                }


                 */
                // where k = i*cols + j
                c.a[k] = mc*bc;
                /*
                if (Math.abs(c.a[k]) > Hi_LIMIT) {
                //    LOG.info("m.a[k] : " + m.a[k] + ", b.a[k]: " + b.a[k] + ", k: " + k);
                    c.a[k] = Math.signum(c.a[k]) * Hi_LIMIT;
                }

                 */
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }

    /**
     Check matrix cells for Infinite or NaN value, and reset cell value

     @param m matrix to check cells
     @return the modified matrix
     */
    public static boolean fixNaNinPlace(Matrix m) {
        boolean wasModified = false;
        try {
            for (int k = 0; k < m.size; k++) {
                if (Double.isNaN(m.a[k]) || Double.isInfinite(m.a[k])) {
                    LOG.info("m.a[k] : " + m.a[k] + ", k: " + k);
                    m.a[k] = Math.signum(m.a[k]) * Hi_LIMIT;
                    wasModified = true;
                    //  throw new RuntimeException("m.a[k] is NaN or Inf");
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return wasModified;
    }

    /**
     Sum matrix cells.

     @param m matrix with cells to sum
     @return the sum of matrix cells
     */
    public static double sumCells(Matrix m) {
        // convert array stream to sum
        double sum = DoubleStream.of(m.a)
                .sum();
        return sum;
    }

    /**
     Sum column cells.

     @param m    matrix with a column to sum
     @param colK index of column to sum over
     @return the sum of column cells
     */
    public static double sumCol(Matrix m, int colK) {
        double sum = 0;
        for (int i = 0; i < m.rows; i++) {
            sum += m.a[i * m.cols + colK];
        }
        return sum;
    }

    /**
     Sum all columns in this matrix and save in new matrix with one row

     @param m matrix with columns to sum
     @return the new row matrix
     */
    public static Matrix sumAllCol(Matrix m) {
        Matrix c = new Matrix(1, m.cols);
        try {
            for (int j = 0; j < m.cols; j++) {
                // j = col # in matrix m
                double colSum = 0;
                for (int i = 0; i < m.rows; i++) {
                    // i = row # in matrix m
                    colSum += m.a[i * m.cols + j];
                }
                // c has 1 row i=0, col j contains sum
                c.a[j] = colSum;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return c;
    }

    /**
     Find cell maximum value

     @param m matrix with cells to check for maximum
     @return the cell maximum value
     */
    public static double maxCell(Matrix m) {
        // double array to Doublestream to OptionalDouble max to double
        double maxVal = DoubleStream.of(m.a)
                .max()
                .getAsDouble();
        return maxVal;
    }

    /**
     Find cell maximum value

     @param m matrix with cells to check for maximum
     @return the cell maximum value
     */
    public static double minCell(Matrix m) {
        // double array to Doublestream to OptionalDouble max to double
        double minVal = DoubleStream.of(m.a)
                .min()
                .getAsDouble();
        return minVal;
    }

    /**
     Find cell maximum value

     @param m matrix with cells to check for maximum
     @return the cell maximum value
     */
    public static double maxAbsCell(Matrix m) {
        double maxVal = 0;
        try {
            // double array to Doublestream to OptionalDouble max to double
            maxVal = DoubleStream.of(m.a)
                    .map(x -> Math.abs(x))
                    .max()
                    .getAsDouble();
            if (maxVal > Hi_LIMIT) {
                maxVal = Hi_LIMIT;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return maxVal;
    }

    /**
     Find cell maximum value

     @param m matrix with cells to check for maximum
     @return the cell maximum value
     */
    public static double minAbsCell(Matrix m) {
        double minVal = 0;
        try {
            // double array to Doublestream to OptionalDouble max to double
            minVal = DoubleStream.of(m.a)
                    .map(x -> Math.abs(x))
                    .min()
                    .getAsDouble();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return minVal;
    }
    /**
     Find cell average value

     @param m matrix with cells to check for average
     @return the cell average value
     */
    public static double aveCell(Matrix m) {
        double aveVal = 0;
        try {
            // double array to Doublestream to OptionalDouble average to double
            aveVal = DoubleStream.of(m.a)
                    .average()
                    .getAsDouble();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return aveVal;
    }

    /**
     Normalize the cell values -1 < cell < 1.0

     @param m matrix with cells to check for maximum
     @return matrix with normalized values (-1.0 < cell < 1.0)
     */
    public static Matrix normalize(Matrix m) {
        Matrix norm = null;
        try {
            double maxVal = maxAbsCell(m);
            double inv = 1.0 / maxVal;
            norm = mulConstant(m, inv);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return norm;
    }

    /**
     Normalize the cell values -1 < cell < 1.0

     @param m matrix with cells to check for maximum
     @return matrix with normalized values (-1.0 < cell < 1.0)
     */
    public static void normalizeInPlace(Matrix m) {
        try {
            double maxVal = maxAbsCell(m);
            double inv = 1.0 / maxVal;
            mulConstInPlace(m, inv);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Normalize the cell values to: -norm < cell < norm

     @param m    matrix with cells to check for maximum
     @param norm the normalization factor
     @return matrix with normalized values (-norm < cell < norm)
     */
    public static void normalizeInPlaceToNorm(Matrix m, double norm) {
        try {
            double maxVal = maxAbsCell(m);
            double inv = norm / maxVal;
            mulConstInPlace(m, inv);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Normalize the cell values to: -norm < cell < norm

     @param m    matrix with cells to check for maximum
     @param norm the normalization factor
     @return matrix with normalized values (-norm < cell < norm)
     */
    public static void normalizeToNormIf(Matrix m, double norm) {
        try {
            double maxVal = maxAbsCell(m);
            if (maxVal > MTX.Hi_LIMIT) {
                double inv = norm / maxVal;
                mulConstInPlace(m, inv);
            } else if (maxVal < MTX.Low_LIMIT) {
                maxVal = 1.0;
                double inv = norm / maxVal;
                mulConstInPlace(m, inv);
            } else{
                mulConstInPlace(m, norm);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Normalize the cell values to: -norm < cell < norm

     @param m    matrix with cells to check for maximum
     @param norm the normalization factor
     @return matrix with normalized values (-norm < cell < norm)
     */
    public static void normalizeToNormIf(Matrix m, double upperLimit, double norm) {
        try {
            double maxVal = maxAbsCell(m);
            if (maxVal > upperLimit) {
                double inv = norm / maxVal;
                mulConstInPlace(m, inv);
            } else{
                mulConstInPlace(m, norm);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Normalize a list of matrix with the cell values: -norm < cell < norm

     @param mList matrix list with cells to normalize
     */
    public static void normalizeList(List<Matrix> mList, double norm) {
        try {
            double globalMax = -100000;
            int numItems = mList.size();
            // find global max over all matrix in list
            for (int i = 0; i < numItems; i++) {
                // next matrix
                Matrix m = mList.get(i);
                boolean wasModified = fixNaNinPlace(m);
                double maxCell = maxAbsCell(m);
                globalMax = Math.max(globalMax, maxCell);
            }
            double inv = norm / globalMax;
       //     LOG.log(Level.INFO, "globalMax: " + globalMax + ", inv: " + inv);
            // now normalize each matrix in list with globalMax
            for (int i = 0; i < numItems; i++) {
                // next matrix
                Matrix m = mList.get(i);
                mulConstInPlace(m, inv);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Normalize a list of (list of matrix) with the cell values: -norm < cell < norm

     @param mList list of (list of matrix) with cells to normalize
     */
    public static void normalizeListOfList(List<List<Matrix>> mList, double norm) {
        try {
            double globalMax = -100000;
            int numRows = mList.size();
            int numCols = 0;
            // find global max over all matrix in list
            for (int i = 0; i < numRows; i++) {
                // next matrix list
                List<Matrix> colList = mList.get(i);
                numCols = colList.size();
                for (int j = 0; j < numCols; j++) {
                    Matrix m = colList.get(j);
                    boolean wasModified = fixNaNinPlace(m);
                    double maxCell = maxAbsCell(m);
                    globalMax = Math.max(globalMax, maxCell);
                }
            }
            double inv = norm / globalMax;
       //     LOG.log(Level.INFO, "globalMax: " + globalMax + ", inv: " + inv);
            // now normalize each matrix in list with globalMax
            for (int i = 0; i < numRows; i++) {
                List<Matrix> colList = mList.get(i);
                for (int j = 0; j < numCols; j++) {
                    // next matrix
                    Matrix m = colList.get(j);
                    mulConstInPlace(m, inv);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Convolve matrix m with matrix filter f

     @param m matrix to be convolved with f
     @param f matrix filter
     @return the new comvolved matrix
     */
    public static Matrix convolve(Matrix m, Matrix f) {
        Matrix convoMatrix = null;
        try {
            if (m.size < f.size) {
                String msg = "Cannot convolve: matrix m smaller than matrix f";
                LOG.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            // matrix c = matrix m convolved with matrix f
            // matrix f must be smaller than matrix m
            // matrix c: number of rows = m #rows - f #rows + 1
            // matrix c: number of cols = m #cols - f # cols + 1
            int crows = m.rows - f.rows + 1;
            int ccols = m.cols - f.cols + 1;
            // convolution matrix c
            convoMatrix = new Matrix(crows, ccols);
            int rowStart = 0;
            int rowEnd = rowStart + f.rows;
            int colStart = 0;
            int colEnd = colStart + f.cols;
            for (int i = 0; i < crows; i++) {
                // i = row # in this matrix c
                for (int j = 0; j < ccols; j++) {
                    // j = col # in matrix c
                    // get sub-matrix of matrix m same size as matrix f
                    Matrix subA = subMatrix(m, rowStart, rowEnd, colStart, colEnd);
                    // matrix subA is same size as matrix f
                    // cell multiply sub-matrix A with matrix f; u = subA[h,k] * f[h,k]
                    Matrix u = cellMult(subA, f);
                    // sum all cells in matrix u, and save in matrix c
                    convoMatrix.a[i * ccols + j] = sumCells(u);
                    //
                    // slide to next col of matrix m
                    colStart++;
                    colEnd = colStart + f.cols;
                }
                // slide to next row of matrix m
                colStart = 0;
                colEnd = colStart + f.cols;
                rowStart++;
                rowEnd = rowStart + f.rows;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return convoMatrix;
    }
    /**
     Unfold matrix m for convolving with filter f

     @param m matrix to be unfolded
     @param frows number of filter rows
     @param fcols number of filter columns
     @return the unfolded matrix
     */
    public static Matrix unfold(Matrix m, int frows, int fcols) {
        Matrix unfoldM = null;
        try {
            int fsize = frows*fcols;
            if (m.size < fsize) {
                String msg = "Cannot convolve: matrix m smaller than matrix f";
                LOG.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            // matrix c = unfolded matrix
            // matrix f must be smaller than matrix m
            // matrix c: number of rows = m #rows - f #rows + 1
            // matrix c: number of cols = m #cols - f # cols + 1
            int crows = m.rows - frows + 1;
            int ccols = m.cols - fcols + 1;
            List<Matrix> unfoldList = new ArrayList<>();
            int rowStart = 0;
            int rowEnd = rowStart + frows;
            int colStart = 0;
            int colEnd = colStart + fcols;
            for (int i = 0; i < crows; i++) {
                // i = row # in this matrix c
                for (int j = 0; j < ccols; j++) {
                    // j = col # in matrix c
                    // get sub-matrix of matrix m same size as matrix f
                    Matrix subA = subMatrix(m, rowStart, rowEnd, colStart, colEnd);
                    //
                    unfoldList.add(subA);
                    //
                    // slide to next col of matrix m
                    colStart++;
                    colEnd = colStart + fcols;
                }
                // slide to next row of matrix m
                colStart = 0;
                colEnd = colStart + fcols;
                rowStart++;
                rowEnd = rowStart + frows;
            }
            unfoldM = MTX.listToSingleCol(unfoldList);
            // reset rows cols in unfold to allow matrix multiply by filter
            unfoldM.cols = fsize;
            unfoldM.rows = crows*ccols;
            unfoldM.size = unfoldM.cols * unfoldM.rows;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return unfoldM;
    }

    /**
     Add zero padding to matrix around the edges

     @return matrix padded copy
     */
    public static Matrix copyAndPad(Matrix m, int padSize) {
        Matrix padCopy = null;
        try {
            int prows = m.rows + 2 * padSize;
            int pcols = m.cols + 2 * padSize;
            padCopy = new Matrix(prows, pcols);
            int subRowI = 0;
            int subColJ = 0;
            // add padding zero cells to matrix padCopy
            // in rows containing this sub matrix
            for (int i = padSize; i < padSize + m.rows; i++) {
                // i = row # in matrix padCopy
                subColJ = 0;
                for (int j = padSize; j < padSize + m.cols; j++) {
                    // j = col # in matrix padCopy
                    // copy cell from sub matrix to padCopy
                    padCopy.a[i * pcols + j] = m.a[subRowI * m.cols + subColJ];
                    subColJ++;
                }
                subRowI++;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return padCopy;
    }


    /**
     Create Max pool matrix

     @param poolRows the pool rows size
     @param poolCols the pool columns size
     @return new matrix containing max pool
     */
    public static Matrix maxPool(Matrix m, int poolRows, int poolCols) {
        Matrix maxPoolMatrix = null;
        try {
            // matrix C: number of rows = A #rows / poolRows
            // matrix C: number of cols = A #cols / poolCols
            int crows = m.rows / poolRows;
            int ccols = m.cols / poolCols;
            maxPoolMatrix = new Matrix(crows, ccols);
            int rowStart = 0;
            int rowEnd = rowStart + poolRows;
            int colStart = 0;
            int colEnd = colStart + poolCols;
            for (int i = 0; i < crows; i++) {
                // i = row # in this matrix c
                for (int j = 0; j < ccols; j++) {
                    // j = col # in matrix C
                    // get sub-matrix of matrix A with size poolRows x poolCols
                    Matrix subA = subMatrix(m, rowStart, rowEnd, colStart, colEnd);
                    // matrix subA is poolRows x poolCols
                    // find max cell in matrix subA, and save in matrix C
                    maxPoolMatrix.a[i * ccols + j] = maxCell(subA);
                    //
                    // slide to next col of matrix A
                    colStart += poolCols;
                    colEnd = colStart + poolCols;
                }
                // slide to next row of matrix A
                colStart = 0;
                colEnd = colStart + poolCols;
                rowStart += poolRows;
                rowEnd = rowStart + poolRows;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return maxPoolMatrix;
    }

    /**
     Create Max pool index matrix

     @param m        the pool input matrix
     @param poolRows the pool rows size
     @param poolCols the pool columns size
     @return new matrix containing max pool index
     */
    public static Matrix poolIndex(Matrix m, int poolRows, int poolCols) {
        Matrix indexMatrix = null;
        try {
            int rowrem = m.rows % poolRows;
            int colrem = m.cols % poolCols;
            if (rowrem != 0 || colrem != 0) {
                String msg = "(rows % poolRows != 0) or (cols % poolCols != 0), For matrix m: " + m;
                LOG.log(Level.INFO, msg);
                throw new RuntimeException(msg);
            }
            // matrix C: number of rows = m #rows / poolRows
            // matrix C: number of cols = m #cols / poolCols
            int crows = m.rows / poolRows;
            int ccols = m.cols / poolCols;
            indexMatrix = new Matrix(crows, ccols);
            //let (h, k) = cell in indexMatrix
            //   int rowStart = 0;
            //   int rowEnd = rowStart + poolRows - 1;
            //    int colStart = 0;
            //    int colEnd = colStart + poolCols - 1;
            int h = 0; // row index of indexMatrix
            int k = 0; // col index of indexMatrix
            for (int i = 0; i < m.rows; i = i + poolRows) {
                int rowStart = i;
                int rowEnd = rowStart + poolRows;

                // i = row # in this matrix c
                for (int j = 0; j < m.cols; j = j + poolCols) {
                    int colStart = j;
                    int colEnd = colStart + poolCols;
                    // j = col # in matrix C
                    // get sub-matrix of matrix m with size poolRows x poolCols
                    Matrix subM = subMatrix(m, rowStart, rowEnd, colStart, colEnd);
                    // matrix subA is poolRows x poolCols
                    // find max cell in matrix subM, and save in matrix C
                    //   double max = maxCell(subA);
                    // find index of max cell in matrix subM
                    int subK = MathUtil.indexOfMax(subM.a);

                    // index relative to subM
                    IndexIJ subIJ = kToIJ(subM.cols, subK);
                    // find index of max cell in matrix m = ref pt in m + rel index in subM
                    int maxI = rowStart + subIJ.i();
                    int maxJ = colStart + subIJ.j();
                    int maxK = maxI * m.cols + maxJ;
                    // save max index
                    setCell(indexMatrix, h, k, maxK);
                    //
                    // next col of indexMatrix
                    k++;
                }
                //reset col of indexMatrix
                k = 0;
                // next row of indexMatrix
                h++;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return indexMatrix;
    }

    /**
     Find average of a list of matrices, and save in new matrix where
     each cell now contains the average value of all corresponding
     cells from the list.

     @param mList list of matrix
     @return average matrix
     */
    public static Matrix averageOfList(List<Matrix> mList) {
        Matrix average = null;
        try {
            int n = mList.size();
            // initialize averagematrix with sum of all matrix in list
            average = sumOfList(mList);
            // now divide sum matrix by n to get average for each cell
            mulConstInPlace(average, 1.0 / n);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return average;
    }

    /**
     Find average of single column matrix list

     @return average of matrix list
     */
    public static Matrix listAverage(List<Matrix> batch) {
        Matrix avg = null;
        try {
            // find average of batch matrices
            avg = MTX.averageOfList(batch);
            //
            // clear list for next batch
            batch.clear();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return avg;
    }
    /**
     Find average, standard deviation, etc. of all cells in matrix

     @param matrix to find stats
     @return matrix stats (mean, standard deviation, ...)
     */
    public static StatsR stats(Matrix m) {
        StatsR stats = null;
        try {
            int n = m.size;
            double sumCells = DoubleStream.of(m.a).sum();
            // average of all cells in matrix
            double mean = sumCells / n;
            // variance
            double v = DoubleStream.of(m.a)
                    .map(x -> x - mean)
                    .map(d -> d*d).sum();
            v = v / n;
            // standard deviation
            double stdDev = Math.sqrt(v);
            double aveCell = 0;
            double minCell = 0;
            double maxCell = 0;

            DoubleStream maxStream = DoubleStream.of(m.a);
            OptionalDouble maxOpt = maxStream.max();
            if (maxOpt.isPresent()) {
                maxCell = maxOpt.getAsDouble();
            }
            DoubleStream minStream = DoubleStream.of(m.a);
            OptionalDouble minOpt = minStream.min();
            if (minOpt.isPresent()) {
                minCell = minOpt.getAsDouble();
            }
            DoubleStream aveStream = DoubleStream.of(m.a);
            OptionalDouble  aveOpt = aveStream.average();
            if (aveOpt.isPresent()) {
                aveCell = aveOpt.getAsDouble();
            }
            stats = new StatsR(mean, stdDev, sumCells, aveCell, minCell, maxCell);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return stats;
    }
    /**
     Find sum of a list of matrices and save in new matrix

     @param mList list of matrix
     @return sum matrix
     */
    public static Matrix sumOfList(List<Matrix> mList) {
        Matrix sum = null;
        try {
            // n = number of matrix in list
            int n = mList.size();
            // check first matrix for size 1
            Matrix m = mList.get(0);
            if (n == 1) {
                sum = m;
            } else {
                sum = new Matrix(m.rows, m.cols);
                for (int k = 0; k < n; k++) {
                    // next matrix from input list
                    m = mList.get(k);
                    // add matrix m to sum matrix
                    addInplace(sum, m);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return sum;
    }

    /**
     Sum over a list of list of BatchNode

     @param mList matrix list to add to sum (must be same size as sumList)
     @return sumList matrix list to save sum
     */
    public static ConvoNode  sumOfBatchList(List<ConvoNode> mList) {
        ConvoNode sumNode = new ConvoNode();
        try {
            // sizing to init sumNode
            ConvoNode nodeItem = mList.get(0);
            int nodeItemSize = nodeItem.size();
            Matrix w = nodeItem.get(0);
            // init sumNode inner matrices
                for (int j = 0; j < nodeItemSize; j++) {
                    // inner sum matrix, all zero cells
                    Matrix sum = new Matrix(w.rows, w.cols);
                    sumNode.add(sum);
                }
            //
            int batchSize = mList.size();
            // sum inner matrices over all batches
            for (int b = 0; b < batchSize; b++) {
                ConvoNode batchItem = mList.get(b);
                    int nodeSize = batchItem.size();
                    for (int i = 0; i < nodeSize; i++) {
                        Matrix ww = batchItem.get(i);
                        Matrix sumW = sumNode.get(i);
                        addInplace(sumW, w);
                    }
                }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return sumNode;
    }
    /**
     Sum over a list of list of BatchNode

     @param mList matrix list to add to sum (must be same size as sumList)
     @return sumList matrix list to save sum
     */
    public static List<ConvoNode>  sumOfBatchNestedList(List<List<ConvoNode>> mList) {
        List<ConvoNode> sumList = new ArrayList<>();
        try {
            // sizing to init sumList
            List<ConvoNode> mListItem = mList.get(0);
            int nOut = mListItem.size();
            ConvoNode nodeItem = mListItem.get(0);
            int nIn = nodeItem.size();
            Matrix w = nodeItem.get(0);
            // init sumList inner matrices
            for (int k = 0; k < nOut; k++) {
                ConvoNode sumNode = new ConvoNode();
                for (int i = 0; i < nIn; i++) {
                    // inner sum matrix, all zero cells
                    Matrix sum = new Matrix(w.rows, w.cols);
                    sumNode.add(sum);
                }
                sumList.add(sumNode);
            }
            //
            int batchSize = mList.size();
            // sum inner matrices over all batches
            for (int b = 0; b < batchSize; b++) {
                // batch index b
                List<ConvoNode> batchItem = mList.get(b);
                int kOut = batchItem.size();
                // sum over all batches
                for (int k = 0; k < kOut; k++) {
                    ConvoNode batchRow = batchItem.get(k);
                    ConvoNode sumNode = sumList.get(k);
                    int iIn = batchRow.size();
                    for (int i = 0; i < iIn; i++) {
                        Matrix wt = batchRow.get(i);
                        Matrix sumW = sumNode.get(i);
                        // modify sumW matrix
                        addInplace(sumW, wt);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return sumList;
    }

    /**
     Compare matrix elements to a constant value

     @param b matrix to copy
     @param c constant to compare
     */
    public static boolean equalsConst(Matrix b, double c) {
        boolean result = false;
        int n = b.size;
        for (int k = 0; k < n; k++) {
            if (b.a[k] != c) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     Derivative of multi-class cross entropy loss function, where only
     one class can be correct (also called neg log likelihood)
     Cross entropy loss: -sum[ yi * log(pi)], sum over classes i
     where yi = actual y positive value
     pi = predicted y probability, positive value
     dLdPi = -yi/pi column matriz
     Nore: pi must not equal 0

     @param actualY col matrix of actual values
     @param p       col matrix of predicted values
     @return derivative of cross entropy loss with respect to predicted values
     */
    public static Matrix dLossdP(Matrix actualY, Matrix p) {
        // matrix y, p, dLdP have same number of rows and cols
        Matrix dLdP = new Matrix(actualY.rows, actualY.cols);
        try {
            for (int k = 0; k < actualY.size; k++) {
                // where k = i*cols + j
                if (p.a[k] < Loss_Low_LIMIT) {
                    // pi ~ 0,
                    dLdP.a[k] = -actualY.a[k]/Loss_Low_LIMIT;
                } else {
                    // normal case
                    dLdP.a[k] = -actualY.a[k] / p.a[k];
                }
                if (Double.isNaN(dLdP.a[k])) {
                    dLdP.a[k] = -1.0;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dLdP;
    }

} // end class