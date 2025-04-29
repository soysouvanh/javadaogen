package com.test.model;



public class TestData {
    private Integer testid;
    private String language;
    private String label;
    private Integer active;
	
	public TestData() {
	}
	
	public TestData(Integer testid, String language, String label, Integer active) {
		this.testid = testid;
this.language = language;
this.label = label;
this.active = active;
	}
	
	public Integer getTestid() {
		return testid;
    }

	public void setTestid(Integer testid) {
		this.testid = testid;
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

	public Integer getActive() {
		return active;
    }

	public void setActive(Integer active) {
		this.active = active;
	}
	
	@Override
	public String toString() {
		return "TestData{" + "testid='" + testid + '\'' + ", " +
"language='" + language + '\'' + ", " +
"label='" + label + '\'' + ", " +
"active='" + active + '\'' + "}";
	}
}