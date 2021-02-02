package com.example.java.structure.demo.thread;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 ReentrantLock 锁的示例
 * ReentrantLock的队列基于LockSupport.park实现等待，所以线程处于 `WAITING` 状态
 *
 * 启动命令：
 *     java -cp java-structure-demo-0.0.1-SNAPSHOT.jar com.example.java.structure.demo.thread.ThreadWaitingByLockDemo
 *
 *
 * 线程堆栈内容：
 * "t2" #13 prio=5 os_prio=0 tid=0x000000001f446800 nid=0x1e14 waiting on condition [0x000000002030f000]
 *    java.lang.Thread.State: WAITING (parking)
 *         at sun.misc.Unsafe.park(Native Method)
 *         - parking to wait for  <0x000000076b627bd0> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
 *         at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
 *         at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
 *         at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
 *         at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
 *         at java.util.concurrent.locks.ReentrantLock$NonfairSync.lock(ReentrantLock.java:209)
 *         at java.util.concurrent.locks.ReentrantLock.lock(ReentrantLock.java:285)
 *         at com.demo.structure.ThreadWaitingByLockDemo$Worker.run(ThreadWaitingByLockDemo.java:27)
 *         at java.lang.Thread.run(Thread.java:748)
 *
 * "t1" #12 prio=5 os_prio=0 tid=0x000000001f446000 nid=0x2ba4 waiting on condition [0x000000002020f000]
 *    java.lang.Thread.State: TIMED_WAITING (sleeping)
 *         at java.lang.Thread.sleep(Native Method)
 *         at com.demo.structure.ThreadWaitingByLockDemo$Worker.run(ThreadWaitingByLockDemo.java:31)
 *         at java.lang.Thread.run(Thread.java:748)
 *
 * "main" #1 prio=5 os_prio=0 tid=0x0000000003873800 nid=0x18b0 in Object.wait() [0x00000000037df000]
 *    java.lang.Thread.State: WAITING (on object monitor)
 *         at java.lang.Object.wait(Native Method)
 *         - waiting on <0x000000076b627c00> (a java.lang.Thread)
 *         at java.lang.Thread.join(Thread.java:1252)
 *         - locked <0x000000076b627c00> (a java.lang.Thread)
 *         at java.lang.Thread.join(Thread.java:1326)
 *         at com.demo.structure.ThreadWaitingByLockDemo.main(ThreadWaitingByLockDemo.java:20)
 */
public class ThreadWaitingByLockDemo {

    private static final ReentrantLock LOCK = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("main");

        Thread t1 = new Thread(new ThreadWaitingByLockDemo.Worker(), "t1");
        Thread t2 = new Thread(new ThreadWaitingByLockDemo.Worker(), "t2");

        t1.start();
        t2.start();
        // t1线程在sleep时，处于`TIMED_WAITING`状态；
        // 此时t2线程等待获取监视器，处于`WAITING`状态；
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
            LOCK.lock();
            try {
                System.out.println("Thread: " + Thread.currentThread().getName() + " is running.");
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                LOCK.unlock();
            }
        }
    }
}
