/*
 * The My Company License
 *
 * Copyright 2016 by My Company. All Rights Reserved.
 * This software is the proprietary information of My Company.
 */
package datasci.frontend.config;

import datasci.backend.model.Matrix;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ron cook at My Company
 */
public class FilterDialog extends Dialog<ArrayList<Matrix>> {

    private static final Logger LOG = Logger.getLogger(FilterDialog.class.getName());

    private final String title = "Filter Dialog";
    private final static double OUTER_MAX_H = 600;
    private FilterView filterEdit;
    private int filterSize;
    private int numFilters;
    // edited filters output
    private ArrayList<Matrix> filterList;
    // initial filters input, if any
    private ArrayList<Matrix> initFilters;

    public FilterDialog(int filterSize, int numFilters, ArrayList<Matrix> initFilters) {
        this.filterSize = filterSize;
        this.numFilters = numFilters;
        this.initFilters = initFilters;
        init();
    }

    public void init() {
        try {
            LOG.log(Level.INFO, "init");
            setTitle(title);
            setHeaderText("This is the matrix filters dialog. Enter info and \n"
                    + "press Okay (or click title bar 'X' for cancel).");
            setResizable(true);
            //
            BorderPane outerPane = new BorderPane();
            outerPane.setPrefHeight(OUTER_MAX_H);
            filterEdit = new FilterView(initFilters);
            Pane formPanel = filterEdit.initFormPanel();
            // Set margin for child node when contained by border pane
            // Insets: top, right, bottom, left (pixel units)
            BorderPane.setMargin(formPanel, new Insets(10,10,10,10));
            outerPane.setTop(formPanel);
            //
            ScrollPane scroller = filterEdit.initFiltersPanel(filterSize, filterSize, numFilters);
            if(initFilters != null) {
                // init filters in filter panel scroller
                filterEdit.setMatrixList(initFilters);
            }

            outerPane.setCenter(scroller);
            //
            getDialogPane().setContent(outerPane);

            // Add button to dialog
            ButtonType buttonOk = ButtonType.OK;
            getDialogPane().getButtonTypes().add(buttonOk);
            ButtonType buttonCancel = ButtonType.CANCEL;
            getDialogPane().getButtonTypes().add(buttonCancel);

            // Result converter for dialog
            setResultConverter(new Callback<ButtonType, ArrayList<Matrix>>() {
                @Override
                public ArrayList<Matrix> call(ButtonType b) {
                    if (b == buttonOk) {
                        getFilters();
                        return filterList;
                    } else if (b == buttonCancel) {
                        //cancel button
                        return null;
                    } else {
                        return null;
                    }
                }
            });

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void getFilters() {
        try {
            filterList = filterEdit.getFilterList();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
}  //  end class
