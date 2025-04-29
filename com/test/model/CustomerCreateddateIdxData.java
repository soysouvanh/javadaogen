package com.test.model;

import java.sql.Date;


public class CustomerCreateddateIdxData {

	    private java.sql.Date createddate;

    public CustomerCreateddateIdxData() {
    }

    public CustomerCreateddateIdxData(java.sql.Date createddate) {
		        this.createddate = createddate;
    }

	public java.sql.Date getCreateddate() {
        return createddate;
    }

    public void setCreateddate(java.sql.Date createddate) {
        this.createddate = createddate;
    }

    @Override
    public String toString() {
        return "CustomerCreateddateIdxData{" +                 "createddate='" + createddate + '\'' + "}";
    }
}