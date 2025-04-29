package com.test.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;


public class CustomerData {

	    private Integer customerid;
    private String customercode;
    private Integer civilityid;
    private String lastname;
    private java.sql.Timestamp updated;
    private java.sql.Date createddate;
    private java.sql.Time createdtime;

    public CustomerData() {
    }

    public CustomerData(Integer customerid, String customercode, Integer civilityid, String lastname, java.sql.Timestamp updated, java.sql.Date createddate, java.sql.Time createdtime) {
		        this.customerid = customerid;
        this.customercode = customercode;
        this.civilityid = civilityid;
        this.lastname = lastname;
        this.updated = updated;
        this.createddate = createddate;
        this.createdtime = createdtime;
    }

	public Integer getCustomerid() {
        return customerid;
    }

    public void setCustomerid(Integer customerid) {
        this.customerid = customerid;
    }

    public String getCustomercode() {
        return customercode;
    }

    public void setCustomercode(String customercode) {
        this.customercode = customercode;
    }

    public Integer getCivilityid() {
        return civilityid;
    }

    public void setCivilityid(Integer civilityid) {
        this.civilityid = civilityid;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public java.sql.Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(java.sql.Timestamp updated) {
        this.updated = updated;
    }

    public java.sql.Date getCreateddate() {
        return createddate;
    }

    public void setCreateddate(java.sql.Date createddate) {
        this.createddate = createddate;
    }

    public java.sql.Time getCreatedtime() {
        return createdtime;
    }

    public void setCreatedtime(java.sql.Time createdtime) {
        this.createdtime = createdtime;
    }

    @Override
    public String toString() {
        return "CustomerData{" +                 "customerid='" + customerid + '\'' + ", " +
                "customercode='" + customercode + '\'' + ", " +
                "civilityid='" + civilityid + '\'' + ", " +
                "lastname='" + lastname + '\'' + ", " +
                "updated='" + updated + '\'' + ", " +
                "createddate='" + createddate + '\'' + ", " +
                "createdtime='" + createdtime + '\'' + "}";
    }
}