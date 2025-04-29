package com.test.model;

import java.sql.Date;


public class CustomerIndexCreateddateData {
    private java.sql.Date createddate;
	
	public CustomerIndexCreateddateData() {
	}
	
	public CustomerIndexCreateddateData(java.sql.Date createddate) {
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
		return "CustomerIndexCreateddateData{"
			+                 "createddate='" + createddate + '\''
			+ "}";
	}
}