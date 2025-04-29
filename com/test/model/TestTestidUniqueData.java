package com.test.model;



public class TestTestidUniqueData {

	    private Integer testid;

    public TestTestidUniqueData() {
    }

    public TestTestidUniqueData(Integer testid) {
		        this.testid = testid;
    }

	public Integer getTestid() {
        return testid;
    }

    public void setTestid(Integer testid) {
        this.testid = testid;
    }

    @Override
    public String toString() {
        return "TestTestidUniqueData{" +                 "testid='" + testid + '\'' + "}";
    }
}