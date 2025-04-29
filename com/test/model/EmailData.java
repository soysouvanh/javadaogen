package com.test.model;



public class EmailData {
    private Integer emailid;
    private String email;
	
	public EmailData() {
	}
	
	public EmailData(Integer emailid, String email) {
		this.emailid = emailid;
this.email = email;
	}
	
	public Integer getEmailid() {
		return emailid;
    }

	public void setEmailid(Integer emailid) {
		this.emailid = emailid;
	}

	public String getEmail() {
		return email;
    }

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String toString() {
		return "EmailData{"
			+                 "emailid='" + emailid + '\'' + ", " +
                "email='" + email + '\''
			+ "}";
	}
}