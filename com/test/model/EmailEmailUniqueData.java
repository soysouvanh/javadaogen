package com.test.model;



public class EmailEmailUniqueData {

	    private String email;

    public EmailEmailUniqueData() {
    }

    public EmailEmailUniqueData(String email) {
		        this.email = email;
    }

	public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "EmailEmailUniqueData{" +                 "email='" + email + '\'' + "}";
    }
}