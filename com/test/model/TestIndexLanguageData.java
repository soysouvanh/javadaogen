package com.test.model;



public class TestIndexLanguageData {
    private String language;
	
	public TestIndexLanguageData() {
	}
	
	public TestIndexLanguageData(String language) {
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
		return "TestIndexLanguageData{" + "language='" + language + '\'' + "}";
	}
}