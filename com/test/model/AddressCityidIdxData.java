package com.test.model;



public class AddressCityidIdxData {

	    private String cityid;

    public AddressCityidIdxData() {
    }

    public AddressCityidIdxData(String cityid) {
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
        return "AddressCityidIdxData{" +                 "cityid='" + cityid + '\'' + "}";
    }
}