package com.test.model;



public class TestUniqueTestidData {
    private Integer testid;
	
	public TestUniqueTestidData() {
	}
	
	public TestUniqueTestidData(Integer testid) {
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
		return "TestUniqueTestidData{" + "testid='" + testid + '\'' + "}";
	}
}