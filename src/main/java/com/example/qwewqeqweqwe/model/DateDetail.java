package com.example.qwewqeqweqwe.model;

import lombok.Data;

@Data
public class DateDetail implements Comparable<DateDetail> {
    private Integer days;
    private Integer hours;
    private Integer minutes;

    public DateDetail(Integer days, Integer hours, Integer minutes) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    public DateDetail(Date date) {
        this.days = date.getDays();
        this.hours = date.getMinutes() / 60;
        this.minutes = date.getMinutes() % 60;
    }

    public DateDetail(int minutes) {
        this.days = minutes / 1440;
        this.hours = minutes / 60;
        this.minutes = minutes % 60;
    }

    @Override
    public String toString() {
        return "DateDetail{" +
            "days=" + days +
            ", hours=" + hours +
            ", minutes=" + minutes +
            '}';
    }

    @Override
    public int compareTo(DateDetail o) {
        int d = this.days.compareTo(o.days);
        int h = this.hours.compareTo(o.hours);
        if (d != 0) {
            return d;
        } else if (h != 0) {
            return h;
        } else {
            return this.minutes.compareTo(o.minutes);
        }
    }
}
