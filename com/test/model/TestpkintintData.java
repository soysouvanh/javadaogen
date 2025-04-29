package com.test.model;



public class TestpkintintData {

	    private Integer pk1int;
    private Integer pk2int;
    private String label;
    private String language;
    private Integer idx12;
    private Integer idx22;

    public TestpkintintData() {
    }

    public TestpkintintData(Integer pk1int, Integer pk2int, String label, String language, Integer idx12, Integer idx22) {
		        this.pk1int = pk1int;
        this.pk2int = pk2int;
        this.label = label;
        this.language = language;
        this.idx12 = idx12;
        this.idx22 = idx22;
    }

	public Integer getPk1int() {
        return pk1int;
    }

    public void setPk1int(Integer pk1int) {
        this.pk1int = pk1int;
    }

    public Integer getPk2int() {
        return pk2int;
    }

    public void setPk2int(Integer pk2int) {
        this.pk2int = pk2int;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getIdx12() {
        return idx12;
    }

    public void setIdx12(Integer idx12) {
        this.idx12 = idx12;
    }

    public Integer getIdx22() {
        return idx22;
    }

    public void setIdx22(Integer idx22) {
        this.idx22 = idx22;
    }

    @Override
    public String toString() {
        return "TestpkintintData{" +                 "pk1int='" + pk1int + '\'' + ", " +
                "pk2int='" + pk2int + '\'' + ", " +
                "label='" + label + '\'' + ", " +
                "language='" + language + '\'' + ", " +
                "idx12='" + idx12 + '\'' + ", " +
                "idx22='" + idx22 + '\'' + "}";
    }
}