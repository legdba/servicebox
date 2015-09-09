package com.brimarx.servicebox.model;

/**
 * Created by vincent on 06/09/15.
 */
public class SumResult {

    public SumResult(String id, long value) {
        this.id = id;
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private long value;
}
