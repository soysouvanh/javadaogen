package com.test.model;



public class TestActiveIdxData {

	    private Integer active;

    public TestActiveIdxData() {
    }

    public TestActiveIdxData(Integer active) {
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
        return "TestActiveIdxData{" +                 "active='" + active + '\'' + "}";
    }
}