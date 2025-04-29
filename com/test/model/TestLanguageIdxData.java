package com.test.model;



public class TestLanguageIdxData {

	    private String language;

    public TestLanguageIdxData() {
    }

    public TestLanguageIdxData(String language) {
		        this.language = language;
    }

	public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "TestLanguageIdxData{" +                 "language='" + language + '\'' + "}";
    }
}