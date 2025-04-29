package com.test.model;



public class TestIndexLabelData {
    private String label;
	
	public TestIndexLabelData() {
	}
	
	public TestIndexLabelData(String label) {
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
		return "TestIndexLabelData{"
			+                 "label='" + label + '\''
			+ "}";
	}
}