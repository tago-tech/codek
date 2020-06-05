package com.kuaishou.kcode;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Producer extends Thread {
    List<BlockingQueue<List<Operation>>> deques;
    InputStream inputStream;
    int nConsumer;
    List<Operation> singalStop;
    public Producer (List<BlockingQueue<List<Operation>>> deques, InputStream inputStream , int nConsumer , List<Operation> singalStop) {
        super();
        this.deques = deques;
        this.inputStream = inputStream;
        //on channel for one consumer,so.
        this.nConsumer = nConsumer;
        this.singalStop = singalStop;
    }
    @Override
    public void run() {
        /**
         * The producer puts the data of one second in a row into multiple buffer queues as a unit
         * */
        int initCap = 1024 * 16;
        String line;
        //inputStream , read data line by line from it
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Operation operation;
        //data pre second record
        List<Operation> datasPreSecond = new ArrayList<>(initCap);
        //last time-stamp
        long last = -1;
        //index channel
        int indexQueue = 0;
        long pcounter = 0 , pspeedSum = 0;
        long pstart = System.currentTimeMillis() , pend;
        try {
            while ((line = reader.readLine()) != null) {
                operation = Operation.parser(line);
                //last time-stamp if to older , put it
                if (last != -1 && operation.getTime() != last) {
                    //select channle from  mutil-channeles
                    deques.get((indexQueue = (indexQueue + 1) % nConsumer)).put(datasPreSecond);

                    //time recorder
                    pend = System.currentTimeMillis();
                    pcounter++;
                    pspeedSum += (pend - pstart);
                    pstart = pend;

                    //init for next second
                    datasPreSecond = new ArrayList<>(initCap);
                }
                //add current data
                datasPreSecond.add(operation);
                //update the newest time-stamp
                last = operation.getTime();
            }
            System.out.println("P-MEAN : " + (pspeedSum / pcounter) );

            //if data remaing
            if (datasPreSecond.size() > 0) {
                deques.get((indexQueue = (indexQueue + 1) % nConsumer)).put(datasPreSecond);
                datasPreSecond = null;
            }
            //singal to kill consumers
            for (int i = 0;i < nConsumer;i++) {
                deques.get(i).put(singalStop);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
