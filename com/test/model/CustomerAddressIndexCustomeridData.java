package com.test.model;



public class CustomerAddressIndexCustomeridData {
    private Integer customerid;
	
	public CustomerAddressIndexCustomeridData() {
	}
	
	public CustomerAddressIndexCustomeridData(Integer customerid) {
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
		return "CustomerAddressIndexCustomeridData{" + "customerid='" + customerid + '\'' + "}";
	}
}