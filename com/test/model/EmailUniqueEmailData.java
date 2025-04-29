package com.test.model;



public class EmailUniqueEmailData {
    private String email;
	
	public EmailUniqueEmailData() {
	}
	
	public EmailUniqueEmailData(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
    }

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String toString() {
		return "EmailUniqueEmailData{"
			+                 "email='" + email + '\''
			+ "}";
	}
}