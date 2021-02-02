package com.example.java.structure.demo.thread;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 ReentrantLock 锁和 Condition 条件的示例
 * ReentrantLock的队列基于LockSupport.park实现等待，所以线程处于 `WAITING` 状态
 *
 * 启动命令：
 *     java -cp java-structure-demo-0.0.1-SNAPSHOT.jar com.example.java.structure.demo.thread.ThreadWaitingByLockWithConditionDemo
 *
 * 线程堆栈内容：
 * "producer-thread" #13 prio=5 os_prio=0 tid=0x000000001ec29000 nid=0x35d0 waiting on condition [0x000000001faef000]
 *    java.lang.Thread.State: TIMED_WAITING (sleeping)
 *         at java.lang.Thread.sleep(Native Method)
 *         at com.demo.structure.ThreadWaitingByLockWithConditionDemo$Producer.run(ThreadWaitingByLockWithConditionDemo.java:31)
 *         at java.lang.Thread.run(Thread.java:748)
 *
 * "consumer-thread" #12 prio=5 os_prio=0 tid=0x000000001ec28800 nid=0x1520 waiting on condition [0x000000001f9ef000]
 *    java.lang.Thread.State: WAITING (parking)
 *         at sun.misc.Unsafe.park(Native Method)
 *         - parking to wait for  <0x000000076b629ef0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
 *         at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
 *         at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
 *         at com.demo.structure.ThreadWaitingByLockWithConditionDemo$Consumer.run(ThreadWaitingByLockWithConditionDemo.java:48)
 *         at java.lang.Thread.run(Thread.java:748)
 *
 * "main" #1 prio=5 os_prio=0 tid=0x00000000030e3800 nid=0x258c in Object.wait() [0x00000000030af000]
 *    java.lang.Thread.State: WAITING (on object monitor)
 *         at java.lang.Object.wait(Native Method)
 *         - waiting on <0x000000076b629f18> (a java.lang.Thread)
 *         at java.lang.Thread.join(Thread.java:1252)
 *         - locked <0x000000076b629f18> (a java.lang.Thread)
 *         at java.lang.Thread.join(Thread.java:1326)
 *         at com.demo.structure.ThreadWaitingByLockWithConditionDemo.main(ThreadWaitingByLockWithConditionDemo.java:21)
 */
public class ThreadWaitingByLockWithConditionDemo {

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Condition NOT_EMPTY = LOCK.newCondition();

    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("main");

        Thread t1 = new Thread(new ThreadWaitingByLockWithConditionDemo.Consumer(), "consumer-thread");
        Thread t2 = new Thread(new ThreadWaitingByLockWithConditionDemo.Producer(), "producer-thread");

        t1.start();
        // 保证t1线程先启动
        Thread.sleep(5 * 1000);
        t2.start();
        // t1线程在 NOT_EMPTY.await() 后，处于`WAITING`状态；
        t1.join();
        t2.join();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Producer implements Runnable {

        @Override
        public void run() {
            LOCK.lock();
            try {
                System.out.println("Thread: " + Thread.currentThread().getName() + " is running.");
                Thread.sleep(30 * 1000);
                NOT_EMPTY.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                LOCK.unlock();
                System.out.println("Thread: " + Thread.currentThread().getName() + " is completed.");
            }
        }
    }

    private static class Consumer implements Runnable {

        @Override
        public void run() {
            LOCK.lock();
            try {
                System.out.println("Thread: " + Thread.currentThread().getName() + " is running.");
                NOT_EMPTY.await();
                System.out.println("Thread: " + Thread.currentThread().getName() + " is resumed.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                LOCK.unlock();
                System.out.println("Thread: " + Thread.currentThread().getName() + " is completed.");
            }
        }
    }
}
