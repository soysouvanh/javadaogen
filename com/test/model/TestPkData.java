package com.test.model;



public class TestPkData {

	    private Integer testid;

    public TestPkData() {
    }

    public TestPkData(Integer testid) {
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
        return "TestPkData{" +                 "testid='" + testid + '\'' + "}";
    }
}