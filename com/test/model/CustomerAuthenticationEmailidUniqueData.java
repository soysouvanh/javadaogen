package com.test.model;



public class CustomerAuthenticationEmailidUniqueData {

	    private Integer emailid;

    public CustomerAuthenticationEmailidUniqueData() {
    }

    public CustomerAuthenticationEmailidUniqueData(Integer emailid) {
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
        return "CustomerAuthenticationEmailidUniqueData{" +                 "emailid='" + emailid + '\'' + "}";
    }
}