package com.test.model;



public class CustomerUniqueCustomeridData {
    private Integer customerid;
	
	public CustomerUniqueCustomeridData() {
	}
	
	public CustomerUniqueCustomeridData(Integer customerid) {
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
		return "CustomerUniqueCustomeridData{"
			+                 "customerid='" + customerid + '\''
			+ "}";
	}
}