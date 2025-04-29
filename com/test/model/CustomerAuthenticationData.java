package com.test.model;

import java.sql.Timestamp;


public class CustomerAuthenticationData {

	    private Integer customerid;
    private String pseudonym;
    private Integer emailid;
    private String password;
    private Integer active;
    private java.sql.Timestamp updated;

    public CustomerAuthenticationData() {
    }

    public CustomerAuthenticationData(Integer customerid, String pseudonym, Integer emailid, String password, Integer active, java.sql.Timestamp updated) {
		        this.customerid = customerid;
        this.pseudonym = pseudonym;
        this.emailid = emailid;
        this.password = password;
        this.active = active;
        this.updated = updated;
    }

	public Integer getCustomerid() {
        return customerid;
    }

    public void setCustomerid(Integer customerid) {
        this.customerid = customerid;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

    public Integer getEmailid() {
        return emailid;
    }

    public void setEmailid(Integer emailid) {
        this.emailid = emailid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public java.sql.Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(java.sql.Timestamp updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "CustomerAuthenticationData{" +                 "customerid='" + customerid + '\'' + ", " +
                "pseudonym='" + pseudonym + '\'' + ", " +
                "emailid='" + emailid + '\'' + ", " +
                "password='" + password + '\'' + ", " +
                "active='" + active + '\'' + ", " +
                "updated='" + updated + '\'' + "}";
    }
}