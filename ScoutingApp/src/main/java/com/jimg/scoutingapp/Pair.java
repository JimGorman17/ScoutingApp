package com.jimg.scoutingapp;

import java.io.Serializable;

/**
 * Created by Jim on 2/17/14.
 */
public class Pair<S, T> implements Serializable {

    public S first;
    public T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }
}
