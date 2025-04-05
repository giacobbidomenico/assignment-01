package pcd.ass01.utility;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncRunning {
    private boolean isRunning;
    private final Lock mutex;

    public SyncRunning() {
        this.mutex = new ReentrantLock();
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

    public void run() {
        try {
            mutex.lock();
            this.isRunning = true;
        } finally {
            mutex.unlock();
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
