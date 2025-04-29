package com.test.model;



public class AddressAddressidUniqueData {

	    private Long addressid;

    public AddressAddressidUniqueData() {
    }

    public AddressAddressidUniqueData(Long addressid) {
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
        return "AddressAddressidUniqueData{" +                 "addressid='" + addressid + '\'' + "}";
    }
}