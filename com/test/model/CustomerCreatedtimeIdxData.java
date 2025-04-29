package com.test.model;

import java.sql.Time;


public class CustomerCreatedtimeIdxData {

	    private java.sql.Time createdtime;

    public CustomerCreatedtimeIdxData() {
    }

    public CustomerCreatedtimeIdxData(java.sql.Time createdtime) {
		        this.createdtime = createdtime;
    }

	public java.sql.Time getCreatedtime() {
        return createdtime;
    }

    public void setCreatedtime(java.sql.Time createdtime) {
        this.createdtime = createdtime;
    }

    @Override
    public String toString() {
        return "CustomerCreatedtimeIdxData{" +                 "createdtime='" + createdtime + '\'' + "}";
    }
}