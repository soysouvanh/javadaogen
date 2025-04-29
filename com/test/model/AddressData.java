package com.test.model;



public class AddressData {

	    private Long addressid;
    private String address;
    private String place;
    private String zipcode;
    private String cityid;

    public AddressData() {
    }

    public AddressData(Long addressid, String address, String place, String zipcode, String cityid) {
		        this.addressid = addressid;
        this.address = address;
        this.place = place;
        this.zipcode = zipcode;
        this.cityid = cityid;
    }

	public Long getAddressid() {
        return addressid;
    }

    public void setAddressid(Long addressid) {
        this.addressid = addressid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCityid() {
        return cityid;
    }

    public void setCityid(String cityid) {
        this.cityid = cityid;
    }

    @Override
    public String toString() {
        return "AddressData{" +                 "addressid='" + addressid + '\'' + ", " +
                "address='" + address + '\'' + ", " +
                "place='" + place + '\'' + ", " +
                "zipcode='" + zipcode + '\'' + ", " +
                "cityid='" + cityid + '\'' + "}";
    }
}