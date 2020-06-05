package com.kuaishou.kcode;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author kcode
 * Created on 2020-05-20
 */
public class KcodeQuestion {
    //Number of consumers
    int nConsumers = 4;
    //queue cap
    int initQCap = 1024 * 2;
    /**
     * Compared with version 1.7, it improves the concurrency level,
     * so it is suitable for multi value index and can make the data more evenly distributed.
     */
    ConcurrentHashMap<FTBean,ResultBean> map = new ConcurrentHashMap<>(1024  * 16);
    List<Operation> singalStop = new ArrayList<>(0);
    List<BlockingQueue<List<Operation>>> deques = new ArrayList<>();

    /**
     * The prepare () method is used to accept the input data set, and the data set format reference README.md
     * producer : consumer = 1 : N;
     * 1.Use blocking queue as container, select bounded queue to avoid memory overflow
     * 2.one queue for one consumer
     * @param inputStream
     */
    public void prepare(InputStream inputStream) throws IOException , InterruptedException {
        //initilize queues
        for (int i = 0;i < nConsumers;i++) {
            //After experimental comparison,"Array" fast than "Link" here;
            deques.add(new ArrayBlockingQueue<>(initQCap));
        }
        Producer producer = new Producer(deques,inputStream,nConsumers,singalStop);
        producer.start();
        Consumer[] consumers = new Consumer[nConsumers];
        for (int i = 0;i < nConsumers;i++) {
            consumers[i] = new Consumer(deques.get(i),map,singalStop);
            consumers[i].start();
        }
        //waite for worker done
        for (int i = 0;i < nConsumers;i++) {
            consumers[i].join();
        }
        producer.join();

        deques = null;

        return;
    }
    /**
     * getresult() method is called by kcode profiling system and is part of the correctness of the profiling program.
     * Please return the correct data according to the requirements of the topic
     * @param timestamp time-stamp
     * @param methodName func-name
     */
    public String getResult(Long timestamp, String methodName) {
        // do something
        return map.get(new FTBean(methodName,timestamp)).toString();
    }
}
