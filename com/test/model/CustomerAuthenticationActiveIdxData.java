package com.test.model;



public class CustomerAuthenticationActiveIdxData {

	    private Integer active;

    public CustomerAuthenticationActiveIdxData() {
    }

    public CustomerAuthenticationActiveIdxData(Integer active) {
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
        return "CustomerAuthenticationActiveIdxData{" +                 "active='" + active + '\'' + "}";
    }
}