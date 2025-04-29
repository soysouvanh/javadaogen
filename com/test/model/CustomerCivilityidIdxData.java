package com.test.model;



public class CustomerCivilityidIdxData {

	    private Integer civilityid;

    public CustomerCivilityidIdxData() {
    }

    public CustomerCivilityidIdxData(Integer civilityid) {
		        this.civilityid = civilityid;
    }

	public Integer getCivilityid() {
        return civilityid;
    }

    public void setCivilityid(Integer civilityid) {
        this.civilityid = civilityid;
    }

    @Override
    public String toString() {
        return "CustomerCivilityidIdxData{" +                 "civilityid='" + civilityid + '\'' + "}";
    }
}