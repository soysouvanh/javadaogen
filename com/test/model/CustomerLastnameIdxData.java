package com.test.model;



public class CustomerLastnameIdxData {

	    private String lastname;

    public CustomerLastnameIdxData() {
    }

    public CustomerLastnameIdxData(String lastname) {
		        this.lastname = lastname;
    }

	public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return "CustomerLastnameIdxData{" +                 "lastname='" + lastname + '\'' + "}";
    }
}