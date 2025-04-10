package pcd.ass01.utility;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncRunning {
    private boolean isStopped;
    private boolean isThreadWaiting;
    private final Lock mutex;
    private final Condition suspendedCondition;
    private final Condition waitEnteredCondition;

    public SyncRunning() {
        this.mutex = new ReentrantLock();
        this.suspendedCondition = mutex.newCondition();
        this.waitEnteredCondition = mutex.newCondition();
        this.isStopped = false;
        this.isThreadWaiting = false;
    }

    public void waitIfStopped() {
        mutex.lock();
        try {
            while (isStopped) {
                isThreadWaiting = true;
                waitEnteredCondition.signalAll(); // Notifica che il thread è in attesa
                suspendedCondition.await();       // Attesa effettiva
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            isThreadWaiting = false; // reset
            mutex.unlock();
        }
    }

    public void waitUntilArriveWait() {
        mutex.lock();
        try {
            while (!isThreadWaiting) {
                waitEnteredCondition.await(); // Attende che il primo thread si sospenda
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
            suspendedCondition.signalAll(); // Risveglia i thread sospesi
        } finally {
            mutex.unlock();
        }
    }
}
