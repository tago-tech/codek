package com.kuaishou.kcode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kcode
 * Created on 2020-05-20
 */
public class KcodeMain {

    public static void main(String[] args) throws Exception {
        long frist_start = System.currentTimeMillis() / 1000 , frist_end = 0, second_start = 0, second_end = 0;
        // "demo.data" 是你从网盘上下载的测试数据，这里直接填你的本地绝对路径
        String path2Demo = "/home/tangsz/kcode/warmup-test.data";
        String path2Result = "/home/tangsz/kcode/result-test.data";
        InputStream fileInputStream = new FileInputStream(path2Demo);
        Class<?> clazz = Class.forName("com.kuaishou.kcode.KcodeQuestion");
        Object instance = clazz.newInstance();
        Method prepareMethod = clazz.getMethod("prepare", InputStream.class);
        Method getResultMethod = clazz.getMethod("getResult", Long.class, String.class);
        // 调用prepare()方法准备数据
        prepareMethod.invoke(instance, fileInputStream);

        frist_end = System.currentTimeMillis() / 1000;
        second_start = System.currentTimeMillis() / 1000;
        // 验证正确性
        // "result.data" 是你从网盘上下载的结果数据，这里直接填你的本地绝对路径
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path2Result)));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split = line.split("\\|");
            String[] keys = split[0].split(",");
            // 调用getResult()方法
            Object result = getResultMethod.invoke(instance, new Long(keys[0]), keys[1]);

            if (!split[1].equals(result)) {
                System.out.println("fail");
            }
            // System.out.println("success");
        }

        second_end = System.currentTimeMillis() / 1000;
        System.out.println("Stage-Frist：" + (frist_end - frist_start));
        System.out.println("Stage-Second : " + (second_end - second_start));
        System.out.println("Total : " + (second_end - frist_start));
    }
}