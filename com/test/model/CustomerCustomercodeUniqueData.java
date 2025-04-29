package com.test.model;



public class CustomerCustomercodeUniqueData {

	    private String customercode;

    public CustomerCustomercodeUniqueData() {
    }

    public CustomerCustomercodeUniqueData(String customercode) {
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
        return "CustomerCustomercodeUniqueData{" +                 "customercode='" + customercode + '\'' + "}";
    }
}