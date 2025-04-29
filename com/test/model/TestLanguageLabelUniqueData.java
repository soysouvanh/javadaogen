package com.test.model;



public class TestLanguageLabelUniqueData {

	    private String language;
    private String label;

    public TestLanguageLabelUniqueData() {
    }

    public TestLanguageLabelUniqueData(String language, String label) {
		        this.language = language;
        this.label = label;
    }

	public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "TestLanguageLabelUniqueData{" +                 "language='" + language + '\'' + ", " +
                "label='" + label + '\'' + "}";
    }
}