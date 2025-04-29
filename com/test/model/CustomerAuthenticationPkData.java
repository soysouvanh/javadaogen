package com.test.model;



public class CustomerAuthenticationPkData {
    private Integer customerid;
	
	public CustomerAuthenticationPkData() {
	}
	
	public CustomerAuthenticationPkData(Integer customerid) {
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
		return "CustomerAuthenticationPkData{" + "customerid='" + customerid + '\'' + "}";
	}
}