package com.example.java.structure.demo.thread;

import java.io.IOException;

/**
 * 基于 synchronized 同步块的示例，线程处于 `BLOCKED`
 *
 * 启动命令：
 *     java -cp java-structure-demo-0.0.1-SNAPSHOT.jar com.example.java.structure.demo.thread.ThreadBlockedStatusDemo
 *
 * 线程堆栈内容：
 * "t2" #13 prio=5 os_prio=0 tid=0x000000001f11e000 nid=0x24c8 waiting for monitor entry [0x000000001ffef000]
 *    java.lang.Thread.State: BLOCKED (on object monitor)
 *         at com.demo.structure.ThreadBlockedStatusDemo$Worker.run(ThreadBlockedStatusDemo.java:25)
 *         - waiting to lock <0x000000076b621e80> (a java.lang.Object)
 *         at java.lang.Thread.run(Thread.java:748)
 *
 * "t1" #12 prio=5 os_prio=0 tid=0x000000001f11c800 nid=0xcc8 waiting on condition [0x000000001feee000]
 *    java.lang.Thread.State: TIMED_WAITING (sleeping)
 *         at java.lang.Thread.sleep(Native Method)
 *         at com.demo.structure.ThreadBlockedStatusDemo$Worker.run(ThreadBlockedStatusDemo.java:27)
 *         - locked <0x000000076b621e80> (a java.lang.Object)
 *         at java.lang.Thread.run(Thread.java:748)
 *
 * "main" #1 prio=5 os_prio=0 tid=0x000000000343e800 nid=0x2a3c in Object.wait() [0x0000000002d9f000]
 *    java.lang.Thread.State: WAITING (on object monitor)
 *         at java.lang.Object.wait(Native Method)
 *         - waiting on <0x000000076b621ea0> (a java.lang.Thread)
 *         at java.lang.Thread.join(Thread.java:1252)
 *         - locked <0x000000076b621ea0> (a java.lang.Thread)
 *         at java.lang.Thread.join(Thread.java:1326)
 *         at com.demo.structure.ThreadBlockedStatusDemo.main(ThreadBlockedStatusDemo.java:16)
 */
public class ThreadBlockedStatusDemo {

    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("main");

        Thread t1 = new Thread(new Worker(), "t1");
        Thread t2 = new Thread(new Worker(), "t2");

        t1.start();
        t2.start();
        // t1线程在sleep时，处于`TIMED_WAITING`状态；
        // 此时t2线程等待获取监视器，处于`BLOCKED`状态；
        // 主线程main处于`WAITING`状态
        t1.join();
        t2.join();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class Worker implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                System.out.println("Thread: " + Thread.currentThread().getName() + " is running.");
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
