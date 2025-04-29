package com.test.model;



public class CustomerUniqueCustomercodeData {
    private String customercode;
	
	public CustomerUniqueCustomercodeData() {
	}
	
	public CustomerUniqueCustomercodeData(String customercode) {
		this.customercode = customercode;
	}
	
	public String getCustomercode() {
		return customercode;
    }

	public void setCustomercode(String customercode) {
		this.customercode = customercode;
	}
	
	@Override
	public String toString() {
		return "CustomerUniqueCustomercodeData{" + "customercode='" + customercode + '\'' + "}";
	}
}