package com.voting;

import java.io.Serializable;

public class Vote implements Serializable {
    private String option;
    private int count;

    public Vote(String option) {
        this.option = option;
        this.count = 0;
    }

    public String getOption() {
        return option;
    }

    public int getCount() {
        return count;
    }

    public void increment() {
        count++;
    }
}
