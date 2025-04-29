package com.test.model;



public class CustomerAuthenticationUniqueEmailidData {
    private Integer emailid;
	
	public CustomerAuthenticationUniqueEmailidData() {
	}
	
	public CustomerAuthenticationUniqueEmailidData(Integer emailid) {
		this.emailid = emailid;
	}
	
	public Integer getEmailid() {
		return emailid;
    }

	public void setEmailid(Integer emailid) {
		this.emailid = emailid;
	}
	
	@Override
	public String toString() {
		return "CustomerAuthenticationUniqueEmailidData{" + "emailid='" + emailid + '\'' + "}";
	}
}