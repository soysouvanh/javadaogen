package com.test.model;



public class CustomerPkData {

	    private Integer customerid;

    public CustomerPkData() {
    }

    public CustomerPkData(Integer customerid) {
		        this.customerid = customerid;
    }

	public Integer getCustomerid() {
        return customerid;
    }

    public void setCustomerid(Integer customerid) {
        this.customerid = customerid;
    }

    @Override
    public String toString() {
        return "CustomerPkData{" +                 "customerid='" + customerid + '\'' + "}";
    }
}