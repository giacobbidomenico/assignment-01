package pcd.ass01.utility;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncRunning {
    private boolean isRunning;
    private final Lock mutex;

    private final Condition startCondition;

    public SyncRunning() {
        this.mutex = new ReentrantLock();
        this.startCondition = this.mutex.newCondition();
        this.isRunning = false;
    }

    public void stop() {
        try {
            mutex.lock();
            this.isRunning = false;
        } finally {
            mutex.unlock();
        }
    }

    public void start() {
        try {
            mutex.lock();
            this.isRunning = true;
            this.startCondition.signalAll();
        } finally {
            mutex.unlock();
        }
    }

    public void waitUntilStart()  {
        try {
            mutex.lock();
            while(!this.isRunning) {
                this.startCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            mutex.unlock();
        }
    }
}
