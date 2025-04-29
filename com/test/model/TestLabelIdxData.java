package com.test.model;



public class TestLabelIdxData {

	    private String label;

    public TestLabelIdxData() {
    }

    public TestLabelIdxData(String label) {
		        this.label = label;
    }

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "TestLabelIdxData{" +                 "label='" + label + '\'' + "}";
    }
}