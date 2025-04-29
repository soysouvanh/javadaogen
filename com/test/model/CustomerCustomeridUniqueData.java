package com.test.model;



public class CustomerCustomeridUniqueData {

	    private Integer customerid;

    public CustomerCustomeridUniqueData() {
    }

    public CustomerCustomeridUniqueData(Integer customerid) {
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
        return "CustomerCustomeridUniqueData{" +                 "customerid='" + customerid + '\'' + "}";
    }
}