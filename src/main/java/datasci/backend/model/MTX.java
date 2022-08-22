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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Matrix in one-D row format
 */
public class MTX {

    private static final Logger LOG = Logger.getLogger(MTX.class.getName());

    public static double Hi_LIMIT = 1.0E6;
    public static double Low_LIMIT = 1.0E-9;
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
     * Create a new Matrix.
     *
     * @param rows the rows
     * @param cols the cols
     * @return new matrix, initialized with zero values
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
     * Create a new Matrix.
     *
     * @param rows the number of rows
     * @param cols the number of cols
     * @param a    array to initialize new matrix; must have size = rows*cols
     * @return new matrix
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
     * Copy matrix to new matrix
     *
     * @param b matrix to copy
     * @return new matrix = copy of matrix b
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
     * Log elements of matrix
     *
     * @param b matrix to copy
     * @param n number of elements to log
     */
    public static void logMatrx(String name, Matrix b, int n) {
        LOG.info(name);
        for (int k = 0; k < n; k++) {
            LOG.info("k: " + k + ", a[k]: " + b.a[k]);
        }
    }

    /**
     * Create i,j index from array index k,
     * where i = row index; j = column index
     * Array index k = i*cols + j
     *
     * @param cols numder of columns of some matrix
     * @param k    array index
     * @return i, j index record
     */
    public static IndexIJ kToIJ(int cols, int k) {
        // division of integers with truncation
        int i = k / cols;
        // remainder
        int j = k % cols;
        return new IndexIJ(i, j);
    }

    /**
     * Initialize matrix cells to specified value.
     *
     * @param m   matrix to Initialize
     * @param val specified value
     */
    public static void initValue(Matrix m, double val) {
        for (int k = 0; k < m.size; k++) {
            // initialize each matrix cell to single value
            m.a[k] = val;
        }
    }

