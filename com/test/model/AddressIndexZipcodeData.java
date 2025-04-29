package com.test.model;



public class AddressIndexZipcodeData {
    private String zipcode;
	
	public AddressIndexZipcodeData() {
	}
	
	public AddressIndexZipcodeData(String zipcode) {
		this.zipcode = zipcode;
	}
	
	public String getZipcode() {
		return zipcode;
    }

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	
	@Override
	public String toString() {
		return "AddressIndexZipcodeData{" + "zipcode='" + zipcode + '\'' + "}";
	}
}