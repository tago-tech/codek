package com.kuaishou.kcode;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
	author by tangsz
*/

public class Consumer extends Thread {
    BlockingQueue<List<Operation>> deque;
    ConcurrentHashMap<FTBean,ResultBean> map;
    List<Operation> singalStop;
    public Consumer (BlockingQueue<List<Operation>> deque,ConcurrentHashMap<FTBean,ResultBean> map , List<Operation> singalStop) {
        this.deque = deque;
        this.map = map;
        this.singalStop = singalStop;
    }

    @Override
    public void run() {
        /***
         * Consumer takes out one second of data at a time
         * */
        List<Operation> unit;
        long csum = 0 , ccounter = 0;
        long cstart = System.currentTimeMillis() , cend;
        try {
            while (true) {
                unit = deque.take();
                // time to exit
                if (unit == singalStop) {
                    break;
                }
                long start = System.currentTimeMillis();
                //cal avg , max , p99 etc...
                processPerSc(unit,map);

                //time recorder
                cend = System.currentTimeMillis();
                ccounter++;
                csum += (cend - cstart);
                cstart = cend;
            }
            System.out.println("C-MEAN : " + (csum / ccounter));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void processPerSc (List<Operation> units, ConcurrentHashMap<FTBean,ResultBean> map) {
        long currentTime = units.get(0).getTime();
        //sorted by func-name , then by time
        Collections.sort(units);
        int left = 0, right = 0 , n = units.size();
        String curFunc;
        while (right < n) {
            curFunc = units.get(left).getFunc();
            int max = 0 , sum = units.get(left).getDuration() , avg = 0;
            while (right < n - 1 && units.get(right + 1).getFunc().equals(curFunc)) {
                right++;
                sum += units.get(right).getDuration();
            }
            //[left - right] corresponds to data in 1 second
            int occurs = right - left + 1;
            //avg
            avg = (int) Math.ceil(1.0 * sum / (occurs));
            //max
            max = units.get(right).getDuration();
            //p99 , p50
            int p99 = units.get(left + (int)Math.ceil(occurs * 0.99) - 1).getDuration();
            int p50 = units.get(left + (int)Math.ceil(occurs * 0.50) - 1).getDuration();

            //concurrentHashMap , may blocking
            map.put(new FTBean(curFunc,currentTime),new ResultBean(occurs,p99,p50,avg,max));

            //next func-name
            left = right + 1;
            right = left;
        }
    }
}