    /**
     * Create square identity matrix
     *
     * @param rows number of rows
     * @param cols number of columns (must equal number of rows)
     * @return square identity matrix
     */
    public static Matrix createIdentMatrix(int rows, int cols) {
        Matrix m = new Matrix(rows, cols);
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
     * Compare two matrix, cell by cell
     *
     * @param m matrix to compare
     * @param b matrix to compare
     * @return true if matrix m = matrix b, otherwise false
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
     * Set matrix cells from 2D array
     *
     * @param m matrix to set
     * @param b 2D array to be converted to flat matrix array
     */
    public static void setFrom2D(Matrix m, double[][] b) {
        // set matrix a = matrix b
        // convert 2D array to 1D array
        m.a = Stream.of(b)
                .flatMapToDouble(DoubleStream::of)
                .toArray();
    }

    /**
     * Create column matrix
     *
     * @param b 1D array to be converted to flat column matrix
     * @return column matrix
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
     * Create row matrix
     *
     * @param b 1D array to be converted to flat row matrix
     * @return row matrix
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
     * Get stream of this matrix
     *
     * @param m matrix to stream
     * @return stream of this matrix
     */
    public static DoubleStream getStreamA(Matrix m) {

        return DoubleStream.of(m.a);
    }

    /**
     * Concatenate matrix m with matrix b by rows
     * Matrix b must have same number of cols as matrix m
     * Result matrix #rows = this #rows + b #rows
     * Result matrix #cols = this #cols
     *
     * @param m matrix to concatenate
     * @param b matrix to concatenate
     * @return Concatenation of this matrix with matrix b by rows
     */
    public static Matrix concat(Matrix m, Matrix b) {
        Matrix c = new Matrix();
        c.rows = m.rows + b.rows;
        c.cols = m.cols;
        c.a = DoubleStream.concat(Arrays.stream(m.a), Arrays.stream(b.a)).toArray();
        return c;
    }

    /**
     * Sets cell value at given row index and column index
     *
     * @param m    matrix of cells
     * @param rowI row index i
     * @param colJ column index j
     * @param val  cell value to set
     */
    public static void setCell(Matrix m, int rowI, int colJ, double val) {
        // row formatted array: index = rowI * cols + colJ
        m.a[rowI * m.cols + colJ] = val;
    }

    /**
     * Sets cell value at given array index
     *
     * @param m     matrix of cells
     * @param cellK array index k, where k = i*cols + j
     * @param val   cell value to set
     */
    public static void setCell(Matrix m, int cellK, double val) {
        // row formatted array: index cellK = rowI * cols + colJ
        m.a[cellK] = val;
    }

    /**
     * Gets cell value by row index and column index
     *
     * @param m    matrix of cells
     * @param rowI the row i
     * @param colJ the col j
     * @return the cell value
     */
    public static double getCell(Matrix m, int rowI, int colJ) {
        return m.a[rowI * m.cols + colJ];
    }

    /**
     * Gets cell value by array index
     *
     * @param m     matrix of cells
     * @param cellK array index k, where k = i*cols + j
     * @return the cell value
     */
    public static double getCell(Matrix m, int cellK) {
        return m.a[cellK];
    }

    /**
     * Update one matrix cell value by a constant
     *
     * @param m     matrix of cells to be incremented
     * @param rowI  cell row i
     * @param colJ  cell col j
     * @param delta increment to add to specified cell
     */
    public static void updateCell(Matrix m, int rowI, int colJ, double delta) {
        // row formatted array: index = rowI * cols + colJ
        m.a[rowI * m.cols + colJ] += delta;
    }

    /**
     * Get row array from matrix
     *
     * @param m    matrix with row to retrieve
     * @param rowI the row i
     * @return row array from matrix
     */
    public static double[] getRow(Matrix m, int rowI) {
        double[] row = new double[m.cols];
        // copy all colums in single row
        row = Arrays.copyOfRange(m.a, rowI * m.cols, rowI * m.cols + m.cols);
        return row;
    }

    /**
     * Get column array from matrix
     *
     * @param m    matrix with column to retrieve
     * @param colJ the col j
     * @return column array from matrix
     */
    public static double[] getCol(Matrix m, int colJ) {
        double[] col = new double[m.rows];
        for (int i = 0; i < m.rows; i++) {
            col[i] = m.a[i * m.cols + colJ];
        }
        return col;
    }

    /**
     * Gets sub matrix.
     *
     * @param m        matrix with submatrix
     * @param rowStart the row start in matrix m
     * @param rowEnd   the row end in matrix m
     * @param colStart the col start in matrix m
     * @param colEnd   the col end in matrix m
     * @return sub matrix
     */
    public static Matrix subMatrix(Matrix m, int rowStart, int rowEnd, int colStart, int colEnd) {
        int subRows = rowEnd - rowStart + 1;
        int subCols = colEnd - colStart + 1;
        //   LOG.fine("subRows: " + subRows + ", subCols: " + subCols);
        Matrix subMatrix = new Matrix(subRows, subCols);
        //    LOG.fine("matrix a len: " + a.length + ", sub size: " + subMatrix.size);
        // copy cols from a matrix row to rows of subMatrix
        int subI = 0;
        int subJ = 0;
        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = colStart; j <= colEnd; j++) {
                // get specified cell(i, j) from matrix m to form sub matrix cell(subI, subJ)
                subMatrix.a[subI * subCols + subJ] = getCell(m, i, j);
                if (Double.isNaN(subMatrix.a[subI * subCols + subJ])) {
                    LOG.info("subMatrix.a[subI * subCols + subJ] : " + subMatrix.a[subI * subCols + subJ] + ", i: " + i + ", j: " + j);
                    throw new RuntimeException("subMatrix is NaN");
                }
                // next col of sub matrix
                subJ++;
            }
            subJ = 0;
            // next row of sub matrix
            subI++;
        }
        return subMatrix;
    }

    /**
     * Split this matrix into list of n submatrix
     * Each submatrix will have same number of columns as this matrix
     *
     * @param m matrix to be split
     * @param n number of submatrix
     * @return n sub matrix in list
     */
    public static List<Matrix> splitMatrix(Matrix m, int n) {
        List<Matrix> subMatrixList = new ArrayList<>();
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
        return subMatrixList;
    }

    /**
     * Transpose matrix.
     *
     * @param m matrix to be transposed
     * @return transposed matrix
     */
    public static Matrix transpose(Matrix m) {
        // number of rows and colums transposed
        // if matrix m is 3 rows by 2 cols
        // transpose will be 2 rows by 3 cols
        Matrix trans = new Matrix();
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
        return trans;
    }

    /**
     * Rotate matrix: reverse elements
     *
     * @param m matrix to be rotated
     * @return rotated matrix
     */
    public static Matrix rotate(Matrix m) {
        Matrix rotated = copy(m);
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
        return rotated;
    }

    /**
     * Create single column matrix from list of matrix
     *
     * @param mList list of matrix
     * @return single column matrix
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
     * Concatenate in place matrix m with matrix b by rows
     * Matrix b must have same number of cols as matrix m
     * Result matrix #rows = m #rows + b #rows
     * Result matrix #cols = m #cols
     *
     * @param m input matrix to be concatenated, changed by operation
     * @param b matrix to be concatenated to matrix m
     */
    public static void concatInplace(Matrix m, Matrix b) {
        // replace array in matrix m
        m.a = DoubleStream.concat(Arrays.stream(m.a), Arrays.stream(b.a)).toArray();
        // replace matrix m sizes
        m.rows = m.rows + b.rows;
        m.size = m.rows * m.cols;
    }

    /**
     * Find transpose of single column matrix
     *
     * @param m column matrix to be transposed
     * @return single row matrix: the transpose of single column matrix
     */
    public static Matrix colToRow(Matrix m) {
        Matrix c = new Matrix();
        c.rows = 1;
        c.cols = m.size;
        c.size = m.size;
        // copy all matrix cells
        c.a = Arrays.copyOf(m.a, m.size);
        return c;
    }

    /**
     * Find transpose of single row matrix
     *
     * @param m row matrix to be transposed
     * @return single column matrix: the transpose of single row matrix
     */
    public static Matrix rowToCol(Matrix m) {
        Matrix c = new Matrix();
        c.rows = m.size;
        c.cols = 1;
        c.size = m.size;
        // copy all matrix cells
        c.a = Arrays.copyOf(m.a, m.size);
        return c;
    }

    /**
     * Multiply two matrix.
     * Number of columns in matrix m = number of rows in matrix b
     * Product matrix:  #rows = m #rows, #cols = b #cols
     *
     * @param m matrix to be multiplied
     * @param b matrix to be multiplied
     * @return product matrix = m * b
     */
    public static Matrix mult(Matrix m, Matrix b) {
        Matrix prodMatrix = null;
        try {
            // matrix c = this matrix m * matrix b
            // c[i,j] = m[i,k]*b[k,j] summed over k
            // c #rows = m #rows, c #cols = b #cols
            // bcols = number of cols in matrix b
            m.checkNaN("mult m");
            b.checkNaN("mult b");
            int bcols = b.cols;
            // crows = number of rows in matrix c
            int crows = m.rows;
            // ccols = number of cols in matrix c
            int ccols = bcols;
            //    LOG.fine("mult, bcols: " + bcols + ", crows: " + crows + ", ccols: " + ccols + ", cols: " + cols);
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
                        if(Math.abs(mc) > Hi_LIMIT){
                            //  LOG.info("temp: " + temp);
                            mc = Math.signum(mc)*Hi_LIMIT;
                        }
                        if(Math.abs(bc) > Hi_LIMIT){
                            //  LOG.info("temp: " + temp);
                            bc = Math.signum(bc)*Hi_LIMIT;
                        }

                        temp += mc * bc;
                        if(Math.abs(temp) > Hi_LIMIT){
                          //  LOG.info("temp: " + temp);
                            temp = Math.signum(temp)*Hi_LIMIT;
                        }
                        if(Math.abs(temp) < Low_LIMIT){
                            //  LOG.info("temp: " + temp);
                            temp = 0.0;
                        }

                        if (Double.isNaN(temp)) {
                            LOG.info("i: " + i + ", j: " + j + ", k: " + k);
                            LOG.info("m size: " + m.size + ", b size: " + b.size);
                            int r = i * m.cols + k;
                            int s = k * bcols + j;
                            LOG.info("m.a[i * m.cols + k] : " + m.a[i * m.cols + k] + ", r: " + r + ", s: " + s);
                            LOG.info("b.a[k * bcols + j] : " + b.a[k * bcols + j]);
                            throw new RuntimeException("temp is NaN");
                        }
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
     * Add two matrix.
     *
     * @param m matrix to be added
     * @param b matrix to be added to matrix m
     * @return matrix = matrix m + matrix b
     */
    public static Matrix add(Matrix m, Matrix b) {
        // matrix c = this matrix m + matrix b
        // c[i,j] = m[i,j] + b[i,j]
        // matrix m, b, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        for (int k = 0; k < m.size; k++) {
            // add cell values
            c.a[k] = m.a[k] + b.a[k];
        }
        return c;
    }

    /**
     * Subtract two matrix.
     *
     * @param m matrix to be subtracted from by matrix b
     * @param b matrix to be subtract from matrix m
     * @return matrix = matrix m - matrix b
     */
    public static Matrix subtract(Matrix m, Matrix b) {
        // matrix c = matrix m - matrix b
        // c[i,j] = m[i,j] - b[i,j]
        // matrix m, b, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        for (int k = 0; k < m.size; k++) {
            // subtract cell values
            c.a[k] = m.a[k] - b.a[k];
        }
        return c;
    }

    /**
     * Add in place matrix, two matrices with same number of rows and columns
     *
     * @param m matrix to be updated; its cells will be replaced by adding cells from matrix b
     * @param b matrix to be added to matrix m
     */
    public static void addInplace(Matrix m, Matrix b) {
        // matrix m =  matrix m + matrix b
        // add corresponding cells from each matrix
        // m[i,j] = m[i,j] + b[i,j]
        // matrix m, b have same number of rows and cols
        for (int k = 0; k < m.size; k++) {
            // add cell values, and replace m cell
            m.a[k] += b.a[k];
        }
    }

    /**
     * Subtract in place matrix, two matrix with same number of rows and columns
     *
     * @param m matrix to be updated; its cells will be replaced by subtracting b
     * @param b matrix to be subtracted from matrix m
     */
    public static void subtractInplace(Matrix m, Matrix b) {
        // matrix m = matrix m - matrix b
        // m[i,j] = m[i,j] - b[i,j]
        // matrix m, b have same number of rows and cols
        for (int k = 0; k < m.size; k++) {
            // subtract cell values, and replace m cell
            m.a[k] -= b.a[k];
        }
    }


    /**
     * Add constant to matrix.
     *
     * @param m matrix of cells
     * @param u constant to add to each cell in matrix m
     * @return matrix with each cell = m cell + u
     */
    public static Matrix addConstant(Matrix m, double u) {
        // matrix c = matrix m + const
        // const added to each element of matrix a to form matrix c
        // c[i,j] = m[i,j] + const
        // matrix m, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        c.a = Arrays.stream(m.a)
                .map(i -> i + u)
                .toArray();
        return c;
    }

    /**
     * Multiply this matrix by constant
     *
     * @param m matrix to multiply
     * @param u constant to multiply matrix
     * @return new matrix um
     */
    public static Matrix mulConstant(Matrix m, double u) {
        // matrix c = matrix m * const
        // each element of matrix m multiplied by const u to form matrix c
        // c[i,j] = m[i,j] * const
        // matrix m, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        c.a = Arrays.stream(m.a)
                .map(i -> i * u)
                .toArray();
        return c;
    }
    /**
     * Multiply matrix in place by constant
     *
     * @param m matrix to multiply
     * @param u constant to multiply matrix m
     */
    public static void mulConstInPlace(Matrix m, double u) {
        // matrix m = matrix m * const
        // each element of matrix m multiplied by const u to replace matrix m
        // m[i,j] = m[i,j] * const
        m.a = Arrays.stream(m.a)
                .map(i -> i * u)
                .toArray();
    }

    /**
     * Gets matrix c = mx + b.
     * Matrix c and m are rectangular.
     * Matrix x is one column. Matrix b is one column.
     *
     * @param m rectangular matrix with rows to multiply
     * @param x one column matrix x
     * @param b one column matrix b
     * @return rectangular matrix c = mx + b
     */
    public static Matrix aXplusB(Matrix m, Matrix x, Matrix b) {
        // matrix c = matrix m * matrix column x + matrix column b
        // c[i,j] = m[i,j] * x[j] + b[i]
        // matrix m, c have same number of rows and cols
        // matrix x is 1 col, with # rows = # cols in matrix m
        // matrix b is 1 col, with # rows = # rows in matrix m
        //    m.checkNaN();
        //    x.checkNaN();
        b.checkNaN("aXplusB b");
        Matrix c = mult(m, x);
        //   c.checkNaN();
        //   b.checkNaN();
        addInplace(c, b);
        c.checkNaN("aXplusB c");
        return c;
    }

    /**
     * Multiply two matrix cell by cell, not matrix multiplication
     * c[i,j] = m[i,j]*b[i,j] Hadamard matrix
     *
     * @param m matrix with cells to multiply
     * @param b matrix with cells to multiply
     * @return the new product matrix (Hadamard matrix)
     */
    public static Matrix cellMult(Matrix m, Matrix b) {
        // not matrix multiply, but only same cell multiplication
        // matrix c = this matrix a * matrix b
        // c[i,j] = a[i,j]*b[i,j]
        // matrix a, b, c have same number of rows and cols
        Matrix c = new Matrix(m.rows, m.cols);
        for (int k = 0; k < m.size; k++) {
            // where k = i*cols + j
            c.a[k] = m.a[k] * b.a[k];
            if(Double.isNaN(c.a[k])){
                c.a[k] = Hi_LIMIT;
            }
            if (Double.isNaN(c.a[k])) {
                LOG.info("m.a[k] : " + m.a[k] + ", b.a[k]: " + b.a[k] + ", k: " + k);
                throw new RuntimeException("c.a[k] is NaN");
            }

        }
        return c;
    }

    /**
     * Sum matrix cells.
     *
     * @param m matrix with cells to sum
     * @return the sum of matrix cells
     */
    public static double sumCells(Matrix m) {
        // convert array stream to sum
        double sum = DoubleStream.of(m.a)
                .sum();
        return sum;
    }

    /**
     * Sum column cells.
     *
     * @param m    matrix with a column to sum
     * @param colK index of column to sum over
     * @return the sum of column cells
     */
    public static double sumCol(Matrix m, int colK) {
        double sum = 0;
        for (int i = 0; i < m.rows; i++) {
            sum += m.a[i * m.cols + colK];
        }
        return sum;
    }

    /**
     * Sum all columns in this matrix and save in new matrix with one row
     *
     * @param m matrix with columns to sum
     * @return the new row matrix
     */
    public static Matrix sumAllCol(Matrix m) {
        Matrix c = new Matrix(1, m.cols);
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
        return c;
    }

    /**
     * Find cell maximum value
     *
     * @param m matrix with cells to check for maximum
     * @return the cell maximum value
     */
    public static double maxCell(Matrix m) {
        // double array to Doublestream to OptionalDouble max to double
        double maxVal = DoubleStream.of(m.a)
                .max()
                .getAsDouble();
        return maxVal;
    }
    /**
     * Find cell maximum value
     *
     * @param m matrix with cells to check for maximum
     * @return the cell maximum value
     */
    public static double minCell(Matrix m) {
        // double array to Doublestream to OptionalDouble max to double
        double maxVal = DoubleStream.of(m.a)
                .min()
                .getAsDouble();
        return maxVal;
    }

    /**
     * Find cell maximum value
     *
     * @param m matrix with cells to check for maximum
     * @return the cell maximum value
     */
    public static double maxAbsCell(Matrix m) {
        // double array to Doublestream to OptionalDouble max to double
        double maxVal = DoubleStream.of(m.a)
                .map(x -> Math.abs(x))
                .max()
                .getAsDouble();
        if(maxVal > Hi_LIMIT){
            maxVal = Hi_LIMIT;
        }
        return maxVal;
    }
    /**
     * Find cell maximum value
     *
     * @param m matrix with cells to check for maximum
     * @return the cell maximum value
     */
    public static double minAbsCell(Matrix m) {
        // double array to Doublestream to OptionalDouble max to double
        double minVal = DoubleStream.of(m.a)
                .map(x -> Math.abs(x))
                .min()
                .getAsDouble();
        if(minVal < Low_LIMIT){
            minVal = 0.0;
        }
        return minVal;
    }

    /**
     * Normalize the cell values -1 < cell < 1.0
     *
     * @param m matrix with cells to check for maximum
     * @return matrix with normalized values (-1.0 < cell < 1.0)
     */
    public static Matrix normalize(Matrix m) {
        double maxVal = maxAbsCell(m);
        if(maxVal < Low_LIMIT){
            maxVal = Low_LIMIT;
        }
        double inv = 1.0 / maxVal;
        Matrix norm = mulConstant(m, inv);
        return norm;
    }
    /**
     * Normalize the cell values -1 < cell < 1.0
     *
     * @param m matrix with cells to check for maximum
     * @return matrix with normalized values (-1.0 < cell < 1.0)
     */
    public static void normalizeInPlace(Matrix m) {
        double maxVal = maxAbsCell(m);
        if(maxVal < Low_LIMIT){
            maxVal = 1.0;
        }
        double inv = 1.0 / maxVal;
        mulConstInPlace(m, inv);
    }

    /**
     * Convolve matrix m with matrix filter f
     *
     * @param m matrix to be convolved with f
     * @param f matrix filter
     * @return the new comvolved matrix
     */
    public static Matrix convolve(Matrix m, Matrix f) {
        Matrix convoMatrix = null;
        try {
            if(m.size < f.size){
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
            int rowEnd = rowStart + f.rows - 1;
            int colStart = 0;
            int colEnd = colStart + f.cols - 1;
            //     LOG.fine("convolve, f" + f);
            //     LOG.fine("convolve, rowStart: " + rowStart + ", rowEnd: " + rowEnd);
            //     LOG.fine("convolve, colStart: " + colStart + ", colEnd: " + colEnd);
            //     LOG.fine("convolve, crows: " + crows + ", ccols: " + ccols);
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
                    colEnd = colStart + f.cols - 1;
                }
                // slide to next row of matrix m
                colStart = 0;
                colEnd = colStart + f.cols - 1;
                rowStart++;
                rowEnd = rowStart + f.rows - 1;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return convoMatrix;
    }

    /**
     * Add zero padding to matrix around the edges
     *
     * @return matrix padded copy
     */
    public static Matrix copyAndPad(Matrix m, int padSize) {
        Matrix padCopy = null;
        try {
            int prows = m.rows + 2 * padSize;
            int pcols = m.cols + 2 * padSize;
            padCopy = new Matrix(prows, pcols);
            int subRowI = 0;
            int subColJ = 0;
            LOG.fine("padCopy : " + padCopy);
            // add padding zero cells to matrix padCopy
            // in rows containing this sub matrix
            for (int i = padSize; i < padSize + m.rows; i++) {
                // i = row # in matrix padCopy
                subColJ = 0;
                for (int j = padSize; j < padSize + m.cols; j++) {
                    // j = col # in matrix padCopy
                    LOG.finer("i : " + i + ", j: " + j + ",subRowI: " + subRowI + ", subColJ: " + subColJ);
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
     * Create Max pool matrix
     *
     * @param poolRows the pool rows size
     * @param poolCols the pool columns size
     * @return new matrix containing max pool
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
            int rowEnd = rowStart + poolRows - 1;
            int colStart = 0;
            int colEnd = colStart + poolCols - 1;
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
                    colEnd = colStart + poolCols - 1;
                }
                // slide to next row of matrix A
                colStart = 0;
                colEnd = colStart + poolCols - 1;
                rowStart += poolRows;
                rowEnd = rowStart + poolRows - 1;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return maxPoolMatrix;
    }

    /**
     * Create Max pool index matrix
     *
     * @param m the pool input matrix
     * @param poolRows the pool rows size
     * @param poolCols the pool columns size
     * @return new matrix containing max pool index
     */
    public static Matrix poolIndex(Matrix m, int poolRows, int poolCols) {
        Matrix indexMatrix = null;
        try {
            int rowrem = m.rows % poolRows;
            int colrem = m.cols % poolCols;
            if(rowrem != 0 || colrem != 0){
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
                int rowEnd = rowStart + poolRows - 1;

                // i = row # in this matrix c
                for (int j = 0; j < m.cols; j = j + poolCols) {
                    int colStart = j;
                    int colEnd = colStart + poolCols - 1;
                    LOG.fine("i: " + i + ", j: " + j);
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
                    LOG.fine(", subK: " + subK + ", IJ.i: " + subIJ.i() + ", IJ.j: " + subIJ.j());
                    // find index of max cell in matrix m = ref pt in m + rel index in subM
                    int maxI = rowStart + subIJ.i();
                    int maxJ = colStart + subIJ.j();
                    int maxK = maxI * m.cols + maxJ;
                    LOG.fine("maxI: " + maxI + ", maxJ: " + maxJ + ", maxK: " + maxK);
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
     * Find average of a list of matrices, and save in new matrix where
     * each cell now contains the average value of all corresponding
     * cells from the list.
     *
     * @param mList list of matrix
     * @return average matrix
     */
    public static Matrix averageOfList(List<Matrix> mList) {
        Matrix average = null;
        try {
            int n = mList.size();
            // initialize averagematrix with sum of all matrix in list
            average = sumOfList(mList);
            // now divide sum matrix by n to get average for each cell
            mulConstInPlace(average, 1.0/n);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return average;
    }
    /**
     * Find sum of a list of matrices and save in new matrix
     *
     * @param mList list of matrix
     * @return sum matrix
     */
    public static Matrix sumOfList(List<Matrix> mList) {
        Matrix sum = null;
        try {
            // n = number of matrix in list
            int n = mList.size();
            // check first matrix for size
            Matrix m = mList.get(0);
            sum = new Matrix(m.rows, m.cols);
            for (int k = 0; k < n; k++) {
                // next matrix from input list
                Matrix u = mList.get(k);
                // add matrix u to sum matrix
                addInplace(sum, u);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return sum;
    }

    /**
     * Compare matrix elements to a constant value
     *
     * @param b matrix to copy
     * @param c constant to compare
     */
    public static boolean equalsConst(Matrix b, double c) {
        boolean result = false;
        int n = b.size;
        for (int k = 0; k < n; k++) {
            if(b.a[k] != c){
                result = true;
                break;
            }
        }
        return result;
    }

} // end class