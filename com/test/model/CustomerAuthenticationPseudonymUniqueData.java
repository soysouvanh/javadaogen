package com.test.model;



public class CustomerAuthenticationPseudonymUniqueData {

	    private String pseudonym;

    public CustomerAuthenticationPseudonymUniqueData() {
    }

    public CustomerAuthenticationPseudonymUniqueData(String pseudonym) {
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
        return "CustomerAuthenticationPseudonymUniqueData{" +                 "pseudonym='" + pseudonym + '\'' + "}";
    }
}