package com.brimarx.servicebox.model;

/**
 * Created by vincent on 06/09/15.
 */
public class RetainedHeap {

    public RetainedHeap(long retainedHeap) {
        this.retainedHeap = retainedHeap;
    }

    public long getRetainedHeap() {
        return retainedHeap;
    }

    public void setRetainedHeap(long retainedHeap) {
        this.retainedHeap = retainedHeap;
    }

    private long retainedHeap;
}
