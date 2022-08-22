/*
 * The My Company License
 *
 * Copyright 2016 by My Company. All Rights Reserved.
 * This software is the proprietary information of My Company.
 */
package datasci.backend.model;

import java.util.logging.Logger;

/**
 * Store network input data along with actual output value
 */
public class NetData {

    private static final Logger LOG = Logger.getLogger(NetData.class.getName());

    private Matrix inputData;
    // actualIndex: true output index (of class) from known input data
    private int actualIndex;

    /**
     * Default Constructor
     */
    public NetData() {
    }

    /**
     * Instantiates a new Net data.
     *
     * @param inputData      the input data
     * @param actualIndex the actual index
     */
    public NetData(Matrix inputData, int actualIndex) {
        this.inputData = inputData;
        this.actualIndex = actualIndex;
    }

    /**
     * Gets input data.
     *
     * @return the input data
     */
    public Matrix getInputData() {
        return inputData;
    }

    /**
     * Sets input data.
     *
     * @param inputData the input data
     */
    public void setInputData(Matrix inputData) {
        this.inputData = inputData;
    }

    /**
     * Gets actual output.
     *
     * @return the actual output
     */
    public int getActualIndex() {
        return actualIndex;
    }

    /**
     * Sets actual output.
     *
     * @param actualIndex the actual output
     */
    public void setActualIndex(int actualIndex) {
        this.actualIndex = actualIndex;
    }

    @Override
    public String toString() {
        return "NetData{" +
                "inputData=" + inputData +
                ", actualIndex=" + actualIndex +
                '}';
    }
}  //  end class
