package com.kuaishou.kcode;

public class Operation implements Comparable<Operation>{
    long time;
    String func;
    int duration;
    public Operation (long time , String func , int duration) {
        this.time = time;
        this.func = func;
        this.duration = duration;
    }
    //getter
    public long getTime () {
        return this.time;
    }
    public String getFunc () {
        return this.func;
    }
    public int getDuration () {
        return this.duration;
    }
    //setter

    public static Operation parser (String line) {
        String[] segments = line.split(",");
        long time = Long.parseLong(segments[0]) / 1000;
        String func = segments[1];
        int duration = Integer.parseInt(segments[2]);
        return new Operation(time,func,duration);
    }

    @Override
    public String toString() {
        return time + "," + func + "," + duration;
    }

    @Override
    public int compareTo(Operation operation) {
        /**
         * Sort by method name, then by time
         * In this way, we can slice the data
         * */
        if (!this.func.equals(operation.getFunc())) {
            return this.func.compareTo(operation.func);
        }
        else {
            return (int)(this.duration - operation.duration);
        }
    }
}
