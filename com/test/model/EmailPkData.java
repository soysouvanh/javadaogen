package com.test.model;



public class EmailPkData {
    private Integer emailid;
	
	public EmailPkData() {
	}
	
	public EmailPkData(Integer emailid) {
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
		return "EmailPkData{" + "emailid='" + emailid + '\'' + "}";
	}
}