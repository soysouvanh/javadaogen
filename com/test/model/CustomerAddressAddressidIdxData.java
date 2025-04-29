package com.test.model;



public class CustomerAddressAddressidIdxData {

	    private Integer addressid;

    public CustomerAddressAddressidIdxData() {
    }

    public CustomerAddressAddressidIdxData(Integer addressid) {
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
        return "CustomerAddressAddressidIdxData{" +                 "addressid='" + addressid + '\'' + "}";
    }
}