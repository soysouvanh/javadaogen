package com.test.model;



public class CustomerAuthenticationUniquePseudonymData {
    private String pseudonym;
	
	public CustomerAuthenticationUniquePseudonymData() {
	}
	
	public CustomerAuthenticationUniquePseudonymData(String pseudonym) {
		this.pseudonym = pseudonym;
	}
	
	public String getPseudonym() {
		return pseudonym;
    }

	public void setPseudonym(String pseudonym) {
		this.pseudonym = pseudonym;
	}
	
	@Override
	public String toString() {
		return "CustomerAuthenticationUniquePseudonymData{"
			+                 "pseudonym='" + pseudonym + '\''
			+ "}";
	}
}