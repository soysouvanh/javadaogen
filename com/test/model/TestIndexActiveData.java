package com.test.model;



public class TestIndexActiveData {
    private Integer active;
	
	public TestIndexActiveData() {
	}
	
	public TestIndexActiveData(Integer active) {
		this.active = active;
	}
	
	public Integer getActive() {
		return active;
    }

	public void setActive(Integer active) {
		this.active = active;
	}
	
	@Override
	public String toString() {
		return "TestIndexActiveData{"
			+                 "active='" + active + '\''
			+ "}";
	}
}