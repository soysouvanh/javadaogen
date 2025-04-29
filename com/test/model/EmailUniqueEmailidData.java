package com.test.model;



public class EmailUniqueEmailidData {
    private Integer emailid;
	
	public EmailUniqueEmailidData() {
	}
	
	public EmailUniqueEmailidData(Integer emailid) {
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
		return "EmailUniqueEmailidData{"
			+                 "emailid='" + emailid + '\''
			+ "}";
	}
}