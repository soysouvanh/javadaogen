package com.test.model;



public class TestpkintintPk1intPk2intUniqueData {

	    private Integer pk1int;
    private Integer pk2int;

    public TestpkintintPk1intPk2intUniqueData() {
    }

    public TestpkintintPk1intPk2intUniqueData(Integer pk1int, Integer pk2int) {
		        this.pk1int = pk1int;
        this.pk2int = pk2int;
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

    @Override
    public String toString() {
        return "TestpkintintPk1intPk2intUniqueData{" +                 "pk1int='" + pk1int + '\'' + ", " +
                "pk2int='" + pk2int + '\'' + "}";
    }
}