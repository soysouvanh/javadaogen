package com.test.model;



public class CustomerAuthenticationUniqueCustomeridData {
    private Integer customerid;
	
	public CustomerAuthenticationUniqueCustomeridData() {
	}
	
	public CustomerAuthenticationUniqueCustomeridData(Integer customerid) {
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
		return "CustomerAuthenticationUniqueCustomeridData{" + "customerid='" + customerid + '\'' + "}";
	}
}