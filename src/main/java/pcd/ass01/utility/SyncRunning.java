package pcd.ass01.utility;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncRunning {
    private boolean isStopped;
    private boolean isWaiting;
    private final Lock mutex;
    private final Condition suspendedCondition;
    private final Condition waitEnteredCondition;

    public SyncRunning() {
        this.mutex = new ReentrantLock();
        this.suspendedCondition = mutex.newCondition();
        this.waitEnteredCondition = mutex.newCondition();
        this.isStopped = false;
        this.isWaiting = false;
    }

    public void waitIfStopped() {
        mutex.lock();
        try {
            while (isStopped) {
                isWaiting = true;
                waitEnteredCondition.signalAll();
                suspendedCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            isWaiting = false;
            mutex.unlock();
        }
    }

    public void waitUntilArriveWait() {
        mutex.lock();
        try {
            while (!isWaiting) {
                waitEnteredCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            mutex.unlock();
        }
    }

    public void stop() {
        mutex.lock();
        try {
            isStopped = true;
        } finally {
            mutex.unlock();
        }
    }

    public void active() {
        mutex.lock();
        try {
            isStopped = false;
            suspendedCondition.signalAll();
        } finally {
            mutex.unlock();
        }
    }
}
