package com.test.model;



public class EmailEmailidUniqueData {

	    private Integer emailid;

    public EmailEmailidUniqueData() {
    }

    public EmailEmailidUniqueData(Integer emailid) {
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
        return "EmailEmailidUniqueData{" +                 "emailid='" + emailid + '\'' + "}";
    }
}