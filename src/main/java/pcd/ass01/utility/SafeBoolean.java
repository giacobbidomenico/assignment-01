package pcd.ass01.utility;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SafeBoolean {

    private boolean bool;
    private final Lock lock = new ReentrantLock();

    public void setFalse() {
        lock.lock();
        try {
            bool = false;
        } finally {
            lock.unlock();
        }
    }

    public void setTrue() {
        lock.lock();
        try {
            bool = true;
        } finally {
            lock.unlock();
        }
    }

    public boolean get() {
        lock.lock();
        try {
            return bool;
        } finally {
            lock.unlock();
        }
    }
}

