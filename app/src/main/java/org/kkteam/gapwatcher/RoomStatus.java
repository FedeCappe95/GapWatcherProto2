/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kkteam.gapwatcher;

import java.sql.Time;

/**
 *
 * @author Federico
 */
public class RoomStatus {
    
    protected String name;
    protected Interval[] intervals;

    public RoomStatus(String name, Interval[] intervals) {
        this.name = name;
        this.intervals = intervals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Interval[] getIntervals() {
        return intervals;
    }

    public void setIntervals(Interval[] intervals) {
        this.intervals = intervals;
    }
    
    public String freeUntil(int hour, int minutes) {
        Time time = new Time(hour, minutes, 0);
        for(Interval i : intervals) {
            Time start = new Time(
                Integer.parseInt(i.getStart().split(":")[0]),
                Integer.parseInt(i.getStart().split(":")[1]),
                0
            );
            Time end = new Time(
                Integer.parseInt(i.getEnd().split(":")[0]),
                Integer.parseInt(i.getEnd().split(":")[1]),
                0
            );
            if((time.after(start) || time.equals(start)) && time.before(end)) {
                return i.getEnd();
            }
        }
        return "NO";
    }
    
}