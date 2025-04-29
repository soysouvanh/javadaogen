package com.test.model;



public class CustomerIndexCivilityidData {
    private Integer civilityid;
	
	public CustomerIndexCivilityidData() {
	}
	
	public CustomerIndexCivilityidData(Integer civilityid) {
		this.civilityid = civilityid;
	}
	
	public Integer getCivilityid() {
		return civilityid;
    }

	public void setCivilityid(Integer civilityid) {
		this.civilityid = civilityid;
	}
	
	@Override
	public String toString() {
		return "CustomerIndexCivilityidData{" + "civilityid='" + civilityid + '\'' + "}";
	}
}