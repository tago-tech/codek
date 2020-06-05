package com.kuaishou.kcode;

public class FTBean {
    public String func;
    public long timeStamp;
    public final int hc;
    public FTBean (String func , long timeStamp) {
        this.func = func;
        this.timeStamp = timeStamp;
        // XOR hashcode can achive higher performence , because hash algo.
        this.hc = func.hashCode() ^ ((Long)timeStamp).hashCode();
        //this.hc = ((Long)timeStamp).hashCode();
    }
    @Override
    public int hashCode() {
        return this.hc;
    }
    @Override
    public boolean equals(Object obj) {
        FTBean ftBean = (FTBean)obj;
        return this.func.equals(ftBean.func) && this.timeStamp == ftBean.timeStamp;
    }
    @Override
    public String toString() {
        return "(" + func + "，" + timeStamp + ")";
    }
    public String getFunc () {
        return this.func;
    }
    public long getTime () {
        return this.timeStamp;
    }
}
