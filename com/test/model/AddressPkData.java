package com.test.model;



public class AddressPkData {
    private Long addressid;
	
	public AddressPkData() {
	}
	
	public AddressPkData(Long addressid) {
		this.addressid = addressid;
	}
	
	public Long getAddressid() {
		return addressid;
    }

	public void setAddressid(Long addressid) {
		this.addressid = addressid;
	}
	
	@Override
	public String toString() {
		return "AddressPkData{" + "addressid='" + addressid + '\'' + "}";
	}
}