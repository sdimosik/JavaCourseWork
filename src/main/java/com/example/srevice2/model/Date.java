package com.example.srevice2.model;

import lombok.Data;

@Data
public class Date implements Comparable<Date> {
    private Integer days;
    private Integer minutes;

    public Date(Integer days, Integer minutes) {
        this.days = days;
        this.minutes = minutes;
    }

    public Date(int minutes) {
        this.days = minutes / 1440;
        this.minutes = minutes % 1440;
    }

    @Override
    public String toString() {
        return "Date{" +
            "days=" + days +
            ", minutes=" + minutes +
            '}';
    }

    @Override
    public int compareTo(Date o) {
        int d = this.days.compareTo(o.days);
        if (d != 0) {
            return d;
        } else {
            return this.minutes.compareTo(o.minutes);
        }
    }
}
