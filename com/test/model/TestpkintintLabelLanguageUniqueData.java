package com.test.model;



public class TestpkintintLabelLanguageUniqueData {

	    private String label;
    private String language;

    public TestpkintintLabelLanguageUniqueData() {
    }

    public TestpkintintLabelLanguageUniqueData(String label, String language) {
		        this.label = label;
        this.language = language;
    }

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "TestpkintintLabelLanguageUniqueData{" +                 "label='" + label + '\'' + ", " +
                "language='" + language + '\'' + "}";
    }
}