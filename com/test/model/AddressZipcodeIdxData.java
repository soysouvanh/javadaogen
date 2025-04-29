package com.test.model;



public class AddressZipcodeIdxData {

	    private String zipcode;

    public AddressZipcodeIdxData() {
    }

    public AddressZipcodeIdxData(String zipcode) {
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
        return "AddressZipcodeIdxData{" +                 "zipcode='" + zipcode + '\'' + "}";
    }
}