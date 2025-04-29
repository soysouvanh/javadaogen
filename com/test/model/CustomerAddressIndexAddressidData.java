package com.test.model;



public class CustomerAddressIndexAddressidData {
    private Integer addressid;
	
	public CustomerAddressIndexAddressidData() {
	}
	
	public CustomerAddressIndexAddressidData(Integer addressid) {
		this.addressid = addressid;
	}
	
	public Integer getAddressid() {
		return addressid;
    }

	public void setAddressid(Integer addressid) {
		this.addressid = addressid;
	}
	
	@Override
	public String toString() {
		return "CustomerAddressIndexAddressidData{" + "addressid='" + addressid + '\'' + "}";
	}
}