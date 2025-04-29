package com.test.model;



public class CustomerAuthenticationIndexActiveData {
    private Integer active;
	
	public CustomerAuthenticationIndexActiveData() {
	}
	
	public CustomerAuthenticationIndexActiveData(Integer active) {
		this.active = active;
	}
	
	public Integer getActive() {
		return active;
    }

	public void setActive(Integer active) {
		this.active = active;
	}
	
	@Override
	public String toString() {
		return "CustomerAuthenticationIndexActiveData{" + "active='" + active + '\'' + "}";
	}
}