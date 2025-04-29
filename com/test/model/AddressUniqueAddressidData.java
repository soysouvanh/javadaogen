package com.test.model;



public class AddressUniqueAddressidData {
    private Long addressid;
	
	public AddressUniqueAddressidData() {
	}
	
	public AddressUniqueAddressidData(Long addressid) {
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
		return "AddressUniqueAddressidData{" + "addressid='" + addressid + '\'' + "}";
	}
}