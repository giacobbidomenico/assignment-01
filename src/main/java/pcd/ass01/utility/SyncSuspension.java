package pcd.ass01.utility;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncSuspension {
    private boolean isSuspended;
    private final Lock mutex;
    private final Condition suspendedCondition;

    public SyncSuspension() {
        this.mutex = new ReentrantLock();
        this.suspendedCondition = this.mutex.newCondition();
        this.isSuspended = false;
    }


    public void suspend() {
        try {
            mutex.lock();
            this.isSuspended = true;
        } finally {
            mutex.unlock();
        }
    }

    public void resume() {
        try {
            mutex.lock();
            this.isSuspended = false;
            this.suspendedCondition.signalAll();
        } finally {
            mutex.unlock();
        }
    }

    public void suspensionUntilResume()  {
        try {
            mutex.lock();
            while(this.isSuspended) {
                this.suspendedCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            mutex.unlock();
        }
    }

    public void changeState() {
        try {
            mutex.lock();
            if(isSuspended) {
                resume();
            } else {
                suspend();
            }
        } finally {
            mutex.unlock();
        }
    }
}
