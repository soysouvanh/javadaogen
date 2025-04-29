package com.test.model;



public class CustomerAddressUniqueCustomeridAddressidData {
    private Integer customerid;
    private Integer addressid;
	
	public CustomerAddressUniqueCustomeridAddressidData() {
	}
	
	public CustomerAddressUniqueCustomeridAddressidData(Integer customerid, Integer addressid) {
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
		return "CustomerAddressUniqueCustomeridAddressidData{" + "customerid='" + customerid + '\'' + ", " +
"addressid='" + addressid + '\'' + "}";
	}
}