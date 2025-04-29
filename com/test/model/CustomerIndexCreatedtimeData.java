package com.test.model;

import java.sql.Time;


public class CustomerIndexCreatedtimeData {
    private java.sql.Time createdtime;
	
	public CustomerIndexCreatedtimeData() {
	}
	
	public CustomerIndexCreatedtimeData(java.sql.Time createdtime) {
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
		return "CustomerIndexCreatedtimeData{"
			+                 "createdtime='" + createdtime + '\''
			+ "}";
	}
}