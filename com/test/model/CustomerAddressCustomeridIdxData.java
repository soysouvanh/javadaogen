package com.test.model;



public class CustomerAddressCustomeridIdxData {

	    private Integer customerid;

    public CustomerAddressCustomeridIdxData() {
    }

    public CustomerAddressCustomeridIdxData(Integer customerid) {
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
        return "CustomerAddressCustomeridIdxData{" +                 "customerid='" + customerid + '\'' + "}";
    }
}