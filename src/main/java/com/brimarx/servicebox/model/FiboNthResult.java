package com.brimarx.servicebox.model;

/**
 * Created by vincent on 09/09/15.
 */
public class FiboNthResult {

    public FiboNthResult(long n, long term) {
        this.n = n;
        this.term = term;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getN() {
        return n;
    }

    public void setN(long n) {
        this.n = n;
    }

    private long n;
    private long term;
}
