package com.test.model;



public class CustomerAddressData {
    private Integer customerid;
    private Integer addressid;
	
	public CustomerAddressData() {
	}
	
	public CustomerAddressData(Integer customerid, Integer addressid) {
		this.customerid = customerid;
this.addressid = addressid;
	}
	
	public Integer getCustomerid() {
		return customerid;
    }

	public void setCustomerid(Integer customerid) {
		this.customerid = customerid;
	}

	public Integer getAddressid() {
		return addressid;
    }

	public void setAddressid(Integer addressid) {
		this.addressid = addressid;
	}
	
	@Override
	public String toString() {
		return "CustomerAddressData{"
			+                 "customerid='" + customerid + '\'' + ", " +
                "addressid='" + addressid + '\''
			+ "}";
	}
}