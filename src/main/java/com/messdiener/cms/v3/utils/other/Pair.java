package com.messdiener.cms.v3.utils.other;

public class Pair<A,B> {

    private A first;
    private B second;

    public Pair(A a, B b) {
        this.first = a;
        this.second = b;
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B b) {
        this.second = b;
    }
}
