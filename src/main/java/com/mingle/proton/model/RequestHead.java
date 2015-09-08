package com.mingle.proton.model;

/**
 * Created by Daniel on 2015/7/13.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public class RequestHead {

    private String headName;
    private String headValue;

    public RequestHead() {

    }

    public RequestHead(String headName, String headValue) {
        this.headName = headName;
        this.headValue = headValue;
    }

    public String getHeadName() {
        return headName;
    }

    public void setHeadName(String headName) {
        this.headName = headName;
    }

    public String getHeadValue() {
        return headValue;
    }

    public void setHeadValue(String headValue) {
        this.headValue = headValue;
    }
}
