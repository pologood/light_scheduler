package com.jd.eptid.scheduler.client.test.param;

/**
 * Created by classdan on 16-10-27.
 */
public class TestParam {
    private String name;
    private int code;
    private TestNestedParam nestedParam;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public TestNestedParam getNestedParam() {
        return nestedParam;
    }

    public void setNestedParam(TestNestedParam nestedParam) {
        this.nestedParam = nestedParam;
    }
}
