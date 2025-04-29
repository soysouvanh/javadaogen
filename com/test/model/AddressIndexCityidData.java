package com.test.model;



public class AddressIndexCityidData {
    private String cityid;
	
	public AddressIndexCityidData() {
	}
	
	public AddressIndexCityidData(String cityid) {
		this.cityid = cityid;
	}
	
	public String getCityid() {
		return cityid;
    }

	public void setCityid(String cityid) {
		this.cityid = cityid;
	}
	
	@Override
	public String toString() {
		return "AddressIndexCityidData{" + "cityid='" + cityid + '\'' + "}";
	}
}