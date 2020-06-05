## Project Overview
    Key-Word : Producer-Consumer 、 Java 、Mutil-Thread 、BIO 
----------------------------
    From K-Code warmup.
    Description:
        在工程实习过程中开发同学经常需要关注一个方法或接口的调用次数和处理时间，通常请求量衡量标准有QPS, 响应时间的衡量标准有P99, P50,
 AVG, MAX等,分别对应99分位响应时间，中位数时间，平均响应时间和最长耗时。
    Content:
        实现一个计算QPS,P99, P50, AVG, MAX的程序，要求包含的功能如下：
        实现一个接收打点数据的接口，输入数据格式下面会给出
        实现一个查询QPS,P99,P50,AVG,MAX的接口，接口参数和输出下面给出
        P99、P50 相关定义:如果将一组数据从小到大排序，并计算相应的累计百分位，则某一百分位所对应数据的值就称为这一百分位的百分位数。
可表示为：一组n个观测值按数值大小排列。如，处于p%位置的值称第p百分位数。
---------------------------
![arch](https://github.com/tago-tech/codek/blob/master/arch.jpg)

    项目使用了消费者-生产者模型去解决问题，在这个场景中，生产者负责从磁盘顺序读元数据，并解析元数据字符串中各列，然后将数据分发到缓冲
队列中；消费者从缓冲队列中取出消息，然后计算【AVG、MAX、P99】等指标，最后将数据写入ConcurrenHashMap中；这样在第二阶段查询阶段，简单使
用map.get就可以获得数据.

项目中一些小细节:
### 单生产者多消费者
    最简单的方式是在一个线程中IO -> Process -> IO , 耗时肯定大于 Math.max (IO，process),并发的情况能够显著提高吞吐量
    a. N：M 的选择
    磁盘顺序读写的速度相对较快，使用单个生产者能够保持较高的IO吞吐量，同时为了提高并发，必须将单位数据分发到不同的线程中，
由消费解析、存储；但消费者对单通道take 数据中难免因线程过多导致过多的线程阻塞、来回切换，因此消费者的数量不能太多；
    b. M 的确定
    通过对本地测试数据的分阶段计时统计，发现对于 从磁盘开始读入到元数据解析最后到生产者发送到缓冲队列，平均时间是20ms;
而消费者拿出一个单位的数据，排序 -> 计算各个指标，最后分别放入 concurrentHashMap 的耗时大约是 50+ 毫秒；
    这样意味着N:M 大约为 1：3.经过实验对比也发现，当提高消费者数量[2->3->4->5]是，消费单位数据的时间大致对应[58->55->60->63].
经过分析，这种现象的原因应该是消费者数量太多时,消费者去缓冲队列take数据时，线程竞争较大，频繁的阻塞和线程切换所带来的。
    c. 数据单位的确定
    一个单位的数据或者说一段数据被作为一个整体，从生产者传递到消费者，这里选用的是一秒内的所有操作集合(可能会有更优的，暂时没想到)
### Map 及 Map 中 Key 的确定
    赛题要求返回某一秒second，方法func被调用的指标，鉴于多消费者，为了并发安全选用了ConcurrentHashMap。关键在于key的确定，
以单second和func作为key去插入访问数据都不妥，因为JDK1.8中CHM锁的粒度降为了单个槽，这意味着槽数越多1并发度也就越高.
### 调优思路
   1.消费者的简化:多线程负载肯定要做。目前主要的问题在于生产者性能瓶颈，平均解析1秒的数据耗时20ms左右，这个性能提不上去，
N:M中的M就被限制死。所以应该把生产者的任务再简化,提高并发度。
   2.延迟计算 : 目前这个项目使用map方式获取指标，第二阶段基本不耗时.但第二阶段查询的数据相对于第一阶段量肯定要少，第一阶段现
在是默认把所有的数据全计算了，有些第二阶段用不上，浪费了资源，或许能够把指标的计算推迟到第二阶段一些。
   


    
