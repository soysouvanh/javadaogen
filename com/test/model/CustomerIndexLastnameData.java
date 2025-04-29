package com.test.model;



public class CustomerIndexLastnameData {
    private String lastname;
	
	public CustomerIndexLastnameData() {
	}
	
	public CustomerIndexLastnameData(String lastname) {
		this.lastname = lastname;
	}
	
	public String getLastname() {
		return lastname;
    }

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	@Override
	public String toString() {
		return "CustomerIndexLastnameData{" + "lastname='" + lastname + '\'' + "}";
	}
}