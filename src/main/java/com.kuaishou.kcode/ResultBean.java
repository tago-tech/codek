package com.kuaishou.kcode;

public class ResultBean {
    int qps;
    int p99;
    int p50;
    int avg;
    int max;
    public ResultBean (int qps , int p99 , int p50 , int avg , int max) {
        this.qps = qps;
        this.p99 = p99;
        this.p50 = p50;
        this.avg = avg;
        this.max = max;
    }
    @Override
    public String toString() {
        return this.qps + "," + this.p99 + "," + this.p50 + "," + this.avg + "," + this.max;
    }
}
