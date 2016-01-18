package net.nueca.concessioengine.objects;

/**
 * Created by rhymart on 1/8/16.
 */
public class Day {

    private String fullname;
    private String shortname;
    private int dayOfWeek = 1;

    public Day(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Day(String fullname, int dayOfWeek) {
        this.fullname = fullname;
        this.dayOfWeek = dayOfWeek;
    }

    public Day(String fullname, String shortname, int dayOfWeek) {
        this.fullname = fullname;
        this.shortname = shortname;
        this.dayOfWeek = dayOfWeek;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    public boolean equals(Object o) {
        return dayOfWeek == ((Day)o).dayOfWeek;
    }

    @Override
    public String toString() {
        return fullname;
    }
}
