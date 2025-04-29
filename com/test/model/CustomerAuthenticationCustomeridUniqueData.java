package com.test.model;



public class CustomerAuthenticationCustomeridUniqueData {

	    private Integer customerid;

    public CustomerAuthenticationCustomeridUniqueData() {
    }

    public CustomerAuthenticationCustomeridUniqueData(Integer customerid) {
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
        return "CustomerAuthenticationCustomeridUniqueData{" +                 "customerid='" + customerid + '\'' + "}";
    }
}