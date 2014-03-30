package com.jimg.scoutingapp.pojos;

import java.io.Serializable;

/**
 * Created by Jim on 2/9/14.
 */
public class TeamTriplet implements Serializable {
    public final Integer id;
    public final String name;
    public final String division;

    public TeamTriplet(Integer id, String name, String division) {
        this.id = id;
        this.name = name;
        this.division = division;
    }

    @Override
    public String toString() {
        return name;
    }
}